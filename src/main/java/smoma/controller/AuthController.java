package smoma.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS})
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody SimpleLoginRequest loginRequest) {
        System.out.println("Backend received login attempt for: " + loginRequest.getEmail());

        // Standard institutional validation tracking rule matching
        if (loginRequest.getEmail() != null && loginRequest.getEmail().endsWith("@art.cm") && "password123".equals(loginRequest.getPassword())) {
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "fullName", "ART Authorized Agent"
            ));
        }
        
        return ResponseEntity.status(401).body(Map.of("message", "Unauthorized credentials supplied"));
    }
}

// Keeping the request payload template right here avoids missing symbol errors completely!
class SimpleLoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
