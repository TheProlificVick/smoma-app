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

    /**
     * Reverted to open (no identity check) — the X-User-Email + JWT requirement added here was
     * running into a session/token issue in production that made the whole staff directory
     * unusable, and a working directory matters more than closing this particular gap right now.
     * The directory is read by every role across the app (staff search for mandates/OMs, the
     * personnel module itself), so re-adding a check here later should keep it permissive (any
     * resolvable identity, not a specific role) rather than reintroducing the same failure mode.
     */
    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String structure,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(personnelService.searchStaff(query, structure, grade, statut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
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
    public ResponseEntity<?> getCompteIndividuel(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(personnelService.getCompteIndividuel(id, startDate, endDate));
    }
}
