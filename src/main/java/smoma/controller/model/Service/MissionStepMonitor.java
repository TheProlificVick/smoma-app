package smoma.controller.model.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.EtapeMission;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.User;
import smoma.repository.EtapeMissionRepository;
import smoma.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Watches mission steps and, once a step's end date has passed, notifies the mandate's
 * initiator and DRH (HR / Service du Personnel) that this leg of the mission is complete.
 * Runs shortly after startup and then hourly; each step is only ever notified once.
 */
@Component
public class MissionStepMonitor {

    private static final Logger log = LoggerFactory.getLogger(MissionStepMonitor.class);

    private final EtapeMissionRepository etapeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AccessPolicy accessPolicy;

    public MissionStepMonitor(EtapeMissionRepository etapeRepository,
                              UserRepository userRepository,
                              NotificationService notificationService,
                              AccessPolicy accessPolicy) {
        this.etapeRepository = etapeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.accessPolicy = accessPolicy;
    }

    @Scheduled(initialDelay = 60_000, fixedRate = 3_600_000)
    @Transactional
    public void notifyCompletedSteps() {
        List<EtapeMission> completed = etapeRepository.findByDateFinLessThanEqualAndNotificationFinEnvoyeeFalse(LocalDate.now());
        if (completed.isEmpty()) return;

        List<User> hrRecipients = userRepository.findAll().stream()
                .filter(accessPolicy::isHrOrPersonnelService)
                .toList();

        int notified = 0;
        for (EtapeMission etape : completed) {
            try {
                MandatDeMission mandat = etape.getMandatDeMission();
                String ref = mandat != null && mandat.getReferenceMandat() != null
                        ? mandat.getReferenceMandat() : (mandat != null ? "Mandat #" + mandat.getId() : "N/A");
                String lieu = etape.getLieu() != null ? etape.getLieu() : "destination de la mission";
                String titre = "Étape de mission terminée / Mission step completed";
                String message = "L'étape « " + lieu + " » (" + (etape.getDateDebut() != null ? etape.getDateDebut() : "")
                        + " au " + etape.getDateFin() + ") du mandat " + ref + " est arrivée à son terme.";

                if (mandat != null && mandat.getInitiateurUsername() != null && !mandat.getInitiateurUsername().isBlank()) {
                    notificationService.notifyStepCompleted(null, mandat.getInitiateurUsername(), titre, message);
                }
                for (User hr : hrRecipients) {
                    notificationService.notifyStepCompleted(hr.getMatricule(), hr.getUsername(), titre, message);
                }

                etape.setNotificationFinEnvoyee(true);
                etapeRepository.save(etape);
                notified++;
            } catch (Exception e) {
                log.warn("Notification de fin d'étape #{} échouée: {}", etape.getId(), e.getMessage());
            }
        }
        if (notified > 0) {
            log.info("MissionStepMonitor: {} étape(s) de mission notifiée(s) comme terminée(s).", notified);
        }
    }
}
