package smoma.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.OrdreDeMission;
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

    public OrdreDeMissionController(OrdreDeMissionService omService, PdfGeneratorService pdfService) {
        this.omService = omService;
        this.pdfService = pdfService;
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
    }

    @PostMapping
    public ResponseEntity<?> createDirectOrdre(@RequestBody DirectOmRequest req) {
        try {
            OrdreDeMission created = omService.createDirectOrdre(req.om, req.mandatId, req.personnelId, req.etapeId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrdre(@PathVariable Long id, @RequestBody OrdreDeMission details) {
        try {
            OrdreDeMission updated = omService.updateOrdre(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-scan")
    public ResponseEntity<?> uploadSignedScan(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String scanPath = payload.get("scanPath");
            if (scanPath == null || scanPath.isBlank()) scanPath = "/uploads/scans/om_" + id + "_signed.pdf";
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
