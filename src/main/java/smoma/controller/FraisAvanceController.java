package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.AvanceSurFrais;
import smoma.controller.model.Service.IndemniteService;

import java.util.List;

/**
 * Read-only: advances and balances are now managed entirely by the Direction des Finances in its
 * own software, so this endpoint only surfaces the (possibly historical) {@link AvanceSurFrais}
 * records for display on the mission-payment tracker — it never creates or approves a payment.
 */
@RestController
@RequestMapping("/api/frais-avances")
public class FraisAvanceController {

    private final IndemniteService indemniteService;

    public FraisAvanceController(IndemniteService indemniteService) {
        this.indemniteService = indemniteService;
    }

    @GetMapping
    public ResponseEntity<List<AvanceSurFrais>> getAllAvances() {
        return ResponseEntity.ok(indemniteService.getAllAvances());
    }
}
