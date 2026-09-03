package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RapportMissionService {

    private final RapportMissionRepository rapportRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final AuditLogRepository auditLogRepository;

    public RapportMissionService(RapportMissionRepository rapportRepository,
                                 OrdreDeMissionRepository ordreRepository,
                                 AuditLogRepository auditLogRepository) {
        this.rapportRepository = rapportRepository;
        this.ordreRepository = ordreRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public RapportMission depositReport(Long omId, String titre, String description, String categorie, String fichierPath, String justificatifsJson) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));

        RapportMission rapport = new RapportMission();
        rapport.setOrdreDeMission(om);
        rapport.setPersonnel(om.getPersonnel());
        rapport.setTitre(titre != null ? titre : "Rapport de mission " + om.getReferenceOrdre());
        rapport.setDescription(description);
        rapport.setCategorie(categorie);
        rapport.setFichierPath(fichierPath);
        rapport.setJustificatifsJson(justificatifsJson);
        rapport.setDateDepot(LocalDate.now());
        rapport.setStatutValidation("EN_ATTENTE");

        om.setRapportScannePath(fichierPath);
        om.setRapportSoumis(true);
        ordreRepository.save(om);

        RapportMission saved = rapportRepository.save(rapport);
        auditLogRepository.save(new AuditLog("DEPOSIT_REPORT", "SYSTEM", "Rapport de mission depose pour OM #" + omId));
        return saved;
    }

    @Transactional
    public RapportMission validateReport(Long rapportId) {
        RapportMission rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable: " + rapportId));

        rapport.setStatutValidation("VALIDE");
        auditLogRepository.save(new AuditLog("VALIDATE_REPORT", "SYSTEM", "Rapport de mission valide ID: " + rapportId));
        return rapportRepository.save(rapport);
    }

    public List<RapportMission> searchReports(String query, String categorie, Long personnelId) {
        List<RapportMission> all = rapportRepository.findAll();
        return all.stream()
                .filter(r -> categorie == null || categorie.isBlank() || (r.getCategorie() != null && r.getCategorie().name().equalsIgnoreCase(categorie)))
                .filter(r -> personnelId == null || (r.getPersonnel() != null && r.getPersonnel().getId().equals(personnelId)))
                .filter(r -> query == null || query.isBlank() || (r.getTitre() != null && r.getTitre().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<RapportMission> getAllReports() {
        return rapportRepository.findAll();
    }
}
