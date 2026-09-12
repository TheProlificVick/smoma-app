package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.JwtService;
import smoma.controller.model.Service.Role;
import smoma.controller.model.User;
import smoma.repository.UserRepository;
import smoma.controller.model.Service.LdapDirectoryService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final LdapDirectoryService ldapDirectoryService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, LdapDirectoryService ldapDirectoryService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.ldapDirectoryService = ldapDirectoryService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String identity = credentials.get("email") != null ? credentials.get("email") : credentials.get("username");
        if (identity == null && credentials.get("matricule") != null) {
            identity = credentials.get("matricule");
        }
        String password = credentials.get("password");

        if (identity == null || identity.isBlank() || password == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Email/Username/Matricule and password are required."));
        }

        identity = identity.trim();

        // 1. Direct Active Directory LDAP Authentication
        try {
            Map<String, Object> adUser = ldapDirectoryService.authenticateAndResolveUser(identity, password);
            if (adUser != null) {
                String sam = (String) adUser.get("username");
                String fullName = (String) adUser.get("fullName");
                String email = (String) adUser.get("email");
                String matricule = (String) adUser.get("matricule");
                String title = (String) adUser.get("title");
                String dept = (String) adUser.get("department");
                Role role = (Role) adUser.getOrDefault("role", Role.ROLE_AGENT);

                // Auto-sync or update local user record
                User user = userRepository.findByIdentity(sam)
                        .or(() -> (email != null && !email.isBlank()) ? userRepository.findByIdentity(email) : Optional.empty())
                        .or(() -> (matricule != null && !matricule.isBlank()) ? userRepository.findByIdentity(matricule) : Optional.empty())
                        .orElseGet(User::new);

                user.setUsername(sam != null && !sam.isBlank() ? sam : identity);
                if (user.getEmail() == null || user.getEmail().isBlank()) user.setEmail(email);
                if (user.getNom() == null || user.getNom().isBlank()) user.setNom((String) adUser.get("nom"));
                if (user.getPrenom() == null || user.getPrenom().isBlank()) user.setPrenom((String) adUser.get("prenom"));
                if (matricule != null && !matricule.isBlank()) user.setMatricule(matricule);
                if (title != null && !title.isBlank()) user.setTitle(title);
                if (dept != null && !dept.isBlank()) user.setStructure(dept);
                user.setRole(role);
                user.setActive(true);
                userRepository.save(user);

                return buildSuccessResponse(fullName, role.name(), user.getUsername(), matricule, user.getTitle());
            }
        } catch (Exception ignored) {
            // Proceed to local database verification if LDAP service encounters an error
        }

        // 2. Local Database Check — BCrypt for real accounts (bootstrap admin, AD-synced, admin-
        //    created), plain-text only reachable for legacy/dev-seeded demo accounts.
        Optional<User> localUserOpt = userRepository.findByIdentity(identity);
        if (localUserOpt.isPresent()) {
            User user = localUserOpt.get();
            boolean pwdMatch = false;

            if (user.getPassword() != null) {
                try {
                    pwdMatch = passwordEncoder.matches(password, user.getPassword());
                } catch (Exception ignored) {}
                if (!pwdMatch && password.equals(user.getPassword())) {
                    pwdMatch = true;
                }
            }

            if (pwdMatch) {
                String roleName = user.getRole() != null ? user.getRole().name() : Role.ROLE_AGENT.name();
                String displayName = (user.getNom() != null ? user.getNom() : "") +
                                     (user.getPrenom() != null ? " " + user.getPrenom() : "");
                if (displayName.isBlank()) displayName = user.getUsername();
                return buildSuccessResponse(displayName.trim(), roleName, user.getUsername(), user.getMatricule(), user.getTitle());
            }
        }

        return ResponseEntity.status(401).body(Map.of("message", "Identifiants professionnels incorrects ou accès refusé. / Incorrect credentials or access denied."));
    }

    /** Lets an already-authenticated user change their own password (JwtAuthFilter guarantees the caller really is `identity`). */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String identity = body.get("identity");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (identity == null || oldPassword == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "identity, oldPassword et newPassword sont requis."));
        }
        if (newPassword.length() < 8) {
            return ResponseEntity.status(400).body(Map.of("error", "Le nouveau mot de passe doit compter au moins 8 caractères."));
        }

        User user = userRepository.findByIdentity(identity.trim()).orElse(null);
        if (user == null || user.getPassword() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Compte introuvable."));
        }

        boolean oldMatches;
        try {
            oldMatches = passwordEncoder.matches(oldPassword, user.getPassword());
        } catch (Exception e) {
            oldMatches = oldPassword.equals(user.getPassword());
        }
        if (!oldMatches) {
            return ResponseEntity.status(401).body(Map.of("error", "Mot de passe actuel incorrect."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "password_changed"));
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(String fullName, String role, String username, String matricule, String designation) {
        String token = jwtService.generateToken(username, Map.of(
                "role", role != null ? role : "",
                "matricule", matricule != null ? matricule : "",
                "designation", designation != null ? designation : ""
        ));
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", role);
        response.put("username", username);
        response.put("fullName", fullName);
        response.put("matricule", matricule != null ? matricule : "");
        response.put("designation", designation != null ? designation : "");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return userRepository.findByUsername(principal.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}