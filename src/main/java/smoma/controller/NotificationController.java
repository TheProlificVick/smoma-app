package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Notification;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.NotificationService;
import smoma.controller.model.User;

import java.util.List;
import java.util.Map;

/**
 * A notification recipient is always "the caller" — there is no legitimate reason for one
 * account to read or clear another's notifications, so identity is resolved server-side from
 * the authenticated X-User-Email header (see AccessPolicy) instead of trusting whatever
 * matricule/username a caller puts in the query string.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AccessPolicy accessPolicy;

    public NotificationController(NotificationService notificationService, AccessPolicy accessPolicy) {
        this.notificationService = notificationService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = accessPolicy.resolve(requestEmail);
        if (actor == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(notificationService.forRecipient(actor.getMatricule(), actor.getUsername()));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = accessPolicy.resolve(requestEmail);
        if (actor == null) return ResponseEntity.ok(0L);
        return ResponseEntity.ok(notificationService.unreadCount(actor.getMatricule(), actor.getUsername()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id,
                                        @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = accessPolicy.resolve(requestEmail);
        if (actor == null) return ResponseEntity.status(403).body(Map.of("error", "Identité non reconnue."));
        boolean owned = notificationService.markRead(id, actor.getMatricule(), actor.getUsername());
        if (!owned) return ResponseEntity.status(403).body(Map.of("error", "Cette notification ne vous appartient pas."));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(
            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = accessPolicy.resolve(requestEmail);
        if (actor != null) notificationService.markAllRead(actor.getMatricule(), actor.getUsername());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
