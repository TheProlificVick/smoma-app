package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import smoma.controller.model.Service.Role;
import smoma.dto.AdDirectoryEntryDTO;
import smoma.dto.LdapUserDTO;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Service that connects to the ART Active Directory and returns the ENTIRE contents
 * of the directory (users, groups, distributors, Organizational Units, contacts, computers...)
 * using the ldap.url configured in application.properties (spring.ldap.*).
 * This is used by the Admin and AD Sync modules to fortify role-based access.
 */
@Service
public class LdapDirectoryService {

    // LDAP Configuration sourced from application.properties (spring.ldap.*)
    @Value("${spring.ldap.urls:}")
    private String ldapUrls;

    @Value("${spring.ldap.base:dc=art,dc=cm}")
    private String baseDn;

    @Value("${spring.ldap.username:}")
    private String adminUser;

    @Value("${spring.ldap.password:}")
    private String adminPass;

    // Comprehensive set of AD attributes captured for every directory entry.
    private static final String[] ALL_ATTRIBUTES = {
        // Identity
        "distinguishedName", "cn", "name", "objectClass", "objectCategory",
        // User identity
        "sAMAccountName", "userPrincipalName", "givenName", "sn", "displayName", "mail",
        // Functions / organisation
        "title", "department", "company", "office", "physicalDeliveryOfficeName",
        // Communications
        "telephoneNumber", "mobile", "facsimileTelephoneNumber", "otherTelephone",
        // HR / Employee data
        "employeeID", "employeeNumber", "employeeType", "description", "division", "manager",
        // Address
        "streetAddress", "postalCode", "l", "st", "co", "c", "postOfficeBox",
        // Group memberships
        "memberOf", "directReports", "member",
        // Account status
        "userAccountControl", "pwdLastSet", "lastLogon", "lastLogoff", "accountExpires",
        "whenCreated", "whenChanged", "badPwdCount", "badPasswordTime", "logonCount",
        "primaryGroupID", "rid", "samAccountType",
        // User profile
        "homeDirectory", "homeDrive", "scriptPath", "profilePath", "userParameters",
        "accountExpiresText", "lockedOut", "enabled",
        // Computer / group
        "operatingSystem", "operatingSystemVersion", "dNSHostName", "servicePrincipalName",
        // Legacy netbios
        "userAccountStatus", "comment", "info", "personalTitle"
    };

