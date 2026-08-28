package smoma.controller.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "etapes_mission")
public class EtapeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordre_de_mission_id")
    private OrdreDeMission ordreDeMission;

    private String lieu;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;

    @Enumerated(EnumType.STRING)
    private TypeEtape typeEtape;

    private String commentaire;

    public EtapeMission() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrdreDeMission getOrdreDeMission() {
        return ordreDeMission;
    }

    public void setOrdreDeMission(OrdreDeMission ordreDeMission) {
        this.ordreDeMission = ordreDeMission;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDate dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) {
        this.dateDepart = dateDepart;
    }

    public TypeEtape getTypeEtape() {
        return typeEtape;
    }

    public void setTypeEtape(TypeEtape typeEtape) {
        this.typeEtape = typeEtape;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public enum TypeEtape {
        DEPART,
        ARRIVEE,
        TRANSIT,
        RETOUR
    }
}
