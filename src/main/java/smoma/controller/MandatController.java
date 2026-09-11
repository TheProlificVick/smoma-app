package smoma.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.EtapeMission;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.MandatService;
import smoma.controller.model.Service.PdfGeneratorService;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mandats")
public class MandatController {

    private final MandatService mandatService;
    private final AccessPolicy accessPolicy;
    private final PdfGeneratorService pdfService;

    public MandatController(MandatService mandatService, AccessPolicy accessPolicy, PdfGeneratorService pdfService) {
        this.mandatService = mandatService;
        this.accessPolicy = accessPolicy;
        this.pdfService = pdfService;
    }

    @GetMapping
    public ResponseEntity<List<MandatDeMission>> getAllMandats() {
        return ResponseEntity.ok(mandatService.getAllMandats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MandatDeMission> getMandatById(@PathVariable Long id) {
        return ResponseEntity.ok(mandatService.getMandatById(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Long id) {
        MandatDeMission mandat = mandatService.getMandatById(id);
        ByteArrayInputStream pdf = pdfService.generateMandatDeMissionPdf(mandat);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="
                + (mandat.getReferenceMandat() != null ? mandat.getReferenceMandat() : ("mandat-" + id)) + ".pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }

    public static class MandatCreateRequest {
        public MandatDeMission mandat;
        public List<Long> personnelIds;
        public List<EtapeMission> etapes;
    }

    @PostMapping
    public ResponseEntity<?> createMandat(@RequestBody MandatCreateRequest request,
                                          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canInitiateMandat(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMandatRule()));
        }
        if (request == null || request.mandat == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Corps de requête invalide: le mandat est absent."));
        }
        // IllegalArgumentException -> 400, any other error -> 500 with a readable JSON body,
        // both handled centrally by GlobalExceptionHandler.
        MandatDeMission created = mandatService.createMandat(request.mandat, request.personnelIds, request.etapes);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMandat(@PathVariable Long id,
                                          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canDeleteMandat(user)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMandatDeleteRule()));
        }
        mandatService.deleteMandat(id, userEmail);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id));
    }

    @PostMapping("/{id}/upload-scan")
    public ResponseEntity<?> uploadSignedScan(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String scanPath = payload.get("scanPath");
            if (scanPath == null || scanPath.isBlank()) scanPath = "/uploads/scans/mandat_" + id + "_signed.pdf";
            MandatDeMission updated = mandatService.uploadSignedScan(id, scanPath);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
