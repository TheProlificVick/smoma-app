package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.AuditService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditService auditLogService;

    public AuditLogController(AuditService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<?> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}