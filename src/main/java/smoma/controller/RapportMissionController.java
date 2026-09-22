package smoma.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.RapportMission;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.FileStorageService;
import smoma.controller.model.Service.OrdreDeMissionService;
import smoma.controller.model.Service.RapportMissionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rapports")
public class RapportMissionController {

    private final RapportMissionService rapportService;
    private final AccessPolicy accessPolicy;
    private final FileStorageService fileStorageService;
    private final OrdreDeMissionService ordreDeMissionService;

    public RapportMissionController(RapportMissionService rapportService, AccessPolicy accessPolicy,
                                    FileStorageService fileStorageService, OrdreDeMissionService ordreDeMissionService) {
        this.rapportService = rapportService;
        this.accessPolicy = accessPolicy;
        this.fileStorageService = fileStorageService;
        this.ordreDeMissionService = ordreDeMissionService;
    }

    /** Deposits a mission report: the staff member's scanned/typed report, PDF or photo. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> depositReport(@RequestParam Long omId,
                                           @RequestParam String titre,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) String categorie,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        OrdreDeMission om = ordreDeMissionService.getOrdreById(omId);
        if (!accessPolicy.canDepositReport(user, om)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seul l'agent affecté à cet ordre de mission, ou la DRH, peut déposer ce rapport."));
        }
        try {
            String fichierPath = fileStorageService.store(file, "rapports", "rapport_om" + omId);
            RapportMission saved = rapportService.depositReport(omId, titre, description, categorie, fichierPath, "[]");
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Whoever manages report validation (DRH/admin) gets the full roster, same as before;
     * anyone else only gets back reports that belong to them — a report's title, description,
     * and any rejection justification are not something a colleague should see over the wire,
     * even if the UI that called this only intended to show the caller their own.
     */
    @GetMapping
    public ResponseEntity<List<RapportMission>> searchReports(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) Long personnelId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User actor = accessPolicy.resolve(userEmail);
        List<RapportMission> all = rapportService.searchReports(query, categorie, personnelId);
        if (accessPolicy.canValidateReport(actor)) {
            return ResponseEntity.ok(all);
        }
        List<RapportMission> own = all.stream()
                .filter(r -> accessPolicy.canViewReport(actor, r))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(own);
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validateReport(@PathVariable Long id,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canValidateReport(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeReportRule()));
        }
        try {
            RapportMission validated = rapportService.validateReport(id, user.getUsername());
            return ResponseEntity.ok(validated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public static class RejectRequest {
        public String motif;
    }

    /** Same DRH/SP/admin population as validation — rejecting requires a typed-in justification. */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejectReport(@PathVariable Long id, @RequestBody RejectRequest body,
                                          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canValidateReport(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeReportRule()));
        }
        try {
            RapportMission rejected = rapportService.rejectReport(id, body != null ? body.motif : null, user.getUsername());
            return ResponseEntity.ok(rejected);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
