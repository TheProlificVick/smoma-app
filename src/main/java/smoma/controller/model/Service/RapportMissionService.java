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
    private final NotificationService notificationService;

    public RapportMissionService(RapportMissionRepository rapportRepository,
                                 OrdreDeMissionRepository ordreRepository,
                                 AuditLogRepository auditLogRepository,
                                 NotificationService notificationService) {
        this.rapportRepository = rapportRepository;
        this.ordreRepository = ordreRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public RapportMission depositReport(Long omId, String titre, String description, String categorie, String fichierPath, String justificatifsJson) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));

        // One report per order (RapportMission.ordreDeMission is a unique OneToOne): a rejected
        // report is corrected and re-deposited in place rather than creating a second row that
        // would violate that constraint.
        RapportMission rapport = rapportRepository.findByOrdreDeMission(om).orElseGet(RapportMission::new);
        rapport.setOrdreDeMission(om);
        rapport.setPersonnel(om.getPersonnel());
        rapport.setTitre(titre != null ? titre : "Rapport de mission " + om.getReferenceOrdre());
        rapport.setDescription(description);
        rapport.setCategorie(categorie);
        rapport.setFichierPath(fichierPath);
        rapport.setJustificatifsJson(justificatifsJson);
        rapport.setDateDepot(LocalDate.now());
        rapport.setStatutValidation("EN_ATTENTE");
        rapport.setStatut(RapportMission.StatutRapport.DEPOSE);
        rapport.setMotifRejet(null);

        om.setRapportScannePath(fichierPath);
        om.setRapportSoumis(true);
        ordreRepository.save(om);

        RapportMission saved = rapportRepository.save(rapport);
        auditLogRepository.save(new AuditLog("DEPOSIT_REPORT", "SYSTEM", "Rapport de mission depose pour OM #" + omId));
        return saved;
    }

    @Transactional
    public RapportMission validateReport(Long rapportId, String validatorIdentity) {
        RapportMission rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable: " + rapportId));

        rapport.setStatutValidation("VALIDE");
        rapport.setStatut(RapportMission.StatutRapport.VALIDE);
        rapport.setMotifRejet(null);
        rapport.setDateValidation(LocalDate.now());
        rapport.setValidateurUsername(validatorIdentity);
        auditLogRepository.save(new AuditLog("VALIDATE_REPORT", "SYSTEM", "Rapport de mission valide ID: " + rapportId));
        RapportMission saved = rapportRepository.save(rapport);
        notificationService.notifyReportValidated(rapport.getOrdreDeMission());
        return saved;
    }

    /**
     * Sends the report back to the agent with a justification instead of validating it — the
     * agent is expected to correct and re-submit. Rejecting a report also clears
     * {@code OrdreDeMission.rapportSoumis} so the "submit report" flow re-opens for them.
     */
    @Transactional
    public RapportMission rejectReport(Long rapportId, String motif, String validatorIdentity) {
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("La justification du rejet est obligatoire.");
        }
        RapportMission rapport = rapportRepository.findById(rapportId)
                .orElseThrow(() -> new IllegalArgumentException("Rapport introuvable: " + rapportId));

        rapport.setStatutValidation("REJETE");
        // StatutRapport has no REJETE value — it tracks submission state (has a report been filed
        // at all), a different axis from statutValidation's review outcome. A rejected report is
        // still a deposited one (the file exists, DEPOSE), just not validated.
        rapport.setStatut(RapportMission.StatutRapport.DEPOSE);
        rapport.setMotifRejet(motif.trim());
        rapport.setDateValidation(LocalDate.now());
        rapport.setValidateurUsername(validatorIdentity);
        RapportMission saved = rapportRepository.save(rapport);

        OrdreDeMission om = rapport.getOrdreDeMission();
        if (om != null) {
            om.setRapportSoumis(false);
            ordreRepository.save(om);
        }

        auditLogRepository.save(new AuditLog("REJECT_REPORT", "SYSTEM",
                "Rapport de mission rejete ID: " + rapportId + " — motif: " + motif.trim()));
        notificationService.notifyReportRejected(rapport.getOrdreDeMission(), motif.trim());
        return saved;
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
