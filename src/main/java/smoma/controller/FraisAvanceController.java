package smoma.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.AvanceSurFrais;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.IndemniteService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/frais-avances")
public class FraisAvanceController {

    private final IndemniteService indemniteService;
    private final AccessPolicy accessPolicy;

    public FraisAvanceController(IndemniteService indemniteService, AccessPolicy accessPolicy) {
        this.indemniteService = indemniteService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    public ResponseEntity<List<AvanceSurFrais>> getAllAvances() {
        return ResponseEntity.ok(indemniteService.getAllAvances());
    }

    public static class AdvanceRequestPayload {
        public Long omId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate dateDemande;
    }

    public static class ApprovalPayload {
        /** "ESPECES" (retrait au guichet) or "VIREMENT" (virement bancaire). */
        public String modePaiement;
        public String referenceVirement;
    }

    @PostMapping("/demander-avance")
    public ResponseEntity<?> requestAdvance(@RequestBody AdvanceRequestPayload payload) {
        try {
            AvanceSurFrais avance = indemniteService.requestAdvance(payload.omId, payload.dateDemande);
            return ResponseEntity.ok(avance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/valider-avance/{id}")
    public ResponseEntity<?> validateAdvance(@PathVariable Long id,
                                             @RequestBody(required = false) ApprovalPayload payload,
                                             @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User actor = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canApproveAdvance(actor)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeAdvanceApprovalRule()));
        }
        try {
            String mode = payload != null ? payload.modePaiement : null;
            String ref = payload != null ? payload.referenceVirement : null;
            AvanceSurFrais avance = indemniteService.validateAdvance(id, mode, ref);
            return ResponseEntity.ok(avance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verser-solde/{id}")
    public ResponseEntity<?> payBalance(@PathVariable Long id,
                                        @RequestBody(required = false) ApprovalPayload payload,
                                        @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User actor = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canApproveAdvance(actor)) {
            return ResponseEntity.status(403).body(Map.of("error", accessPolicy.describeAdvanceApprovalRule()));
        }
        try {
            String mode = payload != null ? payload.modePaiement : null;
            String ref = payload != null ? payload.referenceVirement : null;
            AvanceSurFrais avance = indemniteService.verserSolde(id, mode, ref);
            return ResponseEntity.ok(avance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
