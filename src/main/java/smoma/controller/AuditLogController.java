package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.AuditLog;
import smoma.repository.AuditLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        // Standard Spring Data JPA find method
        return ResponseEntity.ok(auditLogRepository.findAll());
    }
}