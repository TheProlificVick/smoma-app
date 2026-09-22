package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.*;
import smoma.repository.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Computes mission-indemnity rates and totals. Requesting/approving an advance or balance is no
 * longer part of this app — the Direction des Finances handles that in its own software — so this
 * service only exposes the (read-only) historical advance records and the rate calculation used
 * by the mission-order PDF and the payment tracker.
 */
@Service
public class IndemniteService {

    private final BaremeIndemniteRepository baremeRepository;
    private final AvanceSurFraisRepository avanceRepository;

    public IndemniteService(BaremeIndemniteRepository baremeRepository,
                            AvanceSurFraisRepository avanceRepository) {
        this.baremeRepository = baremeRepository;
        this.avanceRepository = avanceRepository;
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
}
