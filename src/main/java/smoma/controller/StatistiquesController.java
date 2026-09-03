package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.repository.*;

import java.util.HashMap;
import java.util.Map;

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
}
