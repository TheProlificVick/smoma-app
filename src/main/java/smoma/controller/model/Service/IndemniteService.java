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

    public IndemniteService(BaremeIndemniteRepository baremeRepository,
                            AvanceSurFraisRepository avanceRepository,
                            OrdreDeMissionRepository ordreRepository,
                            AuditLogRepository auditLogRepository) {
        this.baremeRepository = baremeRepository;
        this.avanceRepository = avanceRepository;
        this.ordreRepository = ordreRepository;
        this.auditLogRepository = auditLogRepository;
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
        return saved;
    }

    @Transactional
    public AvanceSurFrais validateAdvance(Long avanceId) {
        AvanceSurFrais avance = avanceRepository.findById(avanceId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'avance introuvable: " + avanceId));

        avance.setStatut(AvanceSurFrais.StatutAvance.VALIDEE);
        avance.setDateValidation(LocalDate.now());

        auditLogRepository.save(new AuditLog("VALIDATE_ADVANCE", "SYSTEM", "Avance validée ID: " + avanceId));
        return avanceRepository.save(avance);
    }
}
