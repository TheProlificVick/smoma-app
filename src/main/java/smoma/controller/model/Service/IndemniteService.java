package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class IndemniteService {

    private final BaremeIndemniteRepository baremeRepository;
    private final AvanceSurFraisRepository avanceRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final AuditLogRepository auditLogRepository;
    private final RapportMissionRepository rapportRepository;
    private final NotificationService notificationService;
    private final smoma.repository.UserRepository userRepository;
    private final AccessPolicy accessPolicy;

    public IndemniteService(BaremeIndemniteRepository baremeRepository,
                            AvanceSurFraisRepository avanceRepository,
                            OrdreDeMissionRepository ordreRepository,
                            AuditLogRepository auditLogRepository,
                            RapportMissionRepository rapportRepository,
                            NotificationService notificationService,
                            smoma.repository.UserRepository userRepository,
                            AccessPolicy accessPolicy) {
        this.baremeRepository = baremeRepository;
        this.avanceRepository = avanceRepository;
        this.ordreRepository = ordreRepository;
        this.auditLogRepository = auditLogRepository;
        this.rapportRepository = rapportRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
    }

    private String beneficiaireMatricule(AvanceSurFrais a) {
        return a.getPersonnel() != null ? a.getPersonnel().getMatricule() : null;
    }

    private String beneficiaireUsername(AvanceSurFrais a) {
        return a.getPersonnel() != null ? a.getPersonnel().getEmail() : null;
    }

    /** Notifies every Direction des Finances approver that a new advance / balance request is pending. */
    private void notifyFinanceOfRequest(AvanceSurFrais avance, String kind) {
        try {
            String agent = avance.getPersonnel() != null ? avance.getPersonnel().getFullName() : "Agent";
            String ref = avance.getOrdreDeMission() != null && avance.getOrdreDeMission().getReferenceOrdre() != null
                    ? avance.getOrdreDeMission().getReferenceOrdre() : ("OM #" + (avance.getOrdreDeMission() != null ? avance.getOrdreDeMission().getId() : "?"));
            BigDecimal montant = "SOLDE".equals(kind) ? avance.getMontantSolde() : avance.getMontantAvance();
            String titre = "SOLDE".equals(kind)
                    ? "Demande de solde à approuver / Balance request to approve"
                    : "Demande d'avance à approuver / Advance request to approve";
            String message = ("SOLDE".equals(kind) ? "Solde" : "Avance " + avance.getPourcentageAvance() + "%")
                    + " de " + (montant != null ? montant : "?") + " XAF pour " + agent + " (" + ref + "). "
                    + "À traiter dans le Guichet des Avances & Indemnités.";
            for (smoma.controller.model.User u : userRepository.findAll()) {
                if (accessPolicy.isFinanceApprover(u)) {
                    notificationService.create(u.getMatricule(), u.getUsername(), titre, message,
                            "SOLDE".equals(kind) ? "BALANCE_REQUEST" : "ADVANCE_REQUEST", "/mission-payment.html");
                }
            }
        } catch (Exception e) {
            // never break the request flow because of a notification
        }
    }

    public List<AvanceSurFrais> getAllAvances() {
        return avanceRepository.findAll();
    }

    public BigDecimal calculateDailyRate(Personnel agent, String typeMissionStr) {
        if (agent == null) return BigDecimal.valueOf(25000);

        List<BaremeIndemnite> baremes = baremeRepository.findAll();
        for (BaremeIndemnite b : baremes) {
            if (b.getRang() != null && b.getRang().equalsIgnoreCase(agent.getRang())) {
                if (b.getTypeMission() != null && b.getTypeMission().name().equalsIgnoreCase(typeMissionStr)) {
                    return b.getMontantJournalier();
                }
            }
        }
        return "EXTERNE".equalsIgnoreCase(typeMissionStr) ? BigDecimal.valueOf(150000) : BigDecimal.valueOf(50000);
    }

    public BigDecimal calculateTotalIndemnite(OrdreDeMission om) {
        if (om == null || om.isSansFrais()) return BigDecimal.ZERO;
        if (om.getDateDebut() == null || om.getDateFin() == null) return BigDecimal.ZERO;

        long days = ChronoUnit.DAYS.between(om.getDateDebut(), om.getDateFin()) + 1;
        if (days <= 0) days = 1;

        String typeStr = om.getTypeMission() != null ? om.getTypeMission().name() : "INTERNE";
        BigDecimal dailyRate = calculateDailyRate(om.getPersonnel(), typeStr);

        return dailyRate.multiply(BigDecimal.valueOf(days));
    }

    @Transactional
    public AvanceSurFrais requestAdvance(Long omId, LocalDate dateDemande) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));

        if (om.isSansFrais()) {
            throw new IllegalStateException("Cet ordre de mission est marqué SANS FRAIS. Aucune avance ne peut être sollicitée.");
        }

        if (dateDemande == null) dateDemande = LocalDate.now();

        if (om.getDateDebut() != null && !dateDemande.isBefore(om.getDateDebut())) {
            throw new IllegalStateException("La demande d'avance n'est recevable que si elle est introduite avant la date de début de la mission (" + om.getDateDebut() + ").");
        }

        BigDecimal totalIndemnite = calculateTotalIndemnite(om);
        boolean isExterne = (om.getTypeMission() == OrdreDeMission.TypeMission.EXTERNE);
        int pct = isExterne ? 90 : 75;

        BigDecimal advanceAmount = totalIndemnite.multiply(BigDecimal.valueOf(pct)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        AvanceSurFrais avance = new AvanceSurFrais();
        avance.setOrdreDeMission(om);
        avance.setPersonnel(om.getPersonnel());
        avance.setPourcentageAvance(pct);
        avance.setMontant(advanceAmount);
        avance.setDateDemande(dateDemande);
        avance.setStatut(AvanceSurFrais.StatutAvance.DEMANDEE);

        om.setMontantIndemnite(totalIndemnite);
        om.setMontantAvance(advanceAmount);
        om.setMontantSolde(totalIndemnite.subtract(advanceAmount));
        ordreRepository.save(om);

        AvanceSurFrais saved = avanceRepository.save(avance);
        auditLogRepository.save(new AuditLog("REQUEST_ADVANCE", "SYSTEM", "Demande d'avance (" + pct + "%): " + advanceAmount + " XAF pour OM #" + omId));

        // Route the request to the Direction des Finances for approval.
        notifyFinanceOfRequest(saved, "AVANCE");
        return saved;
    }

    /**
     * Approval of an advance request by the Direction des Finances. The approver chooses the
     * payment channel: ESPECES (the beneficiary comes to collect at the Direction des Finances)
     * or VIREMENT (a bank transfer is executed). The beneficiary is then notified accordingly.
     */
    @Transactional
    public AvanceSurFrais validateAdvance(Long avanceId, String modePaiementStr, String referenceVirement) {
        AvanceSurFrais avance = avanceRepository.findById(avanceId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'avance introuvable: " + avanceId));

        AvanceSurFrais.ModePaiement mode = AvanceSurFrais.ModePaiement.ESPECES;
        if (modePaiementStr != null && !modePaiementStr.isBlank()) {
            try {
                mode = AvanceSurFrais.ModePaiement.valueOf(modePaiementStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                if (modePaiementStr.toLowerCase().contains("vir")) mode = AvanceSurFrais.ModePaiement.VIREMENT;
            }
        }

        avance.setStatut(AvanceSurFrais.StatutAvance.VERSEE);
        avance.setValidee(true);
        avance.setDateValidation(LocalDate.now());
        avance.setDateVersement(LocalDate.now());
        avance.setModePaiement(mode);

        String montant = String.valueOf(avance.getMontantAvance());
        String message;
        if (mode == AvanceSurFrais.ModePaiement.VIREMENT) {
            avance.setReferenceVirement(referenceVirement);
            message = "Votre demande d'avance de " + montant + " XAF a été approuvée par la Direction des Finances "
                    + "et réglée par virement bancaire"
                    + (referenceVirement != null && !referenceVirement.isBlank() ? " (référence : " + referenceVirement + ")" : "") + ".";
        } else {
            message = "Votre demande d'avance de " + montant + " XAF a été approuvée par la Direction des Finances. "
                    + "Veuillez vous présenter à la Direction des Finances, muni(e) d'une pièce d'identité, pour retirer le montant en espèces.";
        }
        avance.setMessageBeneficiaire(message);

        AvanceSurFrais saved = avanceRepository.save(avance);

        notificationService.create(beneficiaireMatricule(avance), beneficiaireUsername(avance),
                "Avance approuvée / Advance approved", message, "ADVANCE_APPROVED", "/my-missions.html");
        auditLogRepository.save(new AuditLog("VALIDATE_ADVANCE", "FINANCE",
                "Avance " + avanceId + " approuvée — mode: " + mode + (referenceVirement != null ? " ref: " + referenceVirement : "")));
        return saved;
    }

    @Transactional
    public AvanceSurFrais validateAdvance(Long avanceId) {
        return validateAdvance(avanceId, null, null);
    }

    /**
     * Liquidation of the mission balance (25% internal / 10% external). Blocked until the signed
     * mission order scan AND the mission report have been submitted (spec 4.6 / 4.7 rules).
     */
    @Transactional
    public AvanceSurFrais verserSolde(Long avanceId) {
        return verserSolde(avanceId, null, null);
    }

    @Transactional
    public AvanceSurFrais verserSolde(Long avanceId, String modePaiementStr, String referenceVirement) {
        AvanceSurFrais avance = avanceRepository.findById(avanceId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'avance introuvable: " + avanceId));

        OrdreDeMission om = avance.getOrdreDeMission();
        if (om == null) {
            throw new IllegalStateException("Cette avance n'est rattachée à aucun ordre de mission.");
        }
        if (!om.isValide()) {
            throw new IllegalStateException("Solde bloqué: la version signée et scannée de l'ordre de mission n'a pas été importée.");
        }
        RapportMission rapport = rapportRepository.findByOrdreDeMission(om).orElse(null);
        if (rapport == null || !om.isRapportSoumis()) {
            throw new IllegalStateException("Solde bloqué: le rapport de mission n'a pas encore été déposé.");
        }

        BigDecimal solde = om.getMontantSolde();
        if (solde == null) {
            BigDecimal indemnite = om.getMontantIndemnite() != null ? om.getMontantIndemnite() : calculateTotalIndemnite(om);
            BigDecimal avanceVersee = om.getMontantAvance() != null ? om.getMontantAvance() : BigDecimal.ZERO;
            solde = indemnite.subtract(avanceVersee);
        }

        AvanceSurFrais.ModePaiement mode = AvanceSurFrais.ModePaiement.ESPECES;
        if (modePaiementStr != null && modePaiementStr.toLowerCase().contains("vir")) {
            mode = AvanceSurFrais.ModePaiement.VIREMENT;
        }

        avance.setMontantSolde(solde);
        avance.setStatut(AvanceSurFrais.StatutAvance.VERSEE);
        avance.setDateVersement(LocalDate.now());
        avance.setValidee(true);
        avance.setModePaiement(mode);

        String montant = String.valueOf(solde);
        String message;
        if (mode == AvanceSurFrais.ModePaiement.VIREMENT) {
            avance.setReferenceVirement(referenceVirement);
            message = "Le solde de vos frais de mission (" + montant + " XAF) a été approuvé par la Direction des Finances "
                    + "et réglé par virement bancaire"
                    + (referenceVirement != null && !referenceVirement.isBlank() ? " (référence : " + referenceVirement + ")" : "") + ".";
        } else {
            message = "Le solde de vos frais de mission (" + montant + " XAF) a été approuvé par la Direction des Finances. "
                    + "Veuillez vous présenter à la Direction des Finances pour retirer le montant en espèces.";
        }
        avance.setMessageBeneficiaire(message);

        AvanceSurFrais saved = avanceRepository.save(avance);
        notificationService.create(beneficiaireMatricule(avance), beneficiaireUsername(avance),
                "Solde versé / Balance paid", message, "BALANCE_PAID", "/my-missions.html");
        auditLogRepository.save(new AuditLog("PAY_BALANCE", "FINANCE",
                "Solde liquidé (" + solde + " XAF) pour OM #" + om.getId() + " — mode: " + mode));
        return saved;
    }
}
