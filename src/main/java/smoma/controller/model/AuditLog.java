package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long missionRequestId;

    @Column(nullable = false)
    private String actorRole; 

    @Column(nullable = false)
    private String actionExecuted;

    @Column(nullable = false)
    private String operationalDetails;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    
    public AuditLog(Long missionRequestId, String actorRole, String actionExecuted, String operationalDetails) {
        this.missionRequestId = missionRequestId;
        this.actorRole = actorRole;
        this.actionExecuted = actionExecuted;
        this.operationalDetails = operationalDetails;
    }
}
