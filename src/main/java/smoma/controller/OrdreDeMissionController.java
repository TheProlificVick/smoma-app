package smoma.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.FileStorageService;
import smoma.controller.model.Service.OrdreDeMissionService;
import smoma.controller.model.Service.PdfGeneratorService;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordres-mission")
public class OrdreDeMissionController {

    private final OrdreDeMissionService omService;
    private final PdfGeneratorService pdfService;
    private final AccessPolicy accessPolicy;
    private final FileStorageService fileStorageService;

    public OrdreDeMissionController(OrdreDeMissionService omService, PdfGeneratorService pdfService, AccessPolicy accessPolicy,
                                    FileStorageService fileStorageService) {
        this.omService = omService;
        this.pdfService = pdfService;
        this.accessPolicy = accessPolicy;
        this.fileStorageService = fileStorageService;
    }

    /**
     * DRH/admin (who manage mission orders) get the full roster, same as before. Anyone else only
     * gets the orders actually issued to them — matched the same way as
     * {@link AccessPolicy#canViewOrdreDeMission}, so "Mes Missions" can no longer receive (and then
     * merely hide in the UI) every employee's mission data over the wire.
     */
    @GetMapping
    public ResponseEntity<List<OrdreDeMission>> getAllOrdres(
            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        List<OrdreDeMission> all = omService.getAllOrdres();
        User actor = accessPolicy.resolve(requestEmail);
        if (accessPolicy.canIssueMissionOrder(actor)) {
            return ResponseEntity.ok(all);
        }
        List<OrdreDeMission> own = all.stream()
                .filter(om -> accessPolicy.canViewOrdreDeMission(actor, om))
                .collect(Collectors.toList());
        return ResponseEntity.ok(own);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrdreById(@PathVariable Long id,
                                          @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        OrdreDeMission om = omService.getOrdreById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewOrdreDeMission(actor, om)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeOrdreDeMissionViewRule()));
        }
        return ResponseEntity.ok(om);
    }

    public static class DirectOmRequest {
        public OrdreDeMission om;
        public Long mandatId;
        public Long personnelId;
        public Long etapeId;
        public List<smoma.controller.model.EtapeMission> etapes;
    }

    @PostMapping
    public ResponseEntity<?> createDirectOrdre(@RequestBody DirectOmRequest req,
                                               @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canIssueMissionOrder(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMissionOrderRule()));
        }
        try {
            OrdreDeMission created = omService.createDirectOrdre(req.om, req.mandatId, req.personnelId, req.etapeId, req.etapes);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrdre(@PathVariable Long id, @RequestBody OrdreDeMission details,
                                         @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canIssueMissionOrder(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMissionOrderRule()));
        }
        if (details != null && details.getDateDebut() != null && details.getDateFin() != null
                && details.getDateFin().isBefore(details.getDateDebut())) {
            return ResponseEntity.badRequest().body(Map.of("error", "La date de fin ne peut pas précéder la date de début."));
        }
        try {
            OrdreDeMission updated = omService.updateOrdre(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Imports the signed individual mission order, scanned back in, PDF or photo. */
    @PostMapping(value = "/{id}/upload-scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSignedScan(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                              @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canIssueMissionOrder(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMissionOrderRule()));
        }
        try {
            String scanPath = fileStorageService.store(file, "scans", "om_" + id + "_signed");
            OrdreDeMission updated = omService.uploadSignedScan(id, scanPath);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * The verso fields (advance decompte, expense note, receipt acknowledgment) belong to whoever
     * may see the order in the first place — the assigned agent (who fills in the acknowledgment
     * they received payment) or DRH/admin (who fill in the administrative figures) — not only
     * DRH/admin, and not gated on the order still being a draft, since this data is filled in as
     * the mission actually happens.
     */
    @PutMapping("/{id}/verso")
    public ResponseEntity<?> updateVerso(@PathVariable Long id, @RequestBody OrdreDeMission details,
                                         @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        OrdreDeMission om = omService.getOrdreById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewOrdreDeMission(actor, om)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeOrdreDeMissionViewRule()));
        }
        try {
            OrdreDeMission updated = omService.updateVersoDetails(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * The assigned agent checks their own step off as done (or DRH/admin does it for them) —
     * same population as viewing the order. From then on, MissionCapacityService no longer counts
     * this order as occupying the agent's calendar, so they can be assigned elsewhere right away.
     */
    @PostMapping("/{id}/terminer")
    public ResponseEntity<?> completeMissionStep(@PathVariable Long id,
                                                 @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        OrdreDeMission om = omService.getOrdreById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewOrdreDeMission(actor, om)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeOrdreDeMissionViewRule()));
        }
        try {
            OrdreDeMission updated = omService.completeMissionStep(id, actor != null ? actor.getUsername() : requestEmail);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> downloadPdf(@PathVariable Long id,
                                         @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        OrdreDeMission om = omService.getOrdreById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewOrdreDeMission(actor, om)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeOrdreDeMissionViewRule()));
        }
        ByteArrayInputStream pdfStream = pdfService.generateOrdreDeMissionPdf(om);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + om.getReferenceOrdre() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
