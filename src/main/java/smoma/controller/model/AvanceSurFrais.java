package smoma.controller.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "avances_frais")
public class AvanceSurFrais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordre_de_mission_id", unique = true)
    private OrdreDeMission ordreDeMission;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    @Column(name = "montant_total")
    private BigDecimal montantTotal;

    @Column(name = "pourcentage_avance")
    private BigDecimal pourcentageAvance;

    @Column(name = "montant_avance")
    private BigDecimal montantAvance;

    @Column(name = "montant_solde")
    private BigDecimal montantSolde;

    @Column(name = "date_demande")
    private LocalDate dateDemande;

    @Column(name = "date_versement")
    private LocalDate dateVersement;

    @Column(name = "date_validation")
    private LocalDate dateValidation;

    private boolean validee;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private StatutAvance statut;

    @Column(name = "numero_reference", length = 100)
    private String numeroReference;

    /** Payment channel decided by the Direction des Finances when approving. */
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", length = 32)
    private ModePaiement modePaiement;

    /** Bank-transfer reference, when {@link #modePaiement} is VIREMENT. */
    @Column(name = "reference_virement", length = 120)
    private String referenceVirement;

    /** Human-readable message sent to the beneficiary once the request has been processed. */
    @Column(name = "message_beneficiaire", columnDefinition = "TEXT")
    private String messageBeneficiaire;

    public AvanceSurFrais() {
        this.pourcentageAvance = BigDecimal.ZERO;
        this.statut = StatutAvance.EN_ATTENTE;
        this.dateDemande = LocalDate.now();
        this.validee = false;
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

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public BigDecimal getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(BigDecimal montantTotal) {
        this.montantTotal = montantTotal;
    }

    public BigDecimal getPourcentageAvance() {
        return pourcentageAvance;
    }

    public void setPourcentageAvance(BigDecimal pourcentageAvance) {
        this.pourcentageAvance = pourcentageAvance;
    }

    public void setPourcentageAvance(int pct) {
        this.pourcentageAvance = BigDecimal.valueOf(pct);
    }

    public BigDecimal getMontantAvance() {
        return montantAvance;
    }

    public void setMontantAvance(BigDecimal montantAvance) {
        this.montantAvance = montantAvance;
    }

    public void setMontant(BigDecimal montant) {
        this.montantAvance = montant;
    }

    public BigDecimal getMontantSolde() {
        return montantSolde;
    }

    public void setMontantSolde(BigDecimal montantSolde) {
        this.montantSolde = montantSolde;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDate dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDate getDateVersement() {
        return dateVersement;
    }

    public void setDateVersement(LocalDate dateVersement) {
        this.dateVersement = dateVersement;
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }

    public boolean isValidee() {
        return validee;
    }

    public void setValidee(boolean validee) {
        this.validee = validee;
    }

    public StatutAvance getStatut() {
        return statut;
    }

    public void setStatut(StatutAvance statut) {
        this.statut = statut;
    }

    public String getNumeroReference() {
        return numeroReference;
    }

    public void setNumeroReference(String numeroReference) {
        this.numeroReference = numeroReference;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getReferenceVirement() {
        return referenceVirement;
    }

    public void setReferenceVirement(String referenceVirement) {
        this.referenceVirement = referenceVirement;
    }

    public String getMessageBeneficiaire() {
        return messageBeneficiaire;
    }

    public void setMessageBeneficiaire(String messageBeneficiaire) {
        this.messageBeneficiaire = messageBeneficiaire;
    }

    public enum StatutAvance {
        DEMANDEE,
        EN_ATTENTE,
        VALIDEE,
        VERSEE,
        REJETEE,
        REGULARISEE
    }

    public enum ModePaiement {
        ESPECES,
        VIREMENT
    }
}
