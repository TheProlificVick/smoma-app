package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.*;
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

    public ReferentielController(DepartmentRepository departmentRepository,
                                 FonctionRepository fonctionRepository,
                                 RangRepository rangRepository,
                                 MotifReglementaireRepository motifRepository,
                                 BaremeIndemniteRepository baremeRepository,
                                 CompanySettingsRepository settingsRepository) {
        this.departmentRepository = departmentRepository;
        this.fonctionRepository = fonctionRepository;
        this.rangRepository = rangRepository;
        this.motifRepository = motifRepository;
        this.baremeRepository = baremeRepository;
        this.settingsRepository = settingsRepository;
    }

    @GetMapping("/structures")
    public ResponseEntity<List<Department>> getStructures() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/structures")
    public ResponseEntity<Department> createStructure(@RequestBody Department dept) {
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @GetMapping("/fonctions")
    public ResponseEntity<List<Fonction>> getFonctions() {
        return ResponseEntity.ok(fonctionRepository.findAll());
    }

    @PostMapping("/fonctions")
    public ResponseEntity<Fonction> createFonction(@RequestBody Fonction f) {
        return ResponseEntity.ok(fonctionRepository.save(f));
    }

    @GetMapping("/rangs")
    public ResponseEntity<List<Rang>> getRangs() {
        return ResponseEntity.ok(rangRepository.findAll());
    }

    @PostMapping("/rangs")
    public ResponseEntity<Rang> createRang(@RequestBody Rang r) {
        return ResponseEntity.ok(rangRepository.save(r));
    }

    @GetMapping("/motifs")
    public ResponseEntity<List<MotifReglementaire>> getMotifs() {
        return ResponseEntity.ok(motifRepository.findAll());
    }

    @PostMapping("/motifs")
    public ResponseEntity<MotifReglementaire> createMotif(@RequestBody MotifReglementaire m) {
        return ResponseEntity.ok(motifRepository.save(m));
    }

    @GetMapping("/baremes")
    public ResponseEntity<List<BaremeIndemnite>> getBaremes() {
        return ResponseEntity.ok(baremeRepository.findAll());
    }

    @PostMapping("/baremes")
    public ResponseEntity<BaremeIndemnite> createBareme(@RequestBody BaremeIndemnite b) {
        return ResponseEntity.ok(baremeRepository.save(b));
    }

    @GetMapping("/settings")
    public ResponseEntity<CompanySettings> getSettings() {
        CompanySettings s = settingsRepository.findAll().stream().findFirst().orElseGet(() -> settingsRepository.save(new CompanySettings()));
        return ResponseEntity.ok(s);
    }

    @PostMapping("/settings")
    public ResponseEntity<CompanySettings> updateSettings(@RequestBody CompanySettings newSettings) {
        CompanySettings s = settingsRepository.findAll().stream().findFirst().orElseGet(CompanySettings::new);
        if (newSettings.getCompanyName() != null) s.setCompanyName(newSettings.getCompanyName());
        if (newSettings.getLogoPath() != null) s.setLogoPath(newSettings.getLogoPath());
        if (newSettings.getDelegation() != null) s.setDelegation(newSettings.getDelegation());
        return ResponseEntity.ok(settingsRepository.save(s));
    }
}
