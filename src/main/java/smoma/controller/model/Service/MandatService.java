package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MandatService {

    private final MandatDeMissionRepository mandatRepository;
    private final EtapeMissionRepository etapeRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final PersonnelRepository personnelRepository;
    private final AuditLogRepository auditLogRepository;

    public MandatService(MandatDeMissionRepository mandatRepository,
                         EtapeMissionRepository etapeRepository,
                         OrdreDeMissionRepository ordreRepository,
                         PersonnelRepository personnelRepository,
                         AuditLogRepository auditLogRepository) {
        this.mandatRepository = mandatRepository;
        this.etapeRepository = etapeRepository;
        this.ordreRepository = ordreRepository;
        this.personnelRepository = personnelRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public MandatDeMission createMandat(MandatDeMission mandat, List<Long> personnelIds, List<EtapeMission> etapes) {
        if (mandat.getReferenceMandat() == null || mandat.getReferenceMandat().isBlank()) {
            mandat.setReferenceMandat("MANDAT-ART-" + System.currentTimeMillis());
        }

        if (mandat.getDateDebut() != null && LocalDate.now().isAfter(mandat.getDateDebut())) {
            if (LocalDate.now().isAfter(mandat.getDateDebut().plusDays(2))) {
                throw new IllegalArgumentException("Régularisation impossible: le délai de force majeure de 48 heures est dépassé.");
            }
            mandat.setForceMajeure(true);
        }

        if (personnelIds != null && !personnelIds.isEmpty()) {
            List<Personnel> staffList = personnelRepository.findAllById(personnelIds);
            mandat.setPersonnelList(staffList);
        }

        MandatDeMission savedMandat = mandatRepository.save(mandat);

        if (etapes != null && !etapes.isEmpty()) {
            for (EtapeMission etape : etapes) {
                if (etape.getDateDebut() != null && etape.getDateFin() != null) {
                    if (etape.getDateDebut().isBefore(savedMandat.getDateDebut()) || etape.getDateFin().isAfter(savedMandat.getDateFin())) {
                        throw new IllegalArgumentException("Les dates de l'étape doivent être incluses dans la période globale du mandat (" 
                                + savedMandat.getDateDebut() + " au " + savedMandat.getDateFin() + ").");
                    }
                }
                etape.setMandatDeMission(savedMandat);
                etapeRepository.save(etape);
            }
        }

        auditLogRepository.save(new AuditLog("CREATE_MANDAT", "SYSTEM", "Création mandat: " + savedMandat.getReferenceMandat()));
        return savedMandat;
    }

    @Transactional
    public MandatDeMission uploadSignedScan(Long mandatId, String scanPath) {
        MandatDeMission mandat = mandatRepository.findById(mandatId)
                .orElseThrow(() -> new IllegalArgumentException("Mandat introuvable: " + mandatId));

        mandat.setScanSignedPath(scanPath);
        mandat.setDateValidation(LocalDate.now());

        if (LocalDate.now().isBefore(mandat.getDateDebut())) {
            mandat.setStatut(MandatDeMission.StatutMandat.PREVU);
        } else if (!LocalDate.now().isAfter(mandat.getDateFin())) {
            mandat.setStatut(MandatDeMission.StatutMandat.EN_COURS);
        } else {
            mandat.setStatut(MandatDeMission.StatutMandat.EXECUTE);
        }

        MandatDeMission updated = mandatRepository.save(mandat);
        generateOrdresDeMissionForMandat(updated);

        auditLogRepository.save(new AuditLog("UPLOAD_MANDAT_SCAN", "SYSTEM", "Scan signé importé pour mandat: " + mandat.getReferenceMandat()));
        return updated;
    }

    @Transactional
    public List<OrdreDeMission> generateOrdresDeMissionForMandat(MandatDeMission mandat) {
        List<OrdreDeMission> generatedOrders = new ArrayList<>();
        List<EtapeMission> etapes = mandat.getEtapes();
        List<Personnel> staffList = mandat.getPersonnelList();

        if (etapes.isEmpty()) {
            EtapeMission defaultEtape = new EtapeMission();
            defaultEtape.setLieu("Mission globale");
            defaultEtape.setDateDebut(mandat.getDateDebut());
            defaultEtape.setDateFin(mandat.getDateFin());
            defaultEtape.setMandatDeMission(mandat);
            etapes.add(etapeRepository.save(defaultEtape));
        }

        for (Personnel agent : staffList) {
            for (EtapeMission etape : etapes) {
                boolean exists = ordreRepository.findAll().stream().anyMatch(o -> 
                        o.getMandatDeMission() != null && o.getMandatDeMission().getId().equals(mandat.getId())
                        && o.getPersonnel() != null && o.getPersonnel().getId().equals(agent.getId())
                        && o.getEtape() != null && o.getEtape().getId().equals(etape.getId()));

                if (!exists) {
                    OrdreDeMission om = new OrdreDeMission();
                    om.setReferenceOrdre("OM-ART-" + agent.getMatricule() + "-" + System.currentTimeMillis() % 10000);
                    om.setMandatDeMission(mandat);
                    om.setEtape(etape);
                    om.setPersonnel(agent);
                    om.setTypeMission(mandat.getTypeMission() == MandatDeMission.TypeMission.INTERNE 
                            ? OrdreDeMission.TypeMission.INTERNE : OrdreDeMission.TypeMission.EXTERNE);
                    om.setObjectifsSpecifiques(mandat.getObjectifsSpecifiques());
                    om.setSansFrais(mandat.isSansFrais());
                    om.setAvecFrais(!mandat.isSansFrais());
                    om.setDateDebut(etape.getDateDebut() != null ? etape.getDateDebut() : mandat.getDateDebut());
                    om.setDateFin(etape.getDateFin() != null ? etape.getDateFin() : mandat.getDateFin());
                    om.setDateEmission(LocalDate.now());
                    om.setStatut(OrdreDeMission.StatutOrdre.BROUILLON_MODIFIABLE);
                    generatedOrders.add(ordreRepository.save(om));
                }
            }
        }
        return generatedOrders;
    }

    public List<MandatDeMission> getAllMandats() {
        return mandatRepository.findAll();
    }

    public MandatDeMission getMandatById(Long id) {
        return mandatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mandat introuvable: " + id));
    }
}
