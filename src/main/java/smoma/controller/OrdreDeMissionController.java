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

    @GetMapping
    public ResponseEntity<List<OrdreDeMission>> getAllOrdres() {
        return ResponseEntity.ok(omService.getAllOrdres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdreDeMission> getOrdreById(@PathVariable Long id) {
        return ResponseEntity.ok(omService.getOrdreById(id));
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

    @GetMapping("/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Long id) {
        OrdreDeMission om = omService.getOrdreById(id);
        ByteArrayInputStream pdfStream = pdfService.generateOrdreDeMissionPdf(om);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + om.getReferenceOrdre() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
