package smoma.controller.model;

import jakarta.persistence.*;
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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "objet_general", columnDefinition = "TEXT")
    private String objetGeneral;

    @Column(name = "objectifs_specifiques", columnDefinition = "TEXT")
    private String objectifsSpecifiques;

    @Column(name = "motif_reglementaire")
    private String motifReglementaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mission")
    private TypeMission typeMission;

    @ElementCollection
    @CollectionTable(name = "mandat_transport_modes", joinColumns = @JoinColumn(name = "mandat_id"))
    @Column(name = "transport_mode")
    private List<String> transportModes = new ArrayList<>();

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @Column(name = "date_validation")
    private LocalDate dateValidation;

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private StatutMandat statut;

    @Column(name = "sans_frais")
    private boolean sansFrais;

    @Column(name = "scan_signed_path", length = 500)
    private String scanSignedPath;

    @Column(name = "force_majeure")
    private boolean forceMajeure;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    @ManyToMany
    @JoinTable(
        name = "mandat_personnel",
        joinColumns = @JoinColumn(name = "mandat_id"),
        inverseJoinColumns = @JoinColumn(name = "personnel_id")
    )
    private List<Personnel> personnelList = new ArrayList<>();

    @OneToMany(mappedBy = "mandatDeMission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EtapeMission> etapes = new ArrayList<>();

    @OneToMany(mappedBy = "mandatDeMission")
    private List<OrdreDeMission> ordresDeMission = new ArrayList<>();

    public MandatDeMission() {
        this.dateCreation = LocalDate.now();
        this.statut = StatutMandat.EN_ATTENTE_SIGNATURE;
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

    public String getObjetGeneral() {
        return objetGeneral;
    }

    public void setObjetGeneral(String objetGeneral) {
        this.objetGeneral = objetGeneral;
    }

    public String getObjectifsSpecifiques() {
        return objectifsSpecifiques;
    }

    public void setObjectifsSpecifiques(String objectifsSpecifiques) {
        this.objectifsSpecifiques = objectifsSpecifiques;
    }

    public String getMotifReglementaire() {
        return motifReglementaire;
    }

    public void setMotifReglementaire(String motifReglementaire) {
        this.motifReglementaire = motifReglementaire;
    }

    public TypeMission getTypeMission() {
        return typeMission;
    }

    public void setTypeMission(TypeMission typeMission) {
        this.typeMission = typeMission;
    }

    public List<String> getTransportModes() {
        return transportModes;
    }

    public void setTransportModes(List<String> transportModes) {
        this.transportModes = transportModes;
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

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public LocalDate getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDate dateCloture) {
        this.dateCloture = dateCloture;
    }

    public StatutMandat getStatut() {
        return statut;
    }

    public void setStatut(StatutMandat statut) {
        this.statut = statut;
    }

    public boolean isSansFrais() {
        return sansFrais;
    }

    public void setSansFrais(boolean sansFrais) {
        this.sansFrais = sansFrais;
    }

    public String getScanSignedPath() {
        return scanSignedPath;
    }

    public void setScanSignedPath(String scanSignedPath) {
        this.scanSignedPath = scanSignedPath;
    }

    public boolean isForceMajeure() {
        return forceMajeure;
    }

    public void setForceMajeure(boolean forceMajeure) {
        this.forceMajeure = forceMajeure;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
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

    public List<EtapeMission> getEtapes() {
        return etapes;
    }

    public void setEtapes(List<EtapeMission> etapes) {
        this.etapes = etapes;
    }

    public void addEtape(EtapeMission etape) {
        etapes.add(etape);
        etape.setMandatDeMission(this);
    }

    public void removeEtape(EtapeMission etape) {
        etapes.remove(etape);
        etape.setMandatDeMission(null);
    }

    public List<OrdreDeMission> getOrdresDeMission() {
        return ordresDeMission;
    }

    public void setOrdresDeMission(List<OrdreDeMission> ordresDeMission) {
        this.ordresDeMission = ordresDeMission;
    }

    public boolean isValide() {
        return scanSignedPath != null && !scanSignedPath.isBlank();
    }

    public enum StatutMandat {
        EN_ATTENTE_SIGNATURE,
        PREVU,
        EN_COURS,
        EXECUTE,
        EN_ATTENTE_DEPOT_RAPPORT,
        CLOTURE,
        ACTIF,
        EN_ATTENTE,
        ANNULE
    }

    public enum TypeMission {
        INTERNE,
        EXTERNE
    }

    @Override
    public String toString() {
        return referenceMandat != null ? referenceMandat : "Mandat #" + id;
    }
}
