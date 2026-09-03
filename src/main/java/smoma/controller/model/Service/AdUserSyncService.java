package smoma.controller.model.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import smoma.controller.model.User;
import smoma.repository.UserRepository;

import javax.naming.NamingException;
import java.util.List;
import java.util.Map;

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
            String rawDefaultPassword = (matricule != null && !matricule.isBlank()) 
                    ? matricule + "@2026!" 
                    : "Art@2026!";

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

            // Only set password if creating a new user
            if (user.getId() == null) {
                user.setPassword(passwordEncoder.encode(rawDefaultPassword));
            }

            userRepository.save(user);
            syncedCount++;
        }

        return syncedCount;
    }
}