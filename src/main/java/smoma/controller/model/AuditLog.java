package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String username;
    private String details;
    private LocalDateTime timestamp;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String action, String username, String details) {
        this();
        this.action = action;
        this.username = username;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getUsername() { return username; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
}