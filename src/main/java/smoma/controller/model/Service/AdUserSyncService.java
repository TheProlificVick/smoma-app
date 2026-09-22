package smoma.controller.model.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import smoma.controller.model.User;
import smoma.repository.UserRepository;

import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdUserSyncService {

    private final LdapDirectoryService ldapDirectoryService;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdUserSyncService(LdapDirectoryService ldapDirectoryService, UserRepository userRepository) {
        this.ldapDirectoryService = ldapDirectoryService;
        this.userRepository = userRepository;
    }

    /**
     * Synchronizes ALL active users from the Active Directory into the local database.
     * Role assignment is fortified using AD group memberships (memberOf) and job titles.
     */
    public int syncUsersFromActiveDirectory() throws NamingException {
        List<Map<String, String>> adUsers = ldapDirectoryService.searchUsers(null);
        int syncedCount = 0;

        for (Map<String, String> adUser : adUsers) {
            String username = adUser.get("login");
            if (username == null || username.isBlank()) {
                continue;
            }

            String matricule = adUser.get("matricule");

            Role assignedRole = Role.valueOf(adUser.getOrDefault("role", "ROLE_AGENT"));

            User user = userRepository.findByUsername(username).orElse(new User());
            user.setUsername(username);
            user.setNom(adUser.get("nom"));
            user.setPrenom(adUser.get("prenom"));
            user.setEmail(adUser.get("email"));
            user.setTitle(adUser.get("codeFonction"));
            user.setStructure(adUser.get("nomStructure"));
            // AD often has none of the matricule-like attributes (description/info/comment/
            // employeeID/employeeNumber — see LdapDirectoryService#extractMatricule), in which case
            // this is "", not absent. Setting it verbatim used to blank out the matricule on every
            // re-sync — including one already correctly backfilled — which then couldn't be matched
            // back to the Personnel record a mandate/mission-order notification is created against.
            // Keep whatever matricule the record already has rather than ever blanking it, and only
            // fall back to a deterministic placeholder for a genuinely new record with nothing from
            // AD either.
            if (matricule == null || matricule.isBlank()) {
                matricule = (user.getMatricule() != null && !user.getMatricule().isBlank())
                        ? user.getMatricule() : "LDAP-" + username;
            }
            if (matricule.length() > 64) matricule = matricule.substring(0, 64);
            user.setMatricule(matricule);
            user.setRole(assignedRole);
            user.setActive(true);

            // AD-managed accounts always sign in via a live LDAP bind; the local password column
            // is never meant to be used for them, so a new user gets an unguessable random hash
            // rather than a predictable "<matricule>@2026!" default.
            if (user.getId() == null) {
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            }

            userRepository.save(user);
            syncedCount++;
        }

        return syncedCount;
    }
}