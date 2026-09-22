package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.*;
import smoma.controller.model.Service.AccessPolicy;
import smoma.dto.FonctionRequest;
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

    /**
     * Without a filter, returns the entire fonction catalogue (used by the admin Référentiels
     * screen). Pass departmentId (preferred) or departmentName to get only the postes that
     * actually exist within that directorate/structure in the organigramme — this is what the
     * Personnel form uses so a fonction can only be picked once its department is chosen, and its
     * linked rang can be auto-filled (see Fonction.department / Fonction.rang).
     */
    @GetMapping("/fonctions")
    public ResponseEntity<List<Fonction>> getFonctions(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String departmentName) {
        if (departmentId != null) {
            return ResponseEntity.ok(fonctionRepository.findByDepartment_Id(departmentId));
        }
        if (departmentName != null && !departmentName.isBlank()) {
            return ResponseEntity.ok(fonctionRepository.findByDepartment_NameIgnoreCase(departmentName));
        }
        return ResponseEntity.ok(fonctionRepository.findAll());
    }

    private Fonction applyFonctionRequest(Fonction target, FonctionRequest req) {
        if (req.getCode() != null && !req.getCode().isBlank()) {
            fonctionRepository.findByCode(req.getCode())
                    .filter(existing -> target.getId() == null || !existing.getId().equals(target.getId()))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Une fonction avec le code « " + req.getCode() + " » existe déjà.");
                    });
        }
        if (req.getLibelle() != null) target.setLibelle(req.getLibelle());
        if (req.getLibelleEn() != null) target.setLibelleEn(req.getLibelleEn());
        if (req.getCode() != null) target.setCode(req.getCode());
        if (req.getDescription() != null) target.setDescription(req.getDescription());
        target.setActif(req.isActif());
        if (req.getDepartmentId() != null) {
            target.setDepartment(departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Direction/structure introuvable: " + req.getDepartmentId())));
        }
        if (req.getRangId() != null) {
            target.setRang(rangRepository.findById(req.getRangId())
                    .orElseThrow(() -> new IllegalArgumentException("Rang introuvable: " + req.getRangId())));
        }
        return target;
    }

    @PostMapping("/fonctions")
    public ResponseEntity<?> createFonction(@RequestBody FonctionRequest req,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        Fonction f = applyFonctionRequest(new Fonction(), req);
        return ResponseEntity.ok(fonctionRepository.save(f));
    }

    @PutMapping("/fonctions/{id}")
    public ResponseEntity<?> updateFonction(@PathVariable Long id, @RequestBody FonctionRequest req,
                                            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        Fonction existing = fonctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fonction introuvable: " + id));
        return ResponseEntity.ok(fonctionRepository.save(applyFonctionRequest(existing, req)));
    }

    @GetMapping("/rangs")
    public ResponseEntity<List<Rang>> getRangs() {
        return ResponseEntity.ok(rangRepository.findAll());
    }

    private void assertRangCodeAvailable(String code, Long selfId) {
        if (code == null || code.isBlank()) return;
        rangRepository.findByCode(code)
                .filter(existing -> selfId == null || !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Un rang avec le code « " + code + " » existe déjà.");
                });
    }

    @PostMapping("/rangs")
    public ResponseEntity<?> createRang(@RequestBody Rang r,
                                        @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        assertRangCodeAvailable(r.getCode(), null);
        return ResponseEntity.ok(rangRepository.save(r));
    }

    @PutMapping("/rangs/{id}")
    public ResponseEntity<?> updateRang(@PathVariable Long id, @RequestBody Rang r,
                                        @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        ResponseEntity<Map<String, String>> denied = denyUnlessOrgManager(userEmail);
        if (denied != null) return denied;
        Rang existing = rangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rang introuvable: " + id));
        assertRangCodeAvailable(r.getCode(), id);
        if (r.getLibelle() != null) existing.setLibelle(r.getLibelle());
        if (r.getLibelleEn() != null) existing.setLibelleEn(r.getLibelleEn());
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
