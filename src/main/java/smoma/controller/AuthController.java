package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.Role;
import smoma.controller.model.User;
import smoma.repository.UserRepository;
import smoma.controller.model.Service.LdapDirectoryService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LdapDirectoryService ldapDirectoryService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String identity = credentials.get("email") != null ? credentials.get("email") : credentials.get("username");
        String password = credentials.get("password");

        if (identity == null || password == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Email/Username and password required"));
        }

        // 1. Local Database Check
        return userRepository.findByUsername(identity)
                .filter(u -> u.getPassword().equals(password))
                .<ResponseEntity<?>>map(user -> buildSuccessResponse(
                        user.getUsername(),
                        user.getRole() != null ? user.getRole().name() : Role.ROLE_AGENT.name(),
                        user.getUsername()
                ))
                .orElseGet(() -> {
                    // 2. Active Directory Administrator Bootstrap Fallback
                    if ("admin@art.cm".equals(identity) && "admin123".equals(password)) {
                        Role role = ldapDirectoryService.mapAdAttributesToRole("Administrateur", "admin");
                        return buildSuccessResponse("Administrator ART", role.name(), "admin@art.cm");
                    }
                    return ResponseEntity.status(401).body(Map.of("message", "Identifiants professionnels incorrects ou accès refusé."));
                });
    }

    private ResponseEntity<Map<String, Object>> buildSuccessResponse(String fullName, String role, String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", "dummy-jwt-token-" + username);
        response.put("role", role);
        response.put("username", username);
        response.put("fullName", fullName);
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