package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.RapportMission;
import smoma.controller.model.Service.RapportMissionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rapports")
public class RapportMissionController {

    private final RapportMissionService rapportService;

    public RapportMissionController(RapportMissionService rapportService) {
        this.rapportService = rapportService;
    }

    public static class DepositReportRequest {
        public Long omId;
        public String titre;
        public String description;
        public String categorie;
        public String fichierPath;
        public String justificatifsJson;
    }

    @PostMapping
    public ResponseEntity<?> depositReport(@RequestBody DepositReportRequest req) {
        try {
            RapportMission saved = rapportService.depositReport(req.omId, req.titre, req.description, req.categorie, req.fichierPath, req.justificatifsJson);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<RapportMission>> searchReports(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) Long personnelId) {
        return ResponseEntity.ok(rapportService.searchReports(query, categorie, personnelId));
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validateReport(@PathVariable Long id) {
        try {
            RapportMission validated = rapportService.validateReport(id);
            return ResponseEntity.ok(validated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
