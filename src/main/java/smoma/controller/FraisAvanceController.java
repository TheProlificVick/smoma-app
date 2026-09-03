package smoma.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.AvanceSurFrais;
import smoma.controller.model.Service.IndemniteService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/frais-avances")
public class FraisAvanceController {

    private final IndemniteService indemniteService;

    public FraisAvanceController(IndemniteService indemniteService) {
        this.indemniteService = indemniteService;
    }

    public static class AdvanceRequestPayload {
        public Long omId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate dateDemande;
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
    public ResponseEntity<?> validateAdvance(@PathVariable Long id) {
        try {
            AvanceSurFrais avance = indemniteService.validateAdvance(id);
            return ResponseEntity.ok(avance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
