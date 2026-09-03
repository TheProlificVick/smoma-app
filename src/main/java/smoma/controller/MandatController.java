package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.EtapeMission;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.Service.MandatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mandats")
public class MandatController {

    private final MandatService mandatService;

    public MandatController(MandatService mandatService) {
        this.mandatService = mandatService;
    }

    @GetMapping
    public ResponseEntity<List<MandatDeMission>> getAllMandats() {
        return ResponseEntity.ok(mandatService.getAllMandats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MandatDeMission> getMandatById(@PathVariable Long id) {
        return ResponseEntity.ok(mandatService.getMandatById(id));
    }

    public static class MandatCreateRequest {
        public MandatDeMission mandat;
        public List<Long> personnelIds;
        public List<EtapeMission> etapes;
    }

    @PostMapping
    public ResponseEntity<?> createMandat(@RequestBody MandatCreateRequest request) {
        try {
            MandatDeMission created = mandatService.createMandat(request.mandat, request.personnelIds, request.etapes);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
