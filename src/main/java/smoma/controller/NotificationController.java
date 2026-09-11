package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Notification;
import smoma.controller.model.Service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam(required = false) String matricule,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok(notificationService.forRecipient(matricule, username));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam(required = false) String matricule,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok(notificationService.unreadCount(matricule, username));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(
            @RequestParam(required = false) String matricule,
            @RequestParam(required = false) String username) {
        notificationService.markAllRead(matricule, username);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
