package smoma.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Personnel;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.PersonnelService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {

    private final PersonnelService personnelService;
    private final AccessPolicy accessPolicy;

    public PersonnelController(PersonnelService personnelService, AccessPolicy accessPolicy) {
        this.personnelService = personnelService;
        this.accessPolicy = accessPolicy;
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
    public ResponseEntity<?> save(@RequestBody Personnel p,
                                  @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canCreateOrgEntities(user)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seuls l'administrateur système et le personnel DRH peuvent créer ou modifier une fiche de personnel."));
        }
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
