package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Optional;

@Service
public class LdapAuthenticationService {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @Value("${ldap.url}")
    private String ldapUrl;

    @Value("${ldap.base-dn}")
    private String baseDn;

    @Value("${ldap.admin.user}")
    private String adminUser;

    @Value("${ldap.admin.password}")
    private String adminPassword;

    /**
     * Authenticates a user against ART Active Directory and synchronizes 
     * LDAP attributes (Name, Email, Matricule, Department) into MySQL.
     */
    public Optional<StaffMember> authenticateAndSyncUser(String identifier, String rawPassword) {
        if (identifier == null || rawPassword == null || identifier.isBlank() || rawPassword.isBlank()) {
            return Optional.empty();
        }

        String cleanIdentifier = identifier.trim().toLowerCase();
        String cleanSamName = extractSamAccountName(cleanIdentifier);
        String fullUserPrincipal = cleanIdentifier.contains("@") ? cleanIdentifier : cleanSamName + "@art.cm";

        // 1. Authenticate user credentials directly against Active Directory
        boolean isAuthenticated = adLdapBind(fullUserPrincipal, rawPassword);
        if (!isAuthenticated) {
            return Optional.empty();
        }

        // 2. Query AD as Admin to retrieve user attributes
        Attributes adAttrs = fetchUserAttributesFromAd(cleanSamName);

        // Check if account is disabled in AD (userAccountControl bitwise 2 check)
        if (isAccountDisabled(adAttrs)) {
            System.err.println("Authentication rejected: AD Account is disabled for " + cleanSamName);
            return Optional.empty();
        }

        // 3. Extract profile details from AD attributes
        String email = getAttrVal(adAttrs, "mail");
        if (email.isEmpty()) {
            email = fullUserPrincipal;
        }

        String firstName = getAttrVal(adAttrs, "givenName");
        String lastName = getAttrVal(adAttrs, "sn");
        String displayName = getAttrVal(adAttrs, "displayName");
        String department = getAttrVal(adAttrs, "department");
        String matricule = extractMatricule(adAttrs);

        // Fallback name formatting if givenName/sn are missing in AD
        if (firstName.isEmpty() && !displayName.isEmpty()) {
            firstName = displayName;
        } else if (firstName.isEmpty()) {
            firstName = capitalizeWords(cleanSamName.replace(".", " "));
        }

        if (lastName.isEmpty()) {
            lastName = "ART";
        }

        // 4. Fetch existing staff member or create new JIT record in MySQL
        String finalEmail = email;
        String finalFirstName = firstName;
        String finalLastName = lastName;

        StaffMember staff = staffMemberRepository.findBySamAccountName(cleanSamName)
                .orElseGet(() -> staffMemberRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    StaffMember newStaff = new StaffMember();
                    newStaff.setSamAccountName(cleanSamName);
                    newStaff.setEmail(finalEmail);
                    newStaff.setFirstName(finalFirstName);
                    newStaff.setLastName(finalLastName);
                    
                    try {
                        newStaff.setRoleScope("STAFF");
                    } catch (Exception ignored) {}

                    return newStaff;
                }));

        // 5. Update user attributes with fresh AD data
        staff.setFirstName(finalFirstName);
        staff.setLastName(finalLastName);
        staff.setEmail(finalEmail);
        staff.setIsAdManaged(true);
        staff.setLastAdSyncAt(LocalDateTime.now());

        // Update matricule if your StaffMember model supports it
        try {
            if (!matricule.isEmpty()) {
                staff.getClass().getMethod("setMatricule", String.class).invoke(staff, matricule);
            }
        } catch (Exception ignored) {}

        // Update department if your StaffMember model supports it
        try {
            if (!department.isEmpty()) {
                staff.getClass().getMethod("setDepartment", String.class).invoke(staff, department);
            }
        } catch (Exception ignored) {}

        // 6. Persist synced user into MySQL database
        try {
            return Optional.of(staffMemberRepository.save(staff));
        } catch (Exception e) {
            System.err.println("Error saving synced LDAP staff to database: " + e.getMessage());
            return Optional.of(staff);
        }
    }

    /**
     * Attempts a BIND authentication against Active Directory using user credentials.
     */
    private boolean adLdapBind(String userPrincipal, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext ctx = new InitialDirContext(env);
            ctx.close();
            return true;
        } catch (Exception e) {
            System.err.println("AD Authentication failed for " + userPrincipal + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Searches Active Directory using administrative credentials to extract all attributes.
     */
    private Attributes fetchUserAttributesFromAd(String samAccountName) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, adminUser);
        env.put(Context.SECURITY_CREDENTIALS, adminPassword);

        try {
            DirContext ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{
                "sAMAccountName", "givenName", "sn", "displayName", "mail",
                "title", "department", "employeeID", "employeeNumber",
                "description", "info", "comment", "userAccountControl"
            });

            String filter = "(&(objectClass=user)(sAMAccountName=" + samAccountName + "))";
            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, controls);

            if (results.hasMore()) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();
                ctx.close();
                return attrs;
            }
            ctx.close();
        } catch (Exception e) {
            System.err.println("Failed to fetch AD attributes for " + samAccountName + ": " + e.getMessage());
        }
        return new BasicAttributes();
    }

    /**
     * Extracts Matricule following priority: description > info > comment > employeeID > employeeNumber
     */
    private String extractMatricule(Attributes attrs) {
        String[] fields = {"description", "info", "comment", "employeeID", "employeeNumber"};
        for (String field : fields) {
            String val = getAttrVal(attrs, field);
            if (!val.isEmpty()) {
                return val;
            }
        }
        return "";
    }

    /**
     * Checks if the userAccountControl attribute has bit 2 set (ACCOUNTDISABLE = 0x0002)
     */
    private boolean isAccountDisabled(Attributes attrs) {
        String uacVal = getAttrVal(attrs, "userAccountControl");
        if (!uacVal.isEmpty()) {
            try {
                int uac = Integer.parseInt(uacVal);
                return (uac & 2) != 0;
            } catch (NumberFormatException ignored) {}
        }
        return false;
    }

    private String getAttrVal(Attributes attrs, String attrName) {
        try {
            Attribute attr = attrs.get(attrName);
            if (attr != null && attr.get() != null) {
                String val = attr.get().toString().trim();
                return ("[]".equals(val) || "None".equals(val)) ? "" : val;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String extractSamAccountName(String identifier) {
        if (identifier.contains("@")) {
            return identifier.substring(0, identifier.indexOf("@"));
        }
        return identifier;
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}