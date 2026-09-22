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

    /**
     * Whoever manages mandates (DRH/admin, or an agent entitled to initiate one) gets the full
     * roster, same as before; anyone else only gets back mandates they are personally part of —
     * a mandate is a fully approved record by the time it exists (printed, DG-signed, scanned
     * back in), so the agents on it should be able to consult it, just not everyone else's.
     */
    @GetMapping
    public ResponseEntity<List<MandatDeMission>> getAllMandats(
            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        List<MandatDeMission> all = mandatService.getAllMandats();
        User actor = accessPolicy.resolve(requestEmail);
        if (accessPolicy.isAdmin(actor) || accessPolicy.isHrOfficer(actor) || accessPolicy.canInitiateMandat(actor)) {
            return ResponseEntity.ok(all);
        }
        List<MandatDeMission> own = all.stream()
                .filter(m -> accessPolicy.canViewMandat(actor, m))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(own);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMandatById(@PathVariable Long id,
                                           @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        MandatDeMission mandat = mandatService.getMandatById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewMandat(actor, mandat)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMandatViewRule()));
        }
        return ResponseEntity.ok(mandat);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> downloadPdf(@PathVariable Long id,
                                         @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        MandatDeMission mandat = mandatService.getMandatById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewMandat(actor, mandat)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMandatViewRule()));
        }
        ByteArrayInputStream pdf = pdfService.generateMandatDeMissionPdf(mandat);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename="
                + (mandat.getReferenceMandat() != null ? mandat.getReferenceMandat() : ("mandat-" + id)) + ".pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }

    /**
     * Marked by the client when it actually triggers printing or a PDF download of the official
     * document — whoever may view the mandate may print it (same population as canViewMandat).
     */
    @PostMapping("/{id}/mark-printed")
    public ResponseEntity<?> markPrinted(@PathVariable Long id,
                                         @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        MandatDeMission mandat = mandatService.getMandatById(id);
        User actor = accessPolicy.resolve(requestEmail);
        if (!accessPolicy.canViewMandat(actor, mandat)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeMandatViewRule()));
        }
        MandatDeMission updated = mandatService.markPrinted(id, actor != null ? actor.getUsername() : requestEmail);
        return ResponseEntity.ok(updated);
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
