package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etapes_mission")
public class EtapeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordre_de_mission_id")
    private OrdreDeMission ordreDeMission;

    @ManyToOne
    @JoinColumn(name = "mandat_de_mission_id")
    private MandatDeMission mandatDeMission;

    private String lieu;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    private LocalDate dateArrivee;
    private LocalDate dateDepart;

    @Enumerated(EnumType.STRING)
    private TypeEtape typeEtape;

    private String transportMode;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @ManyToMany
    @JoinTable(
        name = "etape_personnel",
        joinColumns = @JoinColumn(name = "etape_id"),
        inverseJoinColumns = @JoinColumn(name = "personnel_id")
    )
    private List<Personnel> personnelList = new ArrayList<>();

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

    public MandatDeMission getMandatDeMission() {
        return mandatDeMission;
    }

    public void setMandatDeMission(MandatDeMission mandatDeMission) {
        this.mandatDeMission = mandatDeMission;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
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

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public List<Personnel> getPersonnelList() {
        return personnelList;
    }

    public void setPersonnelList(List<Personnel> personnelList) {
        this.personnelList = personnelList;
    }

    public void addPersonnel(Personnel p) {
        if (!personnelList.contains(p)) {
            personnelList.add(p);
        }
    }

    public void removePersonnel(Personnel p) {
        personnelList.remove(p);
    }

    public boolean datesWithinMandatPeriod(LocalDate mandatDateDebut, LocalDate mandatDateFin) {
        if (dateDebut == null || mandatDateDebut == null || mandatDateFin == null) {
            return true;
        }
        return !dateDebut.isBefore(mandatDateDebut) && !dateFin.isAfter(mandatDateFin);
    }

    public enum TypeEtape {
        DEPART,
        ARRIVEE,
        TRANSIT,
        RETOUR
    }
}
