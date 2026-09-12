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