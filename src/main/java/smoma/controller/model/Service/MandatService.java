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
    private final IndemniteService indemniteService;
    private final NotificationService notificationService;
    private final DepartmentRepository departmentRepository;
    private final AvanceSurFraisRepository avanceRepository;
    private final RapportMissionRepository rapportRepository;
    private final MissionCapacityService missionCapacityService;
    private final UserRepository userRepository;
    private final AccessPolicy accessPolicy;

    public MandatService(MandatDeMissionRepository mandatRepository,
                         EtapeMissionRepository etapeRepository,
                         OrdreDeMissionRepository ordreRepository,
                         PersonnelRepository personnelRepository,
                         AuditLogRepository auditLogRepository,
                         IndemniteService indemniteService,
                         NotificationService notificationService,
                         DepartmentRepository departmentRepository,
                         AvanceSurFraisRepository avanceRepository,
                         RapportMissionRepository rapportRepository,
                         MissionCapacityService missionCapacityService,
                         UserRepository userRepository,
                         AccessPolicy accessPolicy) {
        this.mandatRepository = mandatRepository;
        this.etapeRepository = etapeRepository;
        this.ordreRepository = ordreRepository;
        this.personnelRepository = personnelRepository;
        this.auditLogRepository = auditLogRepository;
        this.indemniteService = indemniteService;
        this.notificationService = notificationService;
        this.departmentRepository = departmentRepository;
        this.avanceRepository = avanceRepository;
        this.rapportRepository = rapportRepository;
        this.missionCapacityService = missionCapacityService;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
    }

    /**
     * Builds the reference of the act that authorises the mission, in the form
     * ART / DG / &lt;acronyme de la direction initiatrice&gt; / &lt;n° d'ordre&gt;, e.g. "ART/DG/CSI/001".
     */
    private String buildReferenceJustification(String directionName) {
        try {
            String acr = "DG";
            if (directionName != null && !directionName.isBlank()) {
                Department d = departmentRepository.findByName(directionName).orElse(null);
                if (d != null && d.getAcronym() != null && !d.getAcronym().isBlank()) {
                    acr = d.getAcronym().toUpperCase();
                } else {
                    // Fallback: initials of the significant words of the direction name.
                    StringBuilder sb = new StringBuilder();
                    for (String w : directionName.split("[\\s'’\\-]+")) {
                        if (w.length() > 2 && Character.isLetter(w.charAt(0))) sb.append(Character.toUpperCase(w.charAt(0)));
                    }
                    if (sb.length() > 0) acr = sb.toString();
                }
            }
            long seq = mandatRepository.count() + 1;
            return String.format("ART/DG/%s/%03d", acr, seq);
        } catch (Exception e) {
            return "ART/DG/GEN/" + String.format("%03d", (System.currentTimeMillis() % 1000));
        }
    }

    @Transactional
    public MandatDeMission createMandat(MandatDeMission mandat, List<Long> personnelIds, List<smoma.dto.EtapeStepRequest> etapeInputs) {
        return createMandat(mandat, personnelIds, etapeInputs, null);
    }

    @Transactional
    public MandatDeMission createMandat(MandatDeMission mandat, List<Long> personnelIds, List<smoma.dto.EtapeStepRequest> etapeInputs, String initiateurUsername) {
        if (mandat == null) {
            throw new IllegalArgumentException("Aucune donnée de mandat reçue.");
        }
        if (mandat.getObjetGeneral() == null || mandat.getObjetGeneral().isBlank()) {
            throw new IllegalArgumentException("L'objet général de la mission est obligatoire.");
        }
        if (mandat.getDateDebut() == null || mandat.getDateFin() == null) {
            throw new IllegalArgumentException("La période globale du mandat (date de début et date de fin) est obligatoire.");
        }
        if (mandat.getDateFin().isBefore(mandat.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }

        if (mandat.getReferenceMandat() == null || mandat.getReferenceMandat().isBlank()) {
            mandat.setReferenceMandat("MANDAT-ART-" + System.currentTimeMillis());
        }
        if (mandat.getStatut() == null) {
            mandat.setStatut(MandatDeMission.StatutMandat.EN_ATTENTE_SIGNATURE);
        }
        if (mandat.getDateCreation() == null) {
            mandat.setDateCreation(LocalDate.now());
        }
        if (initiateurUsername != null && !initiateurUsername.isBlank()) {
            mandat.setInitiateurUsername(initiateurUsername.trim());
        }

        // Reference of the authorising act (spec: motif réglementaire), e.g. ART/DG/CSI/001.
        if (mandat.getReferenceJustification() == null || mandat.getReferenceJustification().isBlank()) {
            mandat.setReferenceJustification(buildReferenceJustification(mandat.getDirectionInitiatrice()));
        }

        if (LocalDate.now().isAfter(mandat.getDateDebut())) {
            if (LocalDate.now().isAfter(mandat.getDateDebut().plusDays(2))) {
                throw new IllegalArgumentException("Régularisation impossible: le délai de force majeure de 48 heures est dépassé.");
            }
            mandat.setForceMajeure(true);
        }

        if (personnelIds != null && !personnelIds.isEmpty()) {
            List<Personnel> staffList = personnelRepository.findAllById(personnelIds);
            mandat.setPersonnelList(new ArrayList<>(staffList));
        } else {
            mandat.setPersonnelList(new ArrayList<>());
        }

        MandatDeMission savedMandat = mandatRepository.save(mandat);
        List<Personnel> team = savedMandat.getPersonnelList();

        if (etapeInputs != null && !etapeInputs.isEmpty()) {
            // Tracks, within this single submission, how many days each agent has already been
            // committed to (across the mandate's other new steps) — the 100-day/fiscal-year cap and
            // the one-step-at-a-time rule must hold even before any OrdreDeMission row exists yet.
            java.util.Map<Long, Long> newDaysTally = new java.util.HashMap<>();
            List<Object[]> newStepWindows = new ArrayList<>(); // {agentId, dateDebut, dateFin, lieu}

            for (smoma.dto.EtapeStepRequest input : etapeInputs) {
                if (input == null) continue;
                boolean noLocation = input.getLieu() == null || input.getLieu().isBlank();
                boolean noDates = input.getDateDebut() == null && input.getDateFin() == null;
                if (noLocation && noDates) continue; // skip an empty step row

                if (input.getDateDebut() != null && input.getDateFin() != null
                        && savedMandat.getDateDebut() != null && savedMandat.getDateFin() != null) {
                    if (input.getDateDebut().isBefore(savedMandat.getDateDebut())
                            || input.getDateFin().isAfter(savedMandat.getDateFin())) {
                        throw new IllegalArgumentException("Les dates de l'étape doivent être comprises dans la période globale du mandat ("
                                + savedMandat.getDateDebut() + " au " + savedMandat.getDateFin() + ").");
                    }
                }

                EtapeMission etape = new EtapeMission();
                etape.setLieu(input.getLieu());
                etape.setDateDebut(input.getDateDebut() != null ? input.getDateDebut() : savedMandat.getDateDebut());
                etape.setDateFin(input.getDateFin() != null ? input.getDateFin() : savedMandat.getDateFin());
                etape.setTransportMode(input.getTransportMode());
                etape.setCommentaire(input.getCommentaire());
                etape.setMandatDeMission(savedMandat);

                // Staff specifically assigned to this leg of the mission (spec 4.4). Falls back to
                // the whole mandate team at OM-generation time when left empty.
                List<Personnel> stepAgents;
                if (input.getPersonnelIds() != null && !input.getPersonnelIds().isEmpty()) {
                    stepAgents = new ArrayList<>(personnelRepository.findAllById(input.getPersonnelIds()));
                    etape.setPersonnelList(new ArrayList<>(stepAgents));
                } else {
                    stepAgents = team != null ? team : new ArrayList<>();
                }

                LocalDate stepDebut = etape.getDateDebut();
                LocalDate stepFin = etape.getDateFin();
                if (stepDebut != null && stepFin != null) {
                    long stepDays = java.time.temporal.ChronoUnit.DAYS.between(stepDebut, stepFin) + 1;
                    if (stepDays < 1) stepDays = 1;

                    for (Personnel agent : stepAgents) {
                        // One step at a time, regardless of rank: reject overlap with any mission
                        // order already committed (other mandates/OMs) ...
                        missionCapacityService.assertNoOverlap(agent, stepDebut, stepFin, null);

                        // ... and with any other step of THIS mandate being submitted right now.
                        for (Object[] w : newStepWindows) {
                            if (!agent.getId().equals(w[0])) continue;
                            LocalDate oDebut = (LocalDate) w[1];
                            LocalDate oFin = (LocalDate) w[2];
                            if (!stepDebut.isAfter(oFin) && !stepFin.isBefore(oDebut)) {
                                throw new IllegalArgumentException("Conflit de planning pour " + agent.getFullName()
                                        + " : déjà affecté(e) du " + oDebut + " au " + oFin + " (" + w[3]
                                        + ") dans ce même mandat. Un agent ne peut être affecté qu'à une seule étape "
                                        + "de mission à la fois ; la nouvelle affectation n'est possible qu'une fois l'étape en cours terminée.");
                            }
                        }

                        // 100-day/fiscal-year cap, regardless of rank — combines already-committed
                        // mission orders with the steps of this same submission.
                        int fiscalYear = stepDebut.getYear();
                        long existingDays = missionCapacityService.committedDaysInFiscalYear(agent.getId(), fiscalYear, null);
                        long alreadyTallied = newDaysTally.getOrDefault(agent.getId(), 0L);
                        long total = existingDays + alreadyTallied + stepDays;
                        if (total > MissionCapacityService.MAX_DAYS_PER_FISCAL_YEAR) {
                            throw new IllegalArgumentException("Plafond annuel de mission dépassé pour " + agent.getFullName()
                                    + " : " + (existingDays + alreadyTallied) + " jour(s) déjà comptabilisé(s) sur l'exercice " + fiscalYear
                                    + "; cette étape de " + stepDays + " jour(s) porterait le total à " + total
                                    + " jours, au-delà du plafond réglementaire de " + MissionCapacityService.MAX_DAYS_PER_FISCAL_YEAR + " jours/an.");
                        }
                        newDaysTally.merge(agent.getId(), stepDays, Long::sum);
                        newStepWindows.add(new Object[]{agent.getId(), stepDebut, stepFin, input.getLieu()});
                    }
                }

                etapeRepository.save(etape);
            }
        }

        // Notify every designated agent that they are part of this mandate (the individual
        // mission order + its notification follow once the signed scan is imported).
        if (team != null) {
            for (Personnel agent : team) {
                notificationService.notifyMandateTeamAssigned(savedMandat, agent);
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
        List<OrdreDeMission> generatedOrders = generateOrdresDeMissionForMandat(updated);

        // Alert DRH / Service du Personnel: the mandate is now signed and its individual mission
        // orders are ready for them to review, print and deliver to the assigned staff.
        for (User u : userRepository.findAll()) {
            if (!accessPolicy.isHrOrPersonnelService(u)) continue;
            notificationService.notifyMandateSigned(updated, u.getMatricule(), u.getUsername(), generatedOrders.size());
        }

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

        for (EtapeMission etape : etapes) {
            // Spec 4.4: an agent may be concerned by only some of the mandate's steps. When a step
            // has no specific assignment, every team member of the mandate is on it (legacy behaviour).
            List<Personnel> etapeAgents = (etape.getPersonnelList() != null && !etape.getPersonnelList().isEmpty())
                    ? etape.getPersonnelList() : staffList;

            for (Personnel agent : etapeAgents) {
                boolean exists = ordreRepository.findAll().stream().anyMatch(o ->
                        o.getMandatDeMission() != null && o.getMandatDeMission().getId().equals(mandat.getId())
                        && o.getPersonnel() != null && o.getPersonnel().getId().equals(agent.getId())
                        && o.getEtape() != null && o.getEtape().getId().equals(etape.getId()));

                if (!exists) {
                    // Re-validate at OM-generation time (dates/assignments may have shifted since the
                    // mandate was drafted). A conflict for one agent must not block the rest of the batch,
                    // so it is skipped and audited rather than failing the whole mandate.
                    try {
                        missionCapacityService.assertAssignable(agent, etape.getDateDebut() != null ? etape.getDateDebut() : mandat.getDateDebut(),
                                etape.getDateFin() != null ? etape.getDateFin() : mandat.getDateFin(), null);
                    } catch (IllegalStateException conflict) {
                        auditLogRepository.save(new AuditLog("SKIP_OM_GENERATION", "SYSTEM",
                                "Ordre de mission non généré pour " + agent.getFullName() + " (mandat " + mandat.getReferenceMandat()
                                        + ", étape " + (etape.getLieu() != null ? etape.getLieu() : etape.getId()) + "): " + conflict.getMessage()));
                        continue;
                    }

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
                    om.setMoyenTransport(etape.getTransportMode() != null && !etape.getTransportMode().isBlank()
                            ? etape.getTransportMode()
                            : String.join(", ", mandat.getTransportModes()));
                    om.setDirectionInitiatrice(mandat.getDirectionInitiatrice());
                    om.setReferenceJustification(mandat.getReferenceJustification());
                    om.setDateEmission(LocalDate.now());
                    om.setStatut(OrdreDeMission.StatutOrdre.BROUILLON_MODIFIABLE);

                    // Automatic indemnity computation from the official barème (spec 4.6): rang/grade/fonction x
                    // internal/external x number of days. Advance stays 0 until explicitly requested; solde = full.
                    java.math.BigDecimal indemnite = mandat.isSansFrais()
                            ? java.math.BigDecimal.ZERO
                            : indemniteService.calculateTotalIndemnite(om);
                    om.setMontantIndemnite(indemnite);
                    om.setMontantAvance(java.math.BigDecimal.ZERO);
                    om.setMontantSolde(indemnite);

                    OrdreDeMission savedOm = ordreRepository.save(om);
                    generatedOrders.add(savedOm);
                    // Notify the assigned agent: PDF download + report upload available in "Mes Missions".
                    notificationService.notifyMissionAssigned(savedOm);
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

    /**
     * Permanently deletes a mission mandate and everything derived from it (steps, generated
     * mission orders, their advances and reports). Used to undo an erroneous entry.
     */
    @Transactional
    public void deleteMandat(Long id, String actor) {
        MandatDeMission mandat = mandatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mandat introuvable: " + id));

        String ref = mandat.getReferenceMandat() != null ? mandat.getReferenceMandat() : ("Mandat #" + id);

        // 1. Remove every mission order generated from this mandate, along with its dependents.
        List<OrdreDeMission> oms = ordreRepository.findAll().stream()
                .filter(o -> o.getMandatDeMission() != null && o.getMandatDeMission().getId().equals(id))
                .toList();
        for (OrdreDeMission om : oms) {
            avanceRepository.findByOrdreDeMission(om).ifPresent(avanceRepository::delete);
            rapportRepository.findByOrdreDeMission(om).ifPresent(rapportRepository::delete);
            om.setEtape(null); // detach from a mandate step before it is removed
            ordreRepository.delete(om); // cascades the OM's own step rows
        }
        ordreRepository.flush();

        // 2. Delete the mandate itself — cascades its steps and clears the mandat_personnel /
        //    mandat_transport_modes rows.
        mandatRepository.delete(mandat);

        auditLogRepository.save(new AuditLog("DELETE_MANDAT",
                actor != null && !actor.isBlank() ? actor : "SYSTEM",
                "Suppression du mandat " + ref + (oms.isEmpty() ? "" : " (" + oms.size() + " OM associés supprimés)")));
    }
}
