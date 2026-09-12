package smoma.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.FileStorageService;
import smoma.controller.model.Service.MandatService;
import smoma.controller.model.Service.PdfGeneratorService;
import smoma.dto.EtapeStepRequest;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mandats")
public class MandatController {

    private final MandatService mandatService;
    private final AccessPolicy accessPolicy;
    private final PdfGeneratorService pdfService;
    private final FileStorageService fileStorageService;

    public MandatController(MandatService mandatService, AccessPolicy accessPolicy, PdfGeneratorService pdfService,
                            FileStorageService fileStorageService) {
        this.mandatService = mandatService;
        this.accessPolicy = accessPolicy;
        this.pdfService = pdfService;
        this.fileStorageService = fileStorageService;
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
        public List<EtapeStepRequest> etapes;
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
        MandatDeMission created = mandatService.createMandat(request.mandat, request.personnelIds, request.etapes, userEmail);
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

    /** Imports the GM-signed mandate: the physical mandate, scanned back in, PDF or photo. */
    @PostMapping(value = "/{id}/upload-scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSignedScan(@PathVariable Long id, @RequestParam("file") MultipartFile file,
                                              @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canUploadMandatScan(user)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seuls l'administrateur, la DRH ou un agent habilité à initier un mandat peuvent importer le scan signé."));
        }
        try {
            String scanPath = fileStorageService.store(file, "scans", "mandat_" + id + "_signed");
            MandatDeMission updated = mandatService.uploadSignedScan(id, scanPath);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
