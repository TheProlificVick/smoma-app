
package smoma.controller.model.Service;

import smoma.controller.model.AuditLog;
import smoma.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Ensures log commits even if business transaction rolls back
    public void logTransaction(Long requestId, String role, String action, String details) {
        AuditLog log = new AuditLog(requestId, role, action, details);
        ((org.springframework.data.repository.CrudRepository<AuditLog, Long>) auditLogRepository).save(log); // Permanent write-only ledger entry
    }
}