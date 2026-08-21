package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.AuditLog;
import smoma.repository.AuditLogRepository;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String username, String role, String action, String target, String details) {
        String combinedDetails = String.format("Role: %s | Target: %s | %s", role, target, details);
        AuditLog log = new AuditLog(action, username, combinedDetails);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}