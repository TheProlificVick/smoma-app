package smoma.controller.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"ordreDeMission", "mandatDeMission", "personnelList"})
    private EtapeMission etape;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
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

    /** Direction / structure that issues the mission order (chosen from the referential). */
    @Column(name = "direction_initiatrice")
    private String directionInitiatrice;

    /** Reference of the act justifying the mission, inherited from the mandate, e.g. "ART/DG/CSI/001". */
    @Column(name = "reference_justification")
    private String referenceJustification;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "scan_signed_path", length = 500)
    private String scanSignedPath;

    private String rapportScannePath;
    private boolean rapportSoumis;

    /**
     * Set by the assigned agent (or DRH/admin) checking this step off as done — independently of
     * dateFin, so an agent who finishes early isn't stuck "occupied" until the originally planned
     * end date. MissionCapacityService.assertNoOverlap skips a step marked this way when checking
     * whether the agent is free for a new assignment.
     */
    private boolean missionTerminee;

    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private StatutOrdre statut;

    @OneToMany(mappedBy = "ordreDeMission", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"ordreDeMission", "mandatDeMission"})
    private List<EtapeMission> etapes = new ArrayList<>();

    // ---- Verso fields (page 2 of the paper form) — filled in by DRH/Finance and the agent as the
    // mission actually unfolds (advance decompte at departure, expense note, receipt acknowledgment),
    // independently of whether the order itself is still a draft or already DG-signed.
    private String indemniteReduiteNombre;
    private String indemniteReduiteTaux;
    private String indemniteReduiteDecompte;
    private String indemnitePartielleNombre;
    private String indemnitePartielleTaux;
    private String indemnitePartielleDecompte;
    @Column(columnDefinition = "TEXT")
    private String indicationRequisitions;
    private String arreteSomme;
    private String payeSomme;
    private String payeeAvanceMontant;
    private String payeeAvanceLieu;
    private LocalDate payeeAvanceDate;
    private String imputationBudgetaire;
    private String acquitDepartRecu;
    private String acquitDepartCni;
    private String acquitDepartLieu;
    private LocalDate acquitDepartDate;
    private String acquitSoldeRecu;

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

    public boolean isMissionTerminee() {
        return missionTerminee;
    }

    public void setMissionTerminee(boolean missionTerminee) {
        this.missionTerminee = missionTerminee;
    }

    public LocalDate getDateFinReelle() {
        return dateFinReelle;
    }

    public void setDateFinReelle(LocalDate dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
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

    public String getDirectionInitiatrice() {
        return directionInitiatrice;
    }

    public void setDirectionInitiatrice(String directionInitiatrice) {
        this.directionInitiatrice = directionInitiatrice;
    }

    public String getReferenceJustification() {
        return referenceJustification;
    }

    public void setReferenceJustification(String referenceJustification) {
        this.referenceJustification = referenceJustification;
    }

    public boolean isModifiable() {
        return statut == StatutOrdre.BROUILLON_MODIFIABLE;
    }

    public String getIndemniteReduiteNombre() { return indemniteReduiteNombre; }
    public void setIndemniteReduiteNombre(String v) { this.indemniteReduiteNombre = v; }

    public String getIndemniteReduiteTaux() { return indemniteReduiteTaux; }
    public void setIndemniteReduiteTaux(String v) { this.indemniteReduiteTaux = v; }

    public String getIndemniteReduiteDecompte() { return indemniteReduiteDecompte; }
    public void setIndemniteReduiteDecompte(String v) { this.indemniteReduiteDecompte = v; }

    public String getIndemnitePartielleNombre() { return indemnitePartielleNombre; }
    public void setIndemnitePartielleNombre(String v) { this.indemnitePartielleNombre = v; }

    public String getIndemnitePartielleTaux() { return indemnitePartielleTaux; }
    public void setIndemnitePartielleTaux(String v) { this.indemnitePartielleTaux = v; }

    public String getIndemnitePartielleDecompte() { return indemnitePartielleDecompte; }
    public void setIndemnitePartielleDecompte(String v) { this.indemnitePartielleDecompte = v; }

    public String getIndicationRequisitions() { return indicationRequisitions; }
    public void setIndicationRequisitions(String v) { this.indicationRequisitions = v; }

    public String getArreteSomme() { return arreteSomme; }
    public void setArreteSomme(String v) { this.arreteSomme = v; }

    public String getPayeSomme() { return payeSomme; }
    public void setPayeSomme(String v) { this.payeSomme = v; }

    public String getPayeeAvanceMontant() { return payeeAvanceMontant; }
    public void setPayeeAvanceMontant(String v) { this.payeeAvanceMontant = v; }

    public String getPayeeAvanceLieu() { return payeeAvanceLieu; }
    public void setPayeeAvanceLieu(String v) { this.payeeAvanceLieu = v; }

    public LocalDate getPayeeAvanceDate() { return payeeAvanceDate; }
    public void setPayeeAvanceDate(LocalDate v) { this.payeeAvanceDate = v; }

    public String getImputationBudgetaire() { return imputationBudgetaire; }
    public void setImputationBudgetaire(String v) { this.imputationBudgetaire = v; }

    public String getAcquitDepartRecu() { return acquitDepartRecu; }
    public void setAcquitDepartRecu(String v) { this.acquitDepartRecu = v; }

    public String getAcquitDepartCni() { return acquitDepartCni; }
    public void setAcquitDepartCni(String v) { this.acquitDepartCni = v; }

    public String getAcquitDepartLieu() { return acquitDepartLieu; }
    public void setAcquitDepartLieu(String v) { this.acquitDepartLieu = v; }

    public LocalDate getAcquitDepartDate() { return acquitDepartDate; }
    public void setAcquitDepartDate(LocalDate v) { this.acquitDepartDate = v; }

    public String getAcquitSoldeRecu() { return acquitSoldeRecu; }
    public void setAcquitSoldeRecu(String v) { this.acquitSoldeRecu = v; }

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
