package smoma.controller.model.Service;

import smoma.controller.model.AuditLog;
import smoma.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Ensures log commits even if business transaction rolls back
    public void logTransaction(Long requestId, String role, String action, String details) {
        // Instantiate using the default constructor
        AuditLog log = new AuditLog();
        
        // Map data elements using the established entity setters
        log.setMissionRequestId(requestId);
        log.setUserRole(role);
        log.setAction(action);
        log.setDescription(details);
        
        // Ensure absolute compliance tracing by capturing the exact moment of execution
        log.setTimestamp(LocalDateTime.now()); 
        
        auditLogRepository.save(log); // Permanent write-only ledger entry 
    }
}