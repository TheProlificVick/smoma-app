package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.LdapAuthenticationService;
import smoma.controller.model.StaffMember;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS})
public class AuthController {

    @Autowired
    private LdapAuthenticationService ldapAuthenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody SimpleLoginRequest loginRequest) {
        System.out.println("Backend received login attempt for: " + (loginRequest != null ? loginRequest.getEmail() : "NULL"));

        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Email and password are required"));
        }

        // 1. Authenticate against LDAP and synchronize user JIT in MySQL
        Optional<StaffMember> authenticatedStaff = ldapAuthenticationService.authenticateAndSyncUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        // 2. Return success response if valid
        if (authenticatedStaff.isPresent()) {
            StaffMember staff = authenticatedStaff.get();
            
            // Build safe display name
            String displayName;
            if (staff.getFirstName() != null && !staff.getFirstName().isBlank()) {
                displayName = staff.getFirstName() + (staff.getLastName() != null ? " " + staff.getLastName() : "");
            } else if (staff.getSamAccountName() != null) {
                displayName = staff.getSamAccountName();
            } else {
                displayName = "ART Agent";
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "SUCCESS");
            responseBody.put("fullName", displayName);
            responseBody.put("email", staff.getEmail() != null ? staff.getEmail() : loginRequest.getEmail());
            responseBody.put("role", staff.getRoleScope() != null ? staff.getRoleScope() : "STAFF");
            responseBody.put("samAccountName", staff.getSamAccountName() != null ? staff.getSamAccountName() : "user");

            System.out.println("===> LOGIN SUCCESSFUL FOR: " + loginRequest.getEmail() + " (" + displayName + ")");
            return ResponseEntity.ok(responseBody);
        }

        System.out.println("===> LOGIN FAILED FOR: " + loginRequest.getEmail());
        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized credentials supplied"));
    }
}

class SimpleLoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}