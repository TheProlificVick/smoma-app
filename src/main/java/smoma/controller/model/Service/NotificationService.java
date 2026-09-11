package smoma.controller.model.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.Notification;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendEmailNotification(String recipientEmail, String subject, String body) {
        // Dispatches email alerts to staff and management (SMTP wiring out of scope).
    }

    /**
     * Persists a notification. A failure here (e.g. the table not yet created) must never
     * roll back the surrounding mission workflow, so it is logged and swallowed.
     */
    public Notification create(String matricule, String username, String titre, String message, String type, String lien) {
        try {
            return notificationRepository.save(new Notification(matricule, username, titre, message, type, lien));
        } catch (Exception e) {
            log.warn("Notification non enregistrée ({}): {}", type, e.getMessage());
            return null;
        }
    }

    /** Raised whenever an individual mission order is generated / assigned to an agent. */
    public void notifyMissionAssigned(OrdreDeMission om) {
        if (om == null || om.getPersonnel() == null) return;
        Personnel p = om.getPersonnel();
        String ref = om.getReferenceOrdre() != null ? om.getReferenceOrdre() : ("OM #" + om.getId());
        String periode = (om.getDateDebut() != null ? om.getDateDebut() : "") + " au " + (om.getDateFin() != null ? om.getDateFin() : "");
        String lieu = om.getLieuDestination() != null && !om.getLieuDestination().isBlank()
                ? (" — destination : " + om.getLieuDestination()) : "";
        create(p.getMatricule(), p.getEmail(),
                "Nouvelle mission assignée / New mission assigned",
                "L'ordre de mission " + ref + " vous a été assigné pour la période " + periode + lieu
                        + ". Ouvrez « Mes Missions » pour télécharger le PDF officiel et déposer votre rapport.",
                "MISSION_ASSIGNED",
                "/my-missions.html");
        sendEmailNotification(p.getEmail(), "Nouvelle mission assignée " + ref, "Voir le portail SMOMA.");
    }

    /** Raised when an agent is added to the team of a mission mandate (before the OMs are generated). */
    public void notifyMandateTeamAssigned(MandatDeMission mandat, Personnel p) {
        if (mandat == null || p == null) return;
        String ref = mandat.getReferenceMandat() != null ? mandat.getReferenceMandat() : ("Mandat #" + mandat.getId());
        String periode = (mandat.getDateDebut() != null ? mandat.getDateDebut() : "") + " au " + (mandat.getDateFin() != null ? mandat.getDateFin() : "");
        create(p.getMatricule(), p.getEmail(),
                "Vous êtes désigné(e) dans un mandat de mission / Assigned to a mission mandate",
                "Vous avez été désigné(e) dans le mandat " + ref + " (« " + (mandat.getObjetGeneral() != null ? mandat.getObjetGeneral() : "") + " »), "
                        + "période " + periode + ". L'ordre de mission individuel vous parviendra dès la signature du mandat.",
                "MANDATE_ASSIGNED",
                "/my-missions.html");
    }

    public void notifyReportValidated(OrdreDeMission om) {
        if (om == null || om.getPersonnel() == null) return;
        Personnel p = om.getPersonnel();
        String ref = om.getReferenceOrdre() != null ? om.getReferenceOrdre() : ("OM #" + om.getId());
        create(p.getMatricule(), p.getEmail(),
                "Rapport de mission validé / Mission report validated",
                "Votre rapport de mission pour " + ref + " a été validé par la DRH (Service du Personnel). "
                        + "Le solde des frais peut désormais être liquidé.",
                "REPORT_VALIDATED",
                "/my-missions.html");
    }

    public List<Notification> forRecipient(String matricule, String username) {
        if (matricule != null && !matricule.isBlank()) {
            List<Notification> byMat = notificationRepository.findByRecipientMatriculeOrderByDateCreationDesc(matricule);
            if (!byMat.isEmpty()) return byMat;
        }
        if (username != null && !username.isBlank()) {
            return notificationRepository.findByRecipientUsernameOrderByDateCreationDesc(username);
        }
        return List.of();
    }

    public long unreadCount(String matricule, String username) {
        long c = 0;
        if (matricule != null && !matricule.isBlank()) c += notificationRepository.countByRecipientMatriculeAndLuFalse(matricule);
        if (c == 0 && username != null && !username.isBlank()) c += notificationRepository.countByRecipientUsernameAndLuFalse(username);
        return c;
    }

    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setLu(true);
            notificationRepository.save(n);
        });
    }

    public void markAllRead(String matricule, String username) {
        for (Notification n : forRecipient(matricule, username)) {
            if (!n.isLu()) {
                n.setLu(true);
                notificationRepository.save(n);
            }
        }
    }
}
