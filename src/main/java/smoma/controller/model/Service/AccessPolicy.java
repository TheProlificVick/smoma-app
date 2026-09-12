package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.User;
import smoma.repository.UserRepository;

/**
 * Central place for the role / designation rules described in the cahier des charges and the
 * ART organigramme (decree n° 2020/727). The web layer has no JWT filter, so callers pass their
 * identity through the X-User-Email header and this class resolves the persisted {@link User}.
 */
@Service
public class AccessPolicy {

    private final UserRepository userRepository;

    public AccessPolicy(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User resolve(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) return null;
        return userRepository.findByIdentity(usernameOrEmail).orElse(null);
    }

    private static String designation(User u) {
        return u == null || u.getTitle() == null ? "" : u.getTitle().trim().toLowerCase();
    }

    public boolean isAdmin(User u) {
        return u != null && u.getRole() == Role.ROLE_ADMIN;
    }

    public boolean isHrOfficer(User u) {
        return u != null && u.getRole() == Role.ROLE_HR_OFFICER;
    }

    private static String structure(User u) {
        return u == null || u.getStructure() == null ? "" : u.getStructure().trim().toLowerCase();
    }

    /**
     * Personnel of the Direction des Finances — they approve mission-expense advances and
     * balances, and decide the payment channel (cash at the counter or bank transfer).
     */
    public boolean isFinanceApprover(User u) {
        if (u == null) return false;
        if (isAdmin(u) || u.getRole() == Role.ROLE_FINANCE_OFFICER) return true;
        String s = structure(u);
        String d = designation(u);
        return s.contains("direction des finances") || s.contains("finance")
                || d.equals("df") || d.contains("(df)") || d.contains("finance");
    }

    public boolean canApproveAdvance(User u) {
        return isFinanceApprover(u);
    }

    public String describeAdvanceApprovalRule() {
        return "Seul le personnel de la Direction des Finances peut approuver une demande d'avance ou de solde.";
    }

    /** DRH staff may create new users and directions; the system administrator may too. */
    public boolean canCreateOrgEntities(User u) {
        return isAdmin(u) || isHrOfficer(u);
    }

    /**
     * Only DRH personnel (Gestionnaire des missions) issue the individual mission orders,
     * whether generated from a signed mandate or created directly. The administrator may too.
     */
    public boolean canIssueMissionOrder(User u) {
        return isAdmin(u) || isHrOfficer(u);
    }

    public String describeMissionOrderRule() {
        return "Seul le personnel de la DRH (gestionnaire des missions) est habilité à établir un ordre de mission.";
    }

    /**
     * A mission mandate may be deleted (to correct an erroneous entry) by the system
     * administrator or by DRH personnel.
     */
    public boolean canDeleteMandat(User u) {
        return isAdmin(u) || isHrOfficer(u);
    }

    public String describeMandatDeleteRule() {
        return "Seuls l'administrateur système et le personnel de la DRH peuvent supprimer un mandat de mission.";
    }

    /** Only the system administrator may permanently delete users or directions. */
    public boolean canDeleteOrgEntities(User u) {
        return isAdmin(u);
    }

    /**
     * A mission mandate may only be initiated by staff holding the designation CEA
     * (Chargé d'Études Assistant), Chef de Cellule, Sous-Directeur or Directeur. The
     * administrator account keeps every functionality for testing and operations.
     */
    public boolean canInitiateMandat(User u) {
        if (u == null) return false;
        if (isAdmin(u)) return true;
        String d = designation(u);
        return d.equals("cea")
                || d.contains("(cea)")
                || d.contains("charge d'etudes assistant")
                || d.contains("chargé d'études assistant")
                || d.contains("chargé d'etudes assistant")
                || d.contains("charge d'études assistant")
                || d.contains("chef de cellule")
                || d.contains("sous-directeur")
                || d.contains("sous directeur")
                || d.contains("directeur");
    }

    /**
     * A mission report is validated by DRH staff of the Service du Personnel (designation "SP"),
     * whose attributions explicitly include "le suivi des missions et déplacements du personnel".
     * HR officers and the administrator may also validate.
     */
    public boolean canValidateReport(User u) {
        if (u == null) return false;
        if (isAdmin(u) || isHrOfficer(u)) return true;
        String d = designation(u);
        return d.equals("sp")
                || d.contains("(sp)")
                || d.contains("service du personnel");
    }

    public String describeMandatRule() {
        return "Seuls les agents ayant la désignation CEA, Chef de Cellule, Sous-Directeur ou Directeur "
                + "(ou le compte administrateur) peuvent établir un mandat de mission.";
    }

    public String describeReportRule() {
        return "Seul le personnel de la DRH affecté au Service du Personnel (désignation « SP ») "
                + "peut valider un rapport de mission.";
    }

    /**
     * DRH staff (HR officers and the Service du Personnel) who should be alerted operationally
     * — e.g. when a mission step is completed — without necessarily including the administrator.
     */
    public boolean isHrOrPersonnelService(User u) {
        if (u == null) return false;
        if (isHrOfficer(u)) return true;
        String d = designation(u);
        return d.equals("sp") || d.contains("(sp)") || d.contains("service du personnel");
    }

    /**
     * The signed mandate scan is imported either by whoever is entitled to initiate a mandate
     * (they're typically the one who carried the printed document to the GM and back) or by
     * DRH/admin downstream.
     */
    public boolean canUploadMandatScan(User u) {
        return isAdmin(u) || isHrOfficer(u) || canInitiateMandat(u);
    }

    /**
     * A mission report is deposited either by the agent it was assigned to, or by DRH/admin
     * filing it on their behalf.
     */
    public boolean canDepositReport(User u, OrdreDeMission om) {
        if (u == null) return false;
        if (isAdmin(u) || isHrOfficer(u)) return true;
        if (om == null || om.getPersonnel() == null) return false;
        String agentMatricule = om.getPersonnel().getMatricule();
        String userMatricule = u.getMatricule();
        return agentMatricule != null && userMatricule != null && agentMatricule.equalsIgnoreCase(userMatricule);
    }
}
