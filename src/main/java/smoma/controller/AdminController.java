package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.User;
import smoma.repository.UserRepository;
import smoma.controller.model.Service.AdUserSyncService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdUserSyncService adUserSyncService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/sync-ad")
    public ResponseEntity<?> syncActiveDirectory() {
        try {
            int count = adUserSyncService.syncUsersFromActiveDirectory();
            return ResponseEntity.ok(Map.of("message", "Active Directory sync finished", "syncedCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
