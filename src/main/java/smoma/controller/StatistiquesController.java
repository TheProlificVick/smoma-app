package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Genre;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
     * Mission analytics for the annual report, built from every mission order on file: gender split
     * of missions/staff/mission-days/indemnities (answers "who traveled more, men or women?"), mission
     * type (Interne/Externe) breakdown, per-department/destination/transport/grade/year volume, the
     * most-traveled agents, report-filing compliance, average duration and total indemnities paid.
     * Read-only, no access restriction — same audience as the rest of the annual dashboard.
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
        Map<String, Long> joursMissionByGenre = new LinkedHashMap<>();
        Map<String, BigDecimal> indemnitesByGenre = new LinkedHashMap<>();
        Map<String, Long> missionsByType = new LinkedHashMap<>();
        Map<String, Long> missionsByDepartment = new LinkedHashMap<>();
        Map<String, Long> missionsByDestination = new HashMap<>();
        Map<String, Long> missionsByTransportMode = new HashMap<>();
        Map<String, Long> missionsByGrade = new HashMap<>();
        Map<String, Long> missionsByStatut = new LinkedHashMap<>();
        Map<Integer, Long> missionsByYear = new TreeMap<>();
        Map<Long, long[]> missionStatsByAgent = new HashMap<>(); // agentId -> {nbMissions, totalJours}
        Map<Long, BigDecimal> indemniteByAgent = new HashMap<>();
        Map<Long, Personnel> agentById = new HashMap<>();

        long totalDays = 0;
        long ordersWithDates = 0;
        long missionsAvecRapport = 0;
        BigDecimal totalIndemnites = BigDecimal.ZERO;
        BigDecimal totalAvances = BigDecimal.ZERO;
        BigDecimal totalSoldes = BigDecimal.ZERO;

        for (String key : new String[]{"HOMME", "FEMME", "NON_PRECISE"}) {
            missionsByGenre.put(key, 0L);
            distinctAgentsByGenre.put(key, 0L);
            agentIdsByGenre.put(key, new HashSet<>());
            joursMissionByGenre.put(key, 0L);
            indemnitesByGenre.put(key, BigDecimal.ZERO);
        }

        for (OrdreDeMission o : allOrders) {
            String genreKey = "NON_PRECISE";
            Personnel p = o.getPersonnel();
            BigDecimal indemnite = o.getMontantIndemnite() != null ? o.getMontantIndemnite() : BigDecimal.ZERO;

            long days = 0;
            if (o.getDateDebut() != null && o.getDateFin() != null) {
                long span = ChronoUnit.DAYS.between(o.getDateDebut(), o.getDateFin()) + 1;
                if (span > 0) {
                    days = span;
                    totalDays += days;
                    ordersWithDates++;
                }
            }

            if (p != null) {
                Genre g = p.getGenre();
                genreKey = g != null ? g.name() : "NON_PRECISE";
                if (p.getId() != null) {
                    agentIdsByGenre.get(genreKey).add(p.getId());
                    agentById.put(p.getId(), p);
                    long[] tally = missionStatsByAgent.computeIfAbsent(p.getId(), k -> new long[2]);
                    tally[0]++;
                    tally[1] += days;
                    indemniteByAgent.merge(p.getId(), indemnite, BigDecimal::add);
                }
                if (p.getDepartement() != null && !p.getDepartement().isBlank()) {
                    missionsByDepartment.merge(p.getDepartement(), 1L, Long::sum);
                }
                if (p.getGrade() != null) {
                    missionsByGrade.merge(p.getGrade().name(), 1L, Long::sum);
                }
            }
            missionsByGenre.merge(genreKey, 1L, Long::sum);
            joursMissionByGenre.merge(genreKey, days, Long::sum);
            indemnitesByGenre.merge(genreKey, indemnite, BigDecimal::add);

            String type = o.getTypeMission() != null ? o.getTypeMission().name() : "INTERNE";
            missionsByType.merge(type, 1L, Long::sum);

            String statutKey = o.getStatut() != null ? o.getStatut().name() : "INCONNU";
            missionsByStatut.merge(statutKey, 1L, Long::sum);

            String destination = (o.getEtape() != null && o.getEtape().getLieu() != null && !o.getEtape().getLieu().isBlank())
                    ? o.getEtape().getLieu() : null;
            if (destination != null) missionsByDestination.merge(destination, 1L, Long::sum);

            if (o.getMoyenTransport() != null && !o.getMoyenTransport().isBlank()) {
                missionsByTransportMode.merge(o.getMoyenTransport(), 1L, Long::sum);
            }

            if (o.getDateDebut() != null) {
                missionsByYear.merge(o.getDateDebut().getYear(), 1L, Long::sum);
            }

            if (o.isRapportSoumis()) missionsAvecRapport++;

            totalIndemnites = totalIndemnites.add(indemnite);
            if (o.getMontantAvance() != null) totalAvances = totalAvances.add(o.getMontantAvance());
            if (o.getMontantSolde() != null) totalSoldes = totalSoldes.add(o.getMontantSolde());
        }
        for (String key : agentIdsByGenre.keySet()) {
            distinctAgentsByGenre.put(key, (long) agentIdsByGenre.get(key).size());
        }

        // Who travels the most: top 10 staff ranked by number of missions.
        List<Map<String, Object>> topAgents = missionStatsByAgent.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    Personnel p = agentById.get(e.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("personnelId", e.getKey());
                    row.put("nomComplet", p != null ? p.getFullName() : "?");
                    row.put("matricule", p != null ? p.getMatricule() : null);
                    row.put("genre", p != null && p.getGenre() != null ? p.getGenre().name() : "NON_PRECISE");
                    row.put("departement", p != null ? p.getDepartement() : null);
                    row.put("nbMissions", e.getValue()[0]);
                    row.put("totalJours", e.getValue()[1]);
                    row.put("totalIndemnite", indemniteByAgent.getOrDefault(e.getKey(), BigDecimal.ZERO));
                    return row;
                })
                .toList();

        Map<String, Long> topDestinations = new LinkedHashMap<>();
        missionsByDestination.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> topDestinations.put(e.getKey(), e.getValue()));

        // Plain answer to "who went on more missions, men or women?"
        long missionsHommes = missionsByGenre.getOrDefault("HOMME", 0L);
        long missionsFemmes = missionsByGenre.getOrDefault("FEMME", 0L);
        String genreDominant = missionsHommes == missionsFemmes ? "EGALITE" : (missionsHommes > missionsFemmes ? "HOMME" : "FEMME");

        List<Personnel> allPersonnel = personnelRepository.findAll();
        long effectifHommes = allPersonnel.stream().filter(p -> p.getGenre() == Genre.HOMME).count();
        long effectifFemmes = allPersonnel.stream().filter(p -> p.getGenre() == Genre.FEMME).count();
        long effectifNonPrecise = allPersonnel.size() - effectifHommes - effectifFemmes;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMissions", allOrders.size());
        result.put("missionsByGenre", missionsByGenre);
        result.put("distinctAgentsByGenre", distinctAgentsByGenre);
        result.put("joursMissionByGenre", joursMissionByGenre);
        result.put("indemnitesByGenre", indemnitesByGenre);
        result.put("genreDominant", genreDominant);
        result.put("missionsByType", missionsByType);
        result.put("missionsByDepartment", missionsByDepartment);
        result.put("missionsByDestination", topDestinations);
        result.put("missionsByTransportMode", missionsByTransportMode);
        result.put("missionsByGrade", missionsByGrade);
        result.put("missionsByStatut", missionsByStatut);
        result.put("missionsByYear", missionsByYear);
        result.put("topAgents", topAgents);
        result.put("totalJoursMission", totalDays);
        result.put("dureeMoyenneJours", ordersWithDates > 0 ? Math.round((double) totalDays / ordersWithDates * 10) / 10.0 : 0);
        result.put("totalIndemnitesVersees", totalIndemnites);
        result.put("totalAvancesVersees", totalAvances);
        result.put("totalSoldesVerses", totalSoldes);
        result.put("moyenneIndemniteParMission", allOrders.isEmpty()
                ? BigDecimal.ZERO
                : totalIndemnites.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP));
        result.put("tauxRapportSoumisPct", allOrders.isEmpty() ? 0 : Math.round((double) missionsAvecRapport / allOrders.size() * 1000) / 10.0);
        result.put("effectifTotal", allPersonnel.size());
        result.put("effectifHommes", effectifHommes);
        result.put("effectifFemmes", effectifFemmes);
        result.put("effectifNonPrecise", effectifNonPrecise);

        return ResponseEntity.ok(result);
    }
}
