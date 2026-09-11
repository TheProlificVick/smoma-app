package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.RapportMission;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.RapportMissionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rapports")
public class RapportMissionController {

    private final RapportMissionService rapportService;
    private final AccessPolicy accessPolicy;

    public RapportMissionController(RapportMissionService rapportService, AccessPolicy accessPolicy) {
        this.rapportService = rapportService;
        this.accessPolicy = accessPolicy;
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
    public ResponseEntity<?> validateReport(@PathVariable Long id,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canValidateReport(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeReportRule()));
        }
        try {
            RapportMission validated = rapportService.validateReport(id);
            return ResponseEntity.ok(validated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
