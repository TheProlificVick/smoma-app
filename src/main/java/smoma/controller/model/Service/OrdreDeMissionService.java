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

    public OrdreDeMissionService(OrdreDeMissionRepository ordreRepository,
                                 MandatDeMissionRepository mandatRepository,
                                 PersonnelRepository personnelRepository,
                                 EtapeMissionRepository etapeRepository,
                                 AuditLogRepository auditLogRepository) {
        this.ordreRepository = ordreRepository;
        this.mandatRepository = mandatRepository;
        this.personnelRepository = personnelRepository;
        this.etapeRepository = etapeRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public void checkAgentOverlap(Long personnelId, LocalDate dateDebut, LocalDate dateFin, Long excludeOmId) {
        if (personnelId == null || dateDebut == null || dateFin == null) return;

        List<OrdreDeMission> agentOrders = ordreRepository.findAll().stream()
                .filter(o -> o.getPersonnel() != null && o.getPersonnel().getId().equals(personnelId))
                .filter(o -> excludeOmId == null || !o.getId().equals(excludeOmId))
                .toList();

        for (OrdreDeMission existing : agentOrders) {
            if (existing.getDateDebut() != null && existing.getDateFin() != null) {
                boolean overlaps = !dateDebut.isAfter(existing.getDateFin()) && !dateFin.isBefore(existing.getDateDebut());
                if (overlaps) {
                    throw new IllegalStateException("Conflit de calendrier: l'agent " + existing.getPersonnel().getFullName() 
                            + " a déjà une mission prévue du " + existing.getDateDebut() + " au " + existing.getDateFin() + " (" + existing.getReferenceOrdre() + ").");
                }
            }
        }
    }

    @Transactional
    public OrdreDeMission createDirectOrdre(OrdreDeMission om, Long mandatId, Long personnelId, Long etapeId) {
        if (mandatId != null) {
            MandatDeMission mandat = mandatRepository.findById(mandatId)
                    .orElseThrow(() -> new IllegalArgumentException("Mandat rattache introuvable: " + mandatId));
            om.setMandatDeMission(mandat);
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

        checkAgentOverlap(om.getPersonnel() != null ? om.getPersonnel().getId() : null, om.getDateDebut(), om.getDateFin(), null);

        if (om.getReferenceOrdre() == null || om.getReferenceOrdre().isBlank()) {
            String matricule = om.getPersonnel() != null ? om.getPersonnel().getMatricule() : "AGENT";
            om.setReferenceOrdre("OM-ART-" + matricule + "-" + System.currentTimeMillis() % 10000);
        }

        om.setDateEmission(LocalDate.now());
        om.setStatut(OrdreDeMission.StatutOrdre.BROUILLON_MODIFIABLE);
        OrdreDeMission saved = ordreRepository.save(om);

        auditLogRepository.save(new AuditLog("CREATE_DIRECT_OM", "SYSTEM", "Création direct OM: " + saved.getReferenceOrdre()));
        return saved;
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

        checkAgentOverlap(om.getPersonnel() != null ? om.getPersonnel().getId() : null, updatedDetails.getDateDebut(), updatedDetails.getDateFin(), omId);

        om.setObjectifsSpecifiques(updatedDetails.getObjectifsSpecifiques());
        om.setAvecFrais(updatedDetails.isAvecFrais());
        om.setSansFrais(updatedDetails.isSansFrais());
        om.setDateDebut(updatedDetails.getDateDebut());
        om.setDateFin(updatedDetails.getDateFin());

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
