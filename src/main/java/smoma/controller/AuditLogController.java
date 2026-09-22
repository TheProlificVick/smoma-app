package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.AuditService;
import smoma.controller.model.User;

import java.util.Map;

/** The audit trail records who did what, when, across the whole system — administrator only. */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditService auditLogService;
    private final AccessPolicy accessPolicy;

    public AuditLogController(AuditService auditLogService, AccessPolicy accessPolicy) {
        this.auditLogService = auditLogService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    public ResponseEntity<?> getAllLogs(@RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.isAdmin(actor)) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul l'administrateur système peut consulter le journal d'audit."));
        }
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}