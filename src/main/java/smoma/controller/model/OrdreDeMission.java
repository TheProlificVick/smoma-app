package smoma.controller.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordres_de_mission")
public class OrdreDeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceOrdre;

    @ManyToOne
    @JoinColumn(name = "mandat_de_mission_id")
    private MandatDeMission mandatDeMission;

    @ManyToOne
    @JoinColumn(name = "etape_id")
    private EtapeMission etape;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    @Enumerated(EnumType.STRING)
    private TypeMission typeMission;

    @Column(columnDefinition = "TEXT")
    private String objectifsSpecifiques;

    private boolean avecFrais;
    private boolean sansFrais;

    private BigDecimal montantFrais;
    private BigDecimal montantIndemnite;
    private BigDecimal montantAvance;
    private BigDecimal montantSolde;

    private String lieuDepart;
    private String lieuDestination;
    private String moyenTransport;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "scan_signed_path", length = 500)
    private String scanSignedPath;

    private String rapportScannePath;
    private boolean rapportSoumis;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private StatutOrdre statut;

    @OneToMany(mappedBy = "ordreDeMission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EtapeMission> etapes = new ArrayList<>();

    public OrdreDeMission() {
        this.dateEmission = LocalDate.now();
        this.statut = StatutOrdre.BROUILLON_MODIFIABLE;
    }

    public OrdreDeMission(String referenceOrdre, MandatDeMission mandatDeMission, Personnel personnel,
                          TypeMission typeMission, String objectifsSpecifiques, boolean avecFrais,
                          BigDecimal montantFrais, LocalDate dateDebut, LocalDate dateFin) {
        this();
        this.referenceOrdre = referenceOrdre;
        this.mandatDeMission = mandatDeMission;
        this.personnel = personnel;
        this.typeMission = typeMission;
        this.objectifsSpecifiques = objectifsSpecifiques;
        this.avecFrais = avecFrais;
        this.montantFrais = montantFrais;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public void addEtape(EtapeMission etape) {
        etapes.add(etape);
        etape.setOrdreDeMission(this);
    }

    public void removeEtape(EtapeMission etape) {
        etapes.remove(etape);
        etape.setOrdreDeMission(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceOrdre() {
        return referenceOrdre;
    }

    public void setReferenceOrdre(String referenceOrdre) {
        this.referenceOrdre = referenceOrdre;
    }

    public MandatDeMission getMandatDeMission() {
        return mandatDeMission;
    }

    public void setMandatDeMission(MandatDeMission mandatDeMission) {
        this.mandatDeMission = mandatDeMission;
    }

    public EtapeMission getEtape() {
        return etape;
    }

    public void setEtape(EtapeMission etape) {
        this.etape = etape;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public TypeMission getTypeMission() {
        return typeMission;
    }

    public void setTypeMission(TypeMission typeMission) {
        this.typeMission = typeMission;
    }

    public String getObjectifsSpecifiques() {
        return objectifsSpecifiques;
    }

    public void setObjectifsSpecifiques(String objectifsSpecifiques) {
        this.objectifsSpecifiques = objectifsSpecifiques;
    }

    public boolean isAvecFrais() {
        return avecFrais;
    }

    public void setAvecFrais(boolean avecFrais) {
        this.avecFrais = avecFrais;
    }

    public boolean isSansFrais() {
        return sansFrais;
    }

    public void setSansFrais(boolean sansFrais) {
        this.sansFrais = sansFrais;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public BigDecimal getMontantIndemnite() {
        return montantIndemnite;
    }

    public void setMontantIndemnite(BigDecimal montantIndemnite) {
        this.montantIndemnite = montantIndemnite;
    }

    public BigDecimal getMontantAvance() {
        return montantAvance;
    }

    public void setMontantAvance(BigDecimal montantAvance) {
        this.montantAvance = montantAvance;
    }

    public BigDecimal getMontantSolde() {
        return montantSolde;
    }

    public void setMontantSolde(BigDecimal montantSolde) {
        this.montantSolde = montantSolde;
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

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public String getScanSignedPath() {
        return scanSignedPath;
    }

    public void setScanSignedPath(String scanSignedPath) {
        this.scanSignedPath = scanSignedPath;
    }

    public String getRapportScannePath() {
        return rapportScannePath;
    }

    public void setRapportScannePath(String rapportScannePath) {
        this.rapportScannePath = rapportScannePath;
    }

    public boolean isRapportSoumis() {
        return rapportSoumis;
    }

    public void setRapportSoumis(boolean rapportSoumis) {
        this.rapportSoumis = rapportSoumis;
    }

    public StatutOrdre getStatut() {
        return statut;
    }

    public void setStatut(StatutOrdre statut) {
        this.statut = statut;
    }

    public List<EtapeMission> getEtapes() {
        return etapes;
    }

    public void setEtapes(List<EtapeMission> etapes) {
        this.etapes = etapes;
    }

    public boolean isValide() {
        return scanSignedPath != null && !scanSignedPath.isBlank();
    }

    public String getLieuDepart() {
        return lieuDepart;
    }

    public void setLieuDepart(String lieuDepart) {
        this.lieuDepart = lieuDepart;
    }

    public String getLieuDestination() {
        return lieuDestination;
    }

    public void setLieuDestination(String lieuDestination) {
        this.lieuDestination = lieuDestination;
    }

    public String getMoyenTransport() {
        return moyenTransport;
    }

    public void setMoyenTransport(String moyenTransport) {
        this.moyenTransport = moyenTransport;
    }

    public boolean isModifiable() {
        return statut == StatutOrdre.BROUILLON_MODIFIABLE;
    }

    public enum TypeMission {
        INTERNE,
        EXTERNE,
        SEMINAIRE,
        FORMATION,
        ETUDE,
        REPRESENTATION,
        MAINTENANCE,
        SUIVI,
        AUTRE
    }

    public enum StatutOrdre {
        BROUILLON_MODIFIABLE,
        SIGNE
    }

    @Override
    public String toString() {
        return referenceOrdre != null ? referenceOrdre : "Ordre de mission";
    }
}
