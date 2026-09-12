package smoma.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload for one step (étape) of a mission mandate, including which team members are
 * specifically assigned to that leg of the mission. When {@code personnelIds} is empty, the
 * step is generated for the whole mandate team (spec 4.4: "un agent peut être concerné par
 * plusieurs étapes d'un même mandat").
 */
public class EtapeStepRequest {
    private String lieu;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String transportMode;
    private String commentaire;
    private List<Long> personnelIds;

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public List<Long> getPersonnelIds() { return personnelIds; }
    public void setPersonnelIds(List<Long> personnelIds) { this.personnelIds = personnelIds; }
}
