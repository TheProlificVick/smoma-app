package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Matricule of the staff member the notification is addressed to. */
    @Column(name = "recipient_matricule", length = 64)
    private String recipientMatricule;

    /** Username / login of the recipient, when known (AD account). */
    @Column(name = "recipient_username")
    private String recipientUsername;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** MISSION_ASSIGNED, REPORT_SUBMITTED, REPORT_VALIDATED, ADVANCE_VALIDATED ... */
    private String type;

    /** Deep link (page or PDF endpoint) the recipient can open. */
    @Column(length = 500)
    private String lien;

    private boolean lu;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    public Notification() {
        this.dateCreation = LocalDateTime.now();
        this.lu = false;
    }

    public Notification(String recipientMatricule, String recipientUsername, String titre,
                        String message, String type, String lien) {
        this();
        this.recipientMatricule = recipientMatricule;
        this.recipientUsername = recipientUsername;
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.lien = lien;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientMatricule() { return recipientMatricule; }
    public void setRecipientMatricule(String recipientMatricule) { this.recipientMatricule = recipientMatricule; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