    public boolean authenticate(String username, String password) {
        if (ldapUrls == null || ldapUrls.isBlank() || username == null || username.isBlank() || password == null) {
            return false;
        }
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrls);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username.trim());
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            DirContext context = new InitialDirContext(env);
            context.close();
            return true;
        } catch (NamingException ignored) {
            return false;
        }
    }

    private DirContext connect() throws NamingException {
        if (ldapUrls == null || ldapUrls.isBlank()) {
            throw new NamingException("LDAP is not configured. Set spring.ldap.urls in application-local.properties.");
        }
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrls);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, adminUser);
        env.put(Context.SECURITY_CREDENTIALS, adminPass);
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("java.naming.ldap.attributes.binary", "-");
        return new InitialDirContext(env);
    }

    private boolean isAccountActive(Attributes attrs) {
        try {
            Attribute uacAttr = attrs.get("userAccountControl");
            if (uacAttr != null) {
                int uac = Integer.parseInt(uacAttr.get().toString());
                return (uac & 2) == 0;
            }
        } catch (Exception ignored) {}
        return true;
    }

    private String extractMatricule(Attributes attrs) {
        String[] priorityList = {"description", "info", "comment", "employeeID", "employeeNumber"};
        for (String attr : priorityList) {
            String val = getAttributeValue(attrs, attr);
            if (!val.isEmpty()) {
                return val;
            }
        }
        return "";
    }

    private String getAttributeValue(Attributes attrs, String attrName) {
        try {
            Attribute attr = attrs.get(attrName);
            if (attr != null && attr.get() != null) {
                String val = attr.get().toString().trim();
                return ("[]".equals(val) || "None".equalsIgnoreCase(val)) ? "" : val;
            }
        } catch (NamingException ignored) {}
        return "";
    }

    private List<String> getAttributeValues(Attributes attrs, String attrName) {
        List<String> values = new ArrayList<>();
        try {
            Attribute attr = attrs.get(attrName);
            if (attr != null) {
                NamingEnumeration<?> all = attr.getAll();
                while (all.hasMore()) {
                    String val = String.valueOf(all.next()).trim();
                    if (!val.isEmpty() && !"[]".equals(val) && !"None".equalsIgnoreCase(val)) {
                        values.add(val);
                    }
                }
            }
        } catch (NamingException ignored) {}
        return values;
    }

    /**
     * Dynamic role resolution based on Active Directory titles, groups and identifiers.
     * Fortified RBAC: maps AD group memberships (memberOf) and title to smoma roles.
     */
    public Role mapAdAttributesToRole(String title, String username, List<String> memberOf) {
        if (username != null && (username.equalsIgnoreCase("admin") || username.equalsIgnoreCase("lucien.mba"))) {
            return Role.ROLE_ADMIN;
        }

        // AD group based mapping — هذهالمجموعات تعادل الأدوار
        if (memberOf != null) {
            String groups = String.join(",", memberOf).toLowerCase();
            if (groups.contains("smoma-admin") || groups.contains("domain admins") || groups.contains("administrators")) {
                return Role.ROLE_ADMIN;
            }
            if (groups.contains("smoma-general-manager") || groups.contains("general managers") || groups.contains("smoma-gm")) {
                return Role.ROLE_GENERAL_MANAGER;
            }
            if (groups.contains("smoma-hr") || groups.contains("human resources") || groups.contains("smoma-hr-officer")) {
                return Role.ROLE_HR_OFFICER;
            }
            if (groups.contains("smoma-directeur") || groups.contains("directeurs") || groups.contains("smoma-director")) {
                return Role.ROLE_DIRECTEUR;
            }
            if (groups.contains("smoma-chef-service") || groups.contains("chefs de service") || groups.contains("smoma-hod")) {
                return Role.ROLE_CHEF_SERVICE;
            }
            if (groups.contains("smoma-department-representative") || groups.contains("department representatives")) {
                return Role.ROLE_DEPARTMENT_REPRESENTATIVE;
            }
            if (groups.contains("smoma-staff") || groups.contains("staff")) {
                return Role.ROLE_STAFF_MEMBER;
            }
        }

        if (title != null) {
            String lowerTitle = title.toLowerCase();
            if (lowerTitle.contains("directeur") || lowerTitle.contains("chef de département")) {
                return Role.ROLE_DIRECTEUR;
            } else if (lowerTitle.contains("chef de service") || lowerTitle.contains("responsable")) {
                return Role.ROLE_CHEF_SERVICE;
            } else if (lowerTitle.contains("administrateur") || lowerTitle.contains("sysadmin") || lowerTitle.contains("admin")) {
                return Role.ROLE_ADMIN;
            } else if (lowerTitle.contains("general manager") || lowerTitle.contains("directeur général")) {
                return Role.ROLE_GENERAL_MANAGER;
            } else if (lowerTitle.contains("rh") || lowerTitle.contains("ressources humaines") || lowerTitle.contains("hr")) {
                return Role.ROLE_HR_OFFICER;
            }
        }

        return Role.ROLE_AGENT;
    }

    public Role mapAdAttributesToRole(String title, String username) {
        return mapAdAttributesToRole(title, username, Collections.emptyList());
    }

    /**
     * Returns the ENTIRE ACTIVE DIRECTORY as a list of AdDirectoryEntryDTO.
     * This includes ALL object classes: users, groups, OUs, contacts, computers, etc.
     */
    public List<AdDirectoryEntryDTO> getAllDirectoryEntries() throws NamingException {
        DirContext ctx = connect();
        List<AdDirectoryEntryDTO> entries = new ArrayList<>();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(ALL_ATTRIBUTES);
        controls.setCountLimit(0); // no limit — entire directory

        String filter = "(|(objectClass=user)(objectClass=group)(objectClass=contact)(objectClass=computer)(objectClass=organizationalUnit)(objectClass=domain))";
        NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, controls);

        while (results.hasMore()) {
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();
            AdDirectoryEntryDTO dto = mapEntryToDto(attrs, sr.getNameInNamespace());
            entries.add(dto);
        }

        ctx.close();
        return entries;
    }

    private AdDirectoryEntryDTO mapEntryToDto(Attributes attrs, String dn) {
        AdDirectoryEntryDTO dto = new AdDirectoryEntryDTO();
        dto.setDistinguishedName(dn);
        dto.setCn(getAttributeValue(attrs, "cn"));
        dto.setName(getAttributeValue(attrs, "name"));
        dto.setObjectClass(String.join(",", getAttributeValues(attrs, "objectClass")));
        dto.setObjectCategory(getAttributeValue(attrs, "objectCategory"));
        dto.setsAMAccountName(getAttributeValue(attrs, "sAMAccountName"));
        dto.setUserPrincipalName(getAttributeValue(attrs, "userPrincipalName"));
        dto.setGivenName(getAttributeValue(attrs, "givenName"));
        dto.setSn(getAttributeValue(attrs, "sn"));
        dto.setDisplayName(getAttributeValue(attrs, "displayName"));
        dto.setMail(getAttributeValue(attrs, "mail"));
        dto.setTitle(getAttributeValue(attrs, "title"));
        dto.setDepartment(getAttributeValue(attrs, "department"));
        dto.setCompany(getAttributeValue(attrs, "company"));
        dto.setOffice(getAttributeValue(attrs, "office"));
        dto.setTelephoneNumber(getAttributeValue(attrs, "telephoneNumber"));
        dto.setMobile(getAttributeValue(attrs, "mobile"));
        dto.setEmployeeID(getAttributeValue(attrs, "employeeID"));
        dto.setEmployeeNumber(getAttributeValue(attrs, "employeeNumber"));
        dto.setDescription(getAttributeValue(attrs, "description"));
        dto.setStreetAddress(getAttributeValue(attrs, "streetAddress"));
        dto.setPostalCode(getAttributeValue(attrs, "postalCode"));
        dto.setPhysicalDeliveryOfficeName(getAttributeValue(attrs, "physicalDeliveryOfficeName"));
        dto.setManager(getAttributeValue(attrs, "manager"));
        dto.setMemberOf(getAttributeValues(attrs, "memberOf"));
        dto.setDirectReports(getAttributeValues(attrs, "directReports"));
        dto.setUserAccountControl(getAttributeValue(attrs, "userAccountControl"));
        dto.setUserAccountStatus(isAccountActive(attrs) ? "ACTIVE" : "DISABLED");
        dto.setAccountEnabled(isAccountActive(attrs));
        dto.setWhenCreated(getAttributeValue(attrs, "whenCreated"));
        dto.setWhenChanged(getAttributeValue(attrs, "whenChanged"));
        dto.setLastLogon(getAttributeValue(attrs, "lastLogon"));
        dto.setLastLogoff(getAttributeValue(attrs, "lastLogoff"));
        dto.setAccountExpires(getAttributeValue(attrs, "accountExpires"));
        dto.setPrimaryGroupId(getAttributeValue(attrs, "primaryGroupID"));
        dto.setHomeDirectory(getAttributeValue(attrs, "homeDirectory"));
        dto.setHomeDrive(getAttributeValue(attrs, "homeDrive"));
        dto.setScriptPath(getAttributeValue(attrs, "scriptPath"));
        dto.setProfilePath(getAttributeValue(attrs, "profile"));
        dto.setLogonCount(getAttributeValue(attrs, "logonCount"));
        dto.setBadPasswordTime(getAttributeValue(attrs, "badPasswordTime"));
        dto.setBadPwdCount(getAttributeValue(attrs, "badPwdCount"));

        // Determine entry type
        String objectClasses = dto.getObjectClass().toLowerCase();
        if (objectClasses.contains("user")) {
            dto.setEntryType("USER");
        } else if (objectClasses.contains("group")) {
            dto.setEntryType("GROUP");
        } else if (objectClasses.contains("computer")) {
            dto.setEntryType("COMPUTER");
        } else if (objectClasses.contains("contact")) {
            dto.setEntryType("CONTACT");
        } else if (objectClasses.contains("organizationalunit")) {
            dto.setEntryType("OU");
        } else if (objectClasses.contains("domain")) {
            dto.setEntryType("DOMAIN");
        } else {
            dto.setEntryType("OTHER");
        }

        return dto;
    }

    /**
     * Returns only the users (matching the previous contract) — kept for backward compatibility
     * with AdUserSyncService and DashboardService.
     */
    public List<Map<String, String>> searchUsers(String customFilter) throws NamingException {
        DirContext ctx = connect();
        List<Map<String, String>> resultList = new ArrayList<>();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(ALL_ATTRIBUTES);

        String filter = (customFilter == null || customFilter.isBlank())
                ? "(&(objectClass=user)(objectCategory=person))"
                : customFilter;

        NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, controls);

        while (results.hasMore()) {
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();

            if (isAccountActive(attrs)) {
                String title = getAttributeValue(attrs, "title");
                String username = getAttributeValue(attrs, "sAMAccountName");
                List<String> groups = getAttributeValues(attrs, "memberOf");
                Role assignedRole = mapAdAttributesToRole(title, username, groups);

                Map<String, String> userRow = new LinkedHashMap<>();
                userRow.put("matricule", extractMatricule(attrs));
                userRow.put("nom", getAttributeValue(attrs, "givenName"));
                userRow.put("prenom", getAttributeValue(attrs, "sn"));
                userRow.put("email", getAttributeValue(attrs, "mail"));
                userRow.put("codeFonction", title);
                userRow.put("nomStructure", getAttributeValue(attrs, "department"));
                userRow.put("login", username);
                userRow.put("role", assignedRole.name());
                userRow.put("groups", String.join(";", groups));
                userRow.put("distinguishedName", sr.getNameInNamespace());

                resultList.add(userRow);
            }
        }
        ctx.close();
        return resultList;
    }

    public Map<String, Integer> countDepartments() throws NamingException {
        List<Map<String, String>> users = searchUsers("(&(objectClass=user)(objectCategory=person))");
        Map<String, Integer> deptCounts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Map<String, String> user : users) {
            String dept = user.get("nomStructure");
            if (dept != null && !dept.isBlank()) {
                deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
            }
        }
        return deptCounts;
    }

    public void generateStructuresSqlScript(String filePath) throws NamingException, IOException {
        Map<String, Integer> depts = countDepartments();
        StringBuilder sql = new StringBuilder();

        sql.append("-- Structures SYGEART générées depuis l'annuaire AD (attribut department)\n")
           .append("-- À exécuter sur evaluation_db avec l'utilisateur evaluation_app.\n\n");

        for (Map.Entry<String, Integer> entry : depts.entrySet()) {
            String nomSql = entry.getKey().replace("'", "''");
            sql.append(String.format(
                "INSERT INTO structure (nom, type, est_active)\n" +
                "SELECT '%s', 'DIRECTION', true\n" +
                "WHERE NOT EXISTS (SELECT 1 FROM structure WHERE LOWER(nom) = LOWER('%s')); -- %d agent(s)\n\n",
                nomSql, nomSql, entry.getValue()
            ));
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sql.toString());
        }
    }
}