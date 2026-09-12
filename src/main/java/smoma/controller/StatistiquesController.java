package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Genre;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.repository.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {

    private final MandatDeMissionRepository mandatRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final PersonnelRepository personnelRepository;
    private final RapportMissionRepository rapportRepository;

    public StatistiquesController(MandatDeMissionRepository mandatRepository,
                                  OrdreDeMissionRepository ordreRepository,
                                  PersonnelRepository personnelRepository,
                                  RapportMissionRepository rapportRepository) {
        this.mandatRepository = mandatRepository;
        this.ordreRepository = ordreRepository;
        this.personnelRepository = personnelRepository;
        this.rapportRepository = rapportRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> stats = new HashMap<>();

        long totalMandats = mandatRepository.count();
        long totalOrdres = ordreRepository.count();
        long totalPersonnel = personnelRepository.count();
        long totalRapports = rapportRepository.count();

        long ordresSansFrais = ordreRepository.findAll().stream().filter(o -> o.isSansFrais()).count();
        long ordresAvecFrais = totalOrdres - ordresSansFrais;

        stats.put("totalMandats", totalMandats);
        stats.put("totalOrdres", totalOrdres);
        stats.put("totalPersonnel", totalPersonnel);
        stats.put("totalRapports", totalRapports);
        stats.put("ordresSansFrais", ordresSansFrais);
        stats.put("ordresAvecFrais", ordresAvecFrais);

        return ResponseEntity.ok(stats);
    }

    /**
     * Mission analytics for the annual report: gender split of missions and staff, mission type
     * (Interne/Externe) breakdown, per-department volume, average duration and total indemnities
     * paid. Read-only, no access restriction — same audience as the rest of the annual dashboard.
     */
    @GetMapping("/analyse-missions")
    public ResponseEntity<Map<String, Object>> getMissionAnalysis(
            @RequestParam(required = false) Integer annee) {
        List<OrdreDeMission> allOrders = ordreRepository.findAll();
        if (annee != null) {
            allOrders = allOrders.stream()
                    .filter(o -> o.getDateDebut() != null && o.getDateDebut().getYear() == annee)
                    .toList();
        }

        Map<String, Long> missionsByGenre = new LinkedHashMap<>();
        Map<String, Long> distinctAgentsByGenre = new LinkedHashMap<>();
        Map<String, Set<Long>> agentIdsByGenre = new HashMap<>();
        Map<String, Long> missionsByType = new LinkedHashMap<>();
        Map<String, Long> missionsByDepartment = new LinkedHashMap<>();
        long totalDays = 0;
        long ordersWithDates = 0;
        BigDecimal totalIndemnites = BigDecimal.ZERO;
        BigDecimal totalAvances = BigDecimal.ZERO;
        BigDecimal totalSoldes = BigDecimal.ZERO;

        for (String key : new String[]{"HOMME", "FEMME", "NON_PRECISE"}) {
            missionsByGenre.put(key, 0L);
            distinctAgentsByGenre.put(key, 0L);
            agentIdsByGenre.put(key, new HashSet<>());
        }

        for (OrdreDeMission o : allOrders) {
            String genreKey = "NON_PRECISE";
            Personnel p = o.getPersonnel();
            if (p != null) {
                Genre g = p.getGenre();
                genreKey = g != null ? g.name() : "NON_PRECISE";
                if (p.getId() != null) agentIdsByGenre.get(genreKey).add(p.getId());
                if (p.getDepartement() != null && !p.getDepartement().isBlank()) {
                    missionsByDepartment.merge(p.getDepartement(), 1L, Long::sum);
                }
            }
            missionsByGenre.merge(genreKey, 1L, Long::sum);

            String type = o.getTypeMission() != null ? o.getTypeMission().name() : "INTERNE";
            missionsByType.merge(type, 1L, Long::sum);

            if (o.getDateDebut() != null && o.getDateFin() != null) {
                long days = ChronoUnit.DAYS.between(o.getDateDebut(), o.getDateFin()) + 1;
                if (days > 0) {
                    totalDays += days;
                    ordersWithDates++;
                }
            }
            if (o.getMontantIndemnite() != null) totalIndemnites = totalIndemnites.add(o.getMontantIndemnite());
            if (o.getMontantAvance() != null) totalAvances = totalAvances.add(o.getMontantAvance());
            if (o.getMontantSolde() != null) totalSoldes = totalSoldes.add(o.getMontantSolde());
        }
        for (String key : agentIdsByGenre.keySet()) {
            distinctAgentsByGenre.put(key, (long) agentIdsByGenre.get(key).size());
        }

        List<Personnel> allPersonnel = personnelRepository.findAll();
        long effectifHommes = allPersonnel.stream().filter(p -> p.getGenre() == Genre.HOMME).count();
        long effectifFemmes = allPersonnel.stream().filter(p -> p.getGenre() == Genre.FEMME).count();
        long effectifNonPrecise = allPersonnel.size() - effectifHommes - effectifFemmes;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMissions", allOrders.size());
        result.put("missionsByGenre", missionsByGenre);
        result.put("distinctAgentsByGenre", distinctAgentsByGenre);
        result.put("missionsByType", missionsByType);
        result.put("missionsByDepartment", missionsByDepartment);
        result.put("totalJoursMission", totalDays);
        result.put("dureeMoyenneJours", ordersWithDates > 0 ? Math.round((double) totalDays / ordersWithDates * 10) / 10.0 : 0);
        result.put("totalIndemnitesVersees", totalIndemnites);
        result.put("totalAvancesVersees", totalAvances);
        result.put("totalSoldesVerses", totalSoldes);
        result.put("effectifTotal", allPersonnel.size());
        result.put("effectifHommes", effectifHommes);
        result.put("effectifFemmes", effectifFemmes);
        result.put("effectifNonPrecise", effectifNonPrecise);

        return ResponseEntity.ok(result);
    }
}
