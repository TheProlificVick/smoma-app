package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.AuditLog;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findAllByOrderByTimestampDesc();
}