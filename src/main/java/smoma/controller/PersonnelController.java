package smoma.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Personnel;
import smoma.controller.model.Service.PersonnelService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {

    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    @GetMapping
    public ResponseEntity<List<Personnel>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String structure,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(personnelService.searchStaff(query, structure, grade, statut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personnel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personnelService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Personnel> save(@RequestBody Personnel p) {
        return ResponseEntity.ok(personnelService.save(p));
    }

    @GetMapping("/{id}/compte-individuel")
    public ResponseEntity<Map<String, Object>> getCompteIndividuel(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(personnelService.getCompteIndividuel(id, startDate, endDate));
    }
}
