package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.repository.OrdreDeMissionRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Two staff-scheduling rules enforced everywhere an agent is committed to a mission period
 * (mandate step assignment, OM generation, direct OM creation):
 * <ul>
 *   <li>a staff member cannot be assigned to more than {@link #MAX_DAYS_PER_FISCAL_YEAR} days
 *       of mission per fiscal year (calendar year), regardless of rank;</li>
 *   <li>a staff member cannot be assigned to two mission periods that overlap in time — they
 *       must finish one step before starting another ("one step at a time").</li>
 * </ul>
 */
@Service
public class MissionCapacityService {

    public static final int MAX_DAYS_PER_FISCAL_YEAR = 100;

    private final OrdreDeMissionRepository ordreRepository;

    public MissionCapacityService(OrdreDeMissionRepository ordreRepository) {
        this.ordreRepository = ordreRepository;
    }

    private long durationDays(LocalDate debut, LocalDate fin) {
        long d = ChronoUnit.DAYS.between(debut, fin) + 1;
        return d > 0 ? d : 1;
    }

    /** Mission days already committed to this agent in the given fiscal (calendar) year. */
    public long committedDaysInFiscalYear(Long personnelId, int fiscalYear, Long excludeOmId) {
        if (personnelId == null) return 0;
        List<OrdreDeMission> all = ordreRepository.findAll();
        long sum = 0;
        for (OrdreDeMission o : all) {
            if (o.getPersonnel() == null || !personnelId.equals(o.getPersonnel().getId())) continue;
            if (excludeOmId != null && excludeOmId.equals(o.getId())) continue;
            if (o.getDateDebut() == null || o.getDateFin() == null) continue;
            if (o.getDateDebut().getYear() != fiscalYear) continue;
            sum += durationDays(o.getDateDebut(), o.getDateFin());
        }
        return sum;
    }

    /**
     * Throws if committing {@code agent} to a mission period {@code [dateDebut, dateFin]} would
     * push their total mission days for that fiscal year beyond {@link #MAX_DAYS_PER_FISCAL_YEAR}.
     */
    public void assertWithinAnnualCap(Personnel agent, LocalDate dateDebut, LocalDate dateFin, Long excludeOmId) {
        if (agent == null || dateDebut == null || dateFin == null) return;
        long newDays = durationDays(dateDebut, dateFin);
        int fiscalYear = dateDebut.getYear();
        long existing = committedDaysInFiscalYear(agent.getId(), fiscalYear, excludeOmId);
        long total = existing + newDays;
        if (total > MAX_DAYS_PER_FISCAL_YEAR) {
            throw new IllegalStateException("Plafond annuel de mission dépassé pour " + agent.getFullName()
                    + " : " + existing + " jour(s) déjà comptabilisé(s) sur l'exercice " + fiscalYear
                    + "; cette affectation de " + newDays + " jour(s) porterait le total à " + total
                    + " jours, au-delà du plafond réglementaire de " + MAX_DAYS_PER_FISCAL_YEAR + " jours/an.");
        }
    }

    /**
     * Throws if {@code agent} is already committed (via another OM) to a mission period that
     * overlaps {@code [dateDebut, dateFin]} — a staff member can only be on one step at a time.
     */
    public void assertNoOverlap(Personnel agent, LocalDate dateDebut, LocalDate dateFin, Long excludeOmId) {
        if (agent == null || dateDebut == null || dateFin == null) return;
        for (OrdreDeMission existing : ordreRepository.findAll()) {
            if (existing.getPersonnel() == null || !existing.getPersonnel().getId().equals(agent.getId())) continue;
            if (excludeOmId != null && excludeOmId.equals(existing.getId())) continue;
            if (existing.getDateDebut() == null || existing.getDateFin() == null) continue;
            boolean overlaps = !dateDebut.isAfter(existing.getDateFin()) && !dateFin.isBefore(existing.getDateDebut());
            if (overlaps) {
                throw new IllegalStateException("Conflit de planning pour " + agent.getFullName()
                        + " : déjà affecté(e) du " + existing.getDateDebut() + " au " + existing.getDateFin()
                        + " (" + existing.getReferenceOrdre() + "). Un agent ne peut être affecté qu'à une seule étape "
                        + "de mission à la fois ; la nouvelle affectation n'est possible qu'une fois l'étape en cours terminée.");
            }
        }
    }

    /** Runs both checks; convenience for the common case. */
    public void assertAssignable(Personnel agent, LocalDate dateDebut, LocalDate dateFin, Long excludeOmId) {
        assertNoOverlap(agent, dateDebut, dateFin, excludeOmId);
        assertWithinAnnualCap(agent, dateDebut, dateFin, excludeOmId);
    }
}
