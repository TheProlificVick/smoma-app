package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.AnalyseAnnuelleIaService;

import java.util.Map;

/**
 * AI analysis block of the annual report (Bilan Annuel): a written analysis of all mission mandates and
 * mission orders, cross-checked against the figures the report displays. Read-only, same audience as the
 * rest of the annual dashboard.
 */
@RestController
@RequestMapping("/api/statistiques")
public class AnalyseAnnuelleIaController {

    private final AnalyseAnnuelleIaService analyseService;

    public AnalyseAnnuelleIaController(AnalyseAnnuelleIaService analyseService) {
        this.analyseService = analyseService;
    }

    @GetMapping("/analyse-ia")
    public ResponseEntity<Map<String, Object>> analyseIa(@RequestParam(required = false) Integer annee,
                                                         @RequestParam(required = false, defaultValue = "fr") String lang) {
        return ResponseEntity.ok(analyseService.analyse(annee, lang));
    }
}
