package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.AuditService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditService auditLogService;

    @GetMapping
    public ResponseEntity<?> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}