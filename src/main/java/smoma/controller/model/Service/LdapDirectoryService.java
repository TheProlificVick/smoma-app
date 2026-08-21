package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.Service.Role;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class LdapDirectoryService {

    // LDAP Configuration derived from ART Active Directory
    private static final String LDAP_URL = "ldap://192.168.0.101:389";
    private static final String BASE_DN = "DC=art,DC=cm";
    private static final String ADMIN_USER = "lucien.mba@art.cm";
    private static final String ADMIN_PASS = "@Wdsi@2913@";

    private static final String[] ATTRIBUTES = {
        "sAMAccountName", "givenName", "sn", "displayName",
        "mail", "title", "department",
        "employeeID", "employeeNumber", "description", "info", "comment",
        "userAccountControl"
    };

    private DirContext connect() throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, LDAP_URL);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ADMIN_USER);
        env.put(Context.SECURITY_CREDENTIALS, ADMIN_PASS);
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

    /**
     * Dynamic role resolution based on Active Directory titles and identifiers
     */
    public Role mapAdAttributesToRole(String title, String username) {
        if (username != null && (username.equalsIgnoreCase("admin") || username.equalsIgnoreCase("lucien.mba"))) {
            return Role.ROLE_ADMIN;
        }

        if (title != null) {
            String lowerTitle = title.toLowerCase();
            if (lowerTitle.contains("directeur") || lowerTitle.contains("chef de département")) {
                return Role.ROLE_DIRECTEUR;
            } else if (lowerTitle.contains("chef de service") || lowerTitle.contains("responsable")) {
                return Role.ROLE_CHEF_SERVICE;
            } else if (lowerTitle.contains("administrateur") || lowerTitle.contains("sysadmin")) {
                return Role.ROLE_ADMIN;
            }
        }

        return Role.ROLE_AGENT;
    }

    public List<Map<String, String>> searchUsers(String customFilter) throws NamingException {
        DirContext ctx = connect();
        List<Map<String, String>> resultList = new ArrayList<>();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(ATTRIBUTES);

        String filter = (customFilter == null || customFilter.isBlank())
                ? "(&(objectClass=user)(objectCategory=person))"
                : customFilter;

        NamingEnumeration<SearchResult> results = ctx.search(BASE_DN, filter, controls);

        while (results.hasMore()) {
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();

            if (isAccountActive(attrs)) {
                String title = getAttributeValue(attrs, "title");
                String username = getAttributeValue(attrs, "sAMAccountName");
                Role assignedRole = mapAdAttributesToRole(title, username);

                Map<String, String> userRow = new HashMap<>();
                userRow.put("matricule", extractMatricule(attrs));
                userRow.put("nom", getAttributeValue(attrs, "givenName"));
                userRow.put("prenom", getAttributeValue(attrs, "sn"));
                userRow.put("email", getAttributeValue(attrs, "mail"));
                userRow.put("codeFonction", title);
                userRow.put("nomStructure", getAttributeValue(attrs, "department"));
                userRow.put("login", username);
                userRow.put("role", assignedRole.name());

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