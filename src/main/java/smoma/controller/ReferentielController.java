package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.*;
import smoma.controller.model.Service.AccessPolicy;
import smoma.repository.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referentiels")
public class ReferentielController {

    private final DepartmentRepository departmentRepository;
    private final FonctionRepository fonctionRepository;
    private final RangRepository rangRepository;
    private final MotifReglementaireRepository motifRepository;
    private final BaremeIndemniteRepository baremeRepository;
    private final CompanySettingsRepository settingsRepository;
    private final AccessPolicy accessPolicy;

    public ReferentielController(DepartmentRepository departmentRepository,
                                 FonctionRepository fonctionRepository,
                                 RangRepository rangRepository,
                                 MotifReglementaireRepository motifRepository,
                                 BaremeIndemniteRepository baremeRepository,
                                 CompanySettingsRepository settingsRepository,
                                 AccessPolicy accessPolicy) {
        this.departmentRepository = departmentRepository;
        this.fonctionRepository = fonctionRepository;
        this.rangRepository = rangRepository;
        this.motifRepository = motifRepository;
        this.baremeRepository = baremeRepository;
        this.settingsRepository = settingsRepository;
        this.accessPolicy = accessPolicy;
    }

    private ResponseEntity<Map<String, String>> denyUnlessOrgManager(String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.canCreateOrgEntities(user)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seuls l'administrateur système et le personnel DRH peuvent modifier les référentiels."));
        }
        return null;
    }

    /** The mission indemnity rate table (tariffs by rank) is administrator-only, unlike the other referentials. */
    private ResponseEntity<Map<String, String>> denyUnlessAdmin(String userEmail) {
        User user = accessPolicy.resolve(userEmail);
        if (!accessPolicy.isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seul l'administrateur système peut modifier la grille tarifaire des indemnités de mission."));
        }
        return null;
    }

    @GetMapping("/structures")
    public ResponseEntity<List<Department>> getStructures() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/structures")
    public ResponseEntity<?> createStructure(@RequestBody Department dept,
                                             @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @GetMapping("/fonctions")
    public ResponseEntity<List<Fonction>> getFonctions() {
        return ResponseEntity.ok(fonctionRepository.findAll());
    }

    @PostMapping("/fonctions")
    public ResponseEntity<?> createFonction(@RequestBody Fonction f,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        return ResponseEntity.ok(fonctionRepository.save(f));
    }

    @PutMapping("/fonctions/{id}")
    public ResponseEntity<?> updateFonction(@PathVariable Long id, @RequestBody Fonction f,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        Fonction existing = fonctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fonction introuvable: " + id));
        if (f.getLibelle() != null) existing.setLibelle(f.getLibelle());
        if (f.getCode() != null) existing.setCode(f.getCode());
        if (f.getDescription() != null) existing.setDescription(f.getDescription());
        existing.setActif(f.isActif());
        return ResponseEntity.ok(fonctionRepository.save(existing));
    }

    @GetMapping("/rangs")
    public ResponseEntity<List<Rang>> getRangs() {
        return ResponseEntity.ok(rangRepository.findAll());
    }

    @PostMapping("/rangs")
    public ResponseEntity<?> createRang(@RequestBody Rang r,
                                        @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        return ResponseEntity.ok(rangRepository.save(r));
    }

    @PutMapping("/rangs/{id}")
    public ResponseEntity<?> updateRang(@PathVariable Long id, @RequestBody Rang r,
                                        @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        Rang existing = rangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rang introuvable: " + id));
        if (r.getLibelle() != null) existing.setLibelle(r.getLibelle());
        if (r.getCode() != null) existing.setCode(r.getCode());
        if (r.getNiveau() != null) existing.setNiveau(r.getNiveau());
        existing.setActif(r.isActif());
        return ResponseEntity.ok(rangRepository.save(existing));
    }

    @GetMapping("/motifs")
    public ResponseEntity<List<MotifReglementaire>> getMotifs() {
        return ResponseEntity.ok(motifRepository.findAll());
    }

    @PostMapping("/motifs")
    public ResponseEntity<?> createMotif(@RequestBody MotifReglementaire m,
                                         @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        return ResponseEntity.ok(motifRepository.save(m));
    }

    @GetMapping("/baremes")
    public ResponseEntity<List<BaremeIndemnite>> getBaremes() {
        return ResponseEntity.ok(baremeRepository.findAll());
    }

    @PostMapping("/baremes")
    public ResponseEntity<?> createBareme(@RequestBody BaremeIndemnite b,
                                          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessAdmin(userEmail);
        if (denied != null) return denied;
        if (b.getMontantJournalier() == null || b.getMontantJournalier().signum() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le montant journalier doit être un nombre positif."));
        }
        return ResponseEntity.ok(baremeRepository.save(b));
    }

    /** Edits an existing daily rate — e.g. after a personnel rank rise/fall, or a rate revision. */
    @PutMapping("/baremes/{id}")
    public ResponseEntity<?> updateBareme(@PathVariable Long id, @RequestBody BaremeIndemnite b,
                                          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessAdmin(userEmail);
        if (denied != null) return denied;
        if (b.getMontantJournalier() == null || b.getMontantJournalier().signum() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le montant journalier doit être un nombre positif."));
        }
        BaremeIndemnite existing = baremeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Barème introuvable: " + id));
        existing.setRang(b.getRang());
        existing.setGrade(b.getGrade());
        existing.setFonction(b.getFonction());
        existing.setTypeMission(b.getTypeMission());
        existing.setMontantJournalier(b.getMontantJournalier());
        existing.setMontantForfaitaire(b.getMontantForfaitaire());
        existing.setEstActif(b.isEstActif());
        return ResponseEntity.ok(baremeRepository.save(existing));
    }

    @DeleteMapping("/baremes/{id}")
    public ResponseEntity<?> deleteBareme(@PathVariable Long id,
                                          @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessAdmin(userEmail);
        if (denied != null) return denied;
        baremeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id));
    }

    @GetMapping("/settings")
    public ResponseEntity<CompanySettings> getSettings() {
        CompanySettings s = settingsRepository.findAll().stream().findFirst().orElseGet(() -> settingsRepository.save(new CompanySettings()));
        return ResponseEntity.ok(s);
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody CompanySettings newSettings,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        CompanySettings s = settingsRepository.findAll().stream().findFirst().orElseGet(CompanySettings::new);
        if (newSettings.getCompanyName() != null) s.setCompanyName(newSettings.getCompanyName());
        if (newSettings.getLogoPath() != null) s.setLogoPath(newSettings.getLogoPath());
        if (newSettings.getDelegation() != null) s.setDelegation(newSettings.getDelegation());
        return ResponseEntity.ok(settingsRepository.save(s));
    }
}
