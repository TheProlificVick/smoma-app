package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrdreDeMissionService {

    private final OrdreDeMissionRepository ordreRepository;
    private final MandatDeMissionRepository mandatRepository;
    private final PersonnelRepository personnelRepository;
    private final EtapeMissionRepository etapeRepository;
    private final AuditLogRepository auditLogRepository;
    private final IndemniteService indemniteService;
    private final NotificationService notificationService;
    private final MissionCapacityService missionCapacityService;

    public OrdreDeMissionService(OrdreDeMissionRepository ordreRepository,
                                 MandatDeMissionRepository mandatRepository,
                                 PersonnelRepository personnelRepository,
                                 EtapeMissionRepository etapeRepository,
                                 AuditLogRepository auditLogRepository,
                                 IndemniteService indemniteService,
                                 NotificationService notificationService,
                                 MissionCapacityService missionCapacityService) {
        this.ordreRepository = ordreRepository;
        this.mandatRepository = mandatRepository;
        this.personnelRepository = personnelRepository;
        this.etapeRepository = etapeRepository;
        this.auditLogRepository = auditLogRepository;
        this.indemniteService = indemniteService;
        this.notificationService = notificationService;
        this.missionCapacityService = missionCapacityService;
    }

    /** Kept for backward compatibility; delegates to {@link MissionCapacityService}. */
    public void checkAgentOverlap(Long personnelId, LocalDate dateDebut, LocalDate dateFin, Long excludeOmId) {
        if (personnelId == null || dateDebut == null || dateFin == null) return;
        Personnel agent = personnelRepository.findById(personnelId).orElse(null);
        missionCapacityService.assertNoOverlap(agent, dateDebut, dateFin, excludeOmId);
    }

    @Transactional
    public OrdreDeMission createDirectOrdre(OrdreDeMission om, Long mandatId, Long personnelId, Long etapeId, List<EtapeMission> etapes) {
        if (om.getDateDebut() != null && om.getDateFin() != null && om.getDateFin().isBefore(om.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }
        if (mandatId != null) {
            MandatDeMission mandat = mandatRepository.findById(mandatId)
                    .orElseThrow(() -> new IllegalArgumentException("Mandat rattache introuvable: " + mandatId));
            om.setMandatDeMission(mandat);
            // Inherit the initiating directorate and the authorising-act reference from the mandate
            // when the direct-OM form did not set them explicitly.
            if (om.getDirectionInitiatrice() == null || om.getDirectionInitiatrice().isBlank()) {
                om.setDirectionInitiatrice(mandat.getDirectionInitiatrice());
            }
            if (om.getReferenceJustification() == null || om.getReferenceJustification().isBlank()) {
                om.setReferenceJustification(mandat.getReferenceJustification());
            }
            if (om.getLieuDepart() == null || om.getLieuDepart().isBlank()) {
                om.setLieuDepart(mandat.getVilleDepart());
            }
            if (om.getLieuDestination() == null || om.getLieuDestination().isBlank()) {
                om.setLieuDestination(mandat.getDestination());
            }
        } else {
            throw new IllegalArgumentException("Tout ordre de mission doit être obligatoirement rattaché à un mandat de mission existant.");
        }

        if (personnelId != null) {
            Personnel agent = personnelRepository.findById(personnelId)
                    .orElseThrow(() -> new IllegalArgumentException("Agent introuvable: " + personnelId));
            om.setPersonnel(agent);
        }

        if (etapeId != null) {
            EtapeMission etape = etapeRepository.findById(etapeId).orElse(null);
            om.setEtape(etape);
        }

        // One step at a time + 100-day/fiscal-year cap, regardless of rank.
        missionCapacityService.assertAssignable(om.getPersonnel(), om.getDateDebut(), om.getDateFin(), null);

        if (om.getReferenceOrdre() == null || om.getReferenceOrdre().isBlank()) {
            String matricule = om.getPersonnel() != null ? om.getPersonnel().getMatricule() : "AGENT";
            om.setReferenceOrdre("OM-ART-" + matricule + "-" + System.currentTimeMillis() % 10000);
        }

        om.setDateEmission(LocalDate.now());
        om.setStatut(OrdreDeMission.StatutOrdre.BROUILLON_MODIFIABLE);

        // Automatic indemnity computation from the official barème (spec 4.6).
        java.math.BigDecimal indemnite = om.isSansFrais()
                ? java.math.BigDecimal.ZERO
                : indemniteService.calculateTotalIndemnite(om);
        om.setMontantIndemnite(indemnite);
        if (om.getMontantAvance() == null) om.setMontantAvance(java.math.BigDecimal.ZERO);
        om.setMontantSolde(indemnite.subtract(om.getMontantAvance()));

        OrdreDeMission saved = ordreRepository.save(om);

        if (etapes != null && !etapes.isEmpty()) {
            for (EtapeMission st : etapes) {
                st.setOrdreDeMission(saved);
                if (saved.getMandatDeMission() != null) {
                    st.setMandatDeMission(saved.getMandatDeMission());
                }
                etapeRepository.save(st);
            }
        }

        auditLogRepository.save(new AuditLog("CREATE_DIRECT_OM", "SYSTEM", "Création direct OM: " + saved.getReferenceOrdre()));
        notificationService.notifyMissionAssigned(saved);
        return saved;
    }

    @Transactional
    public OrdreDeMission createDirectOrdre(OrdreDeMission om, Long mandatId, Long personnelId, Long etapeId) {
        return createDirectOrdre(om, mandatId, personnelId, etapeId, null);
    }

    @Transactional
    public OrdreDeMission uploadSignedScan(Long omId, String scanPath) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));

        om.setScanSignedPath(scanPath);
        om.setStatut(OrdreDeMission.StatutOrdre.SIGNE);
        OrdreDeMission updated = ordreRepository.save(om);

        auditLogRepository.save(new AuditLog("UPLOAD_OM_SCAN", "SYSTEM", "Scan signé importé (OM figé): " + om.getReferenceOrdre()));
        return updated;
    }

    @Transactional
    public OrdreDeMission updateOrdre(Long omId, OrdreDeMission updatedDetails) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));

        if (!om.isModifiable()) {
            throw new IllegalStateException("Modification impossible: cet ordre de mission est déjà signé et fige dans le système.");
        }

        missionCapacityService.assertAssignable(om.getPersonnel(), updatedDetails.getDateDebut(), updatedDetails.getDateFin(), omId);

        om.setObjectifsSpecifiques(updatedDetails.getObjectifsSpecifiques());
        om.setAvecFrais(updatedDetails.isAvecFrais());
        om.setSansFrais(updatedDetails.isSansFrais());
        om.setDateDebut(updatedDetails.getDateDebut());
        om.setDateFin(updatedDetails.getDateFin());
        if (updatedDetails.getTypeMission() != null) om.setTypeMission(updatedDetails.getTypeMission());
        if (updatedDetails.getMoyenTransport() != null) om.setMoyenTransport(updatedDetails.getMoyenTransport());

        // Recompute the indemnity so the financial figures stay consistent with the edited period/type.
        java.math.BigDecimal indemnite = om.isSansFrais()
                ? java.math.BigDecimal.ZERO
                : indemniteService.calculateTotalIndemnite(om);
        om.setMontantIndemnite(indemnite);
        if (om.getMontantAvance() == null) om.setMontantAvance(java.math.BigDecimal.ZERO);
        om.setMontantSolde(indemnite.subtract(om.getMontantAvance()));

        return ordreRepository.save(om);
    }

    /**
     * The assigned agent (or DRH/admin) checks a step off as done — freeing them up for a new
     * assignment immediately, instead of waiting for the originally planned dateFin. Idempotent
     * and irreversible by design: once checked off there's no "uncheck", matching the paper
     * process this mirrors (you don't un-finish a mission).
     */
    @Transactional
    public OrdreDeMission completeMissionStep(Long omId, String actor) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));
        if (om.isMissionTerminee()) return om;
        if (om.getDateDebut() != null && LocalDate.now().isBefore(om.getDateDebut())) {
            throw new IllegalStateException("Cette étape ne débute que le " + om.getDateDebut()
                    + " — elle ne peut pas être marquée terminée avant d'avoir commencé.");
        }
        om.setMissionTerminee(true);
        om.setDateFinReelle(LocalDate.now());
        OrdreDeMission saved = ordreRepository.save(om);
        auditLogRepository.save(new AuditLog("COMPLETE_MISSION_STEP",
                actor != null && !actor.isBlank() ? actor : "SYSTEM",
                "Étape marquée terminée par l'agent: " + om.getReferenceOrdre()
                        + (om.getPersonnel() != null ? " (" + om.getPersonnel().getFullName() + ")" : "")));
        return saved;
    }

    /**
     * Saves the verso ("page 2") fields — advance decompte, expense note, receipt acknowledgment —
     * unlike {@link #updateOrdre}, this is never blocked by {@code isModifiable()}: this data is
     * filled in as the mission actually happens (at departure, on return), which is routinely
     * *after* the order has already been DG-signed and locked for its core fields.
     */
    @Transactional
    public OrdreDeMission updateVersoDetails(Long omId, OrdreDeMission details) {
        OrdreDeMission om = ordreRepository.findById(omId)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + omId));

        om.setIndemniteReduiteNombre(details.getIndemniteReduiteNombre());
        om.setIndemniteReduiteTaux(details.getIndemniteReduiteTaux());
        om.setIndemniteReduiteDecompte(details.getIndemniteReduiteDecompte());
        om.setIndemnitePartielleNombre(details.getIndemnitePartielleNombre());
        om.setIndemnitePartielleTaux(details.getIndemnitePartielleTaux());
        om.setIndemnitePartielleDecompte(details.getIndemnitePartielleDecompte());
        om.setIndicationRequisitions(details.getIndicationRequisitions());
        om.setArreteSomme(details.getArreteSomme());
        om.setPayeSomme(details.getPayeSomme());
        om.setPayeeAvanceMontant(details.getPayeeAvanceMontant());
        om.setPayeeAvanceLieu(details.getPayeeAvanceLieu());
        om.setPayeeAvanceDate(details.getPayeeAvanceDate());
        om.setImputationBudgetaire(details.getImputationBudgetaire());
        om.setAcquitDepartRecu(details.getAcquitDepartRecu());
        om.setAcquitDepartCni(details.getAcquitDepartCni());
        om.setAcquitDepartLieu(details.getAcquitDepartLieu());
        om.setAcquitDepartDate(details.getAcquitDepartDate());
        om.setAcquitSoldeRecu(details.getAcquitSoldeRecu());

        return ordreRepository.save(om);
    }

    public List<OrdreDeMission> getAllOrdres() {
        return ordreRepository.findAll();
    }

    public OrdreDeMission getOrdreById(Long id) {
        return ordreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordre de mission introuvable: " + id));
    }
}
