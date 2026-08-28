package smoma.controller.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mandats_de_mission")
public class MandatDeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceMandat;
    private String titre;
    private String description;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutMandat statut;

    @OneToMany(mappedBy = "mandatDeMission")
    private List<OrdreDeMission> ordresDeMission = new ArrayList<>();

    public MandatDeMission() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceMandat() {
        return referenceMandat;
    }

    public void setReferenceMandat(String referenceMandat) {
        this.referenceMandat = referenceMandat;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public StatutMandat getStatut() {
        return statut;
    }

    public void setStatut(StatutMandat statut) {
        this.statut = statut;
    }

    public List<OrdreDeMission> getOrdresDeMission() {
        return ordresDeMission;
    }

    public void setOrdresDeMission(List<OrdreDeMission> ordresDeMission) {
        this.ordresDeMission = ordresDeMission;
    }

    public enum StatutMandat {
        ACTIF,
        EN_ATTENTE,
        CLOTURE,
        ANNULE
    }
}
