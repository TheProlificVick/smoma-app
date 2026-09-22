package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.controller.model.RapportMission;
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
        return userRepository.findAllByIdentity(usernameOrEmail).stream().findFirst().orElse(null);
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

    /**
     * DRH staff may create new users, directions, and Personnel records (agents) — the system
     * administrator may too. Also gates editing an existing Personnel record, since it's the same
     * endpoint.
     */
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
     * A mission order's official document (view or PDF) may only be opened by the staff member it
     * was issued to, or by whoever manages mission orders (DRH/admin, same rule as
     * {@link #canIssueMissionOrder}). The requester's User record is matched against the order's
     * assigned Personnel by matricule or email — case-insensitively, and tolerant of either side
     * being blank — since the two records are populated by different flows (AD sync vs. the
     * Personnel referential) and aren't always in perfect sync.
     */
    public boolean canViewOrdreDeMission(User requester, OrdreDeMission om) {
        if (requester == null || om == null) return false;
        if (canIssueMissionOrder(requester)) return true;
        smoma.controller.model.Personnel assignee = om.getPersonnel();
        if (assignee == null) return false;
        if (sameIdentity(requester.getMatricule(), assignee.getMatricule())) return true;
        return sameIdentity(requester.getEmail(), assignee.getEmail());
    }

    public String describeOrdreDeMissionViewRule() {
        return "Seul l'agent désigné pour cette mission, ou le personnel de la DRH, peut consulter cet ordre de mission.";
    }

    /**
     * A Personnel record's "compte individuel" (their mission/financial ledger — days spent on
     * mission, indemnities) may only be opened by that same person, or by whoever manages mission
     * orders (DRH/admin). Same matricule-or-email cross-reference as {@link #canViewOrdreDeMission},
     * for the same reason: the two identity records come from different sync flows.
     */
    public boolean canViewPersonnelAccount(User requester, Personnel target) {
        if (requester == null || target == null) return false;
        if (canIssueMissionOrder(requester)) return true;
        if (sameIdentity(requester.getMatricule(), target.getMatricule())) return true;
        return sameIdentity(requester.getEmail(), target.getEmail());
    }

    public String describePersonnelAccountViewRule() {
        return "Seul l'agent concerné, ou le personnel de la DRH, peut consulter ce compte individuel.";
    }

    private static boolean sameIdentity(String a, String b) {
        return a != null && !a.isBlank() && b != null && !b.isBlank() && a.equalsIgnoreCase(b);
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

    /**
     * Once a mandate has been printed, signed by the DG and scanned back in, it is a fully
     * approved record — whoever manages mandates (DRH/admin, or an agent entitled to initiate one,
     * same population as {@link #canInitiateMandat}) may consult any of them, same as the mission
     * order roster. Anyone else may only consult a mandate they are personally part of.
     */
    public boolean canViewMandat(User requester, MandatDeMission mandat) {
        if (requester == null || mandat == null) return false;
        if (isAdmin(requester) || isHrOfficer(requester) || canInitiateMandat(requester)) return true;
        if (mandat.getPersonnelList() == null) return false;
        for (Personnel p : mandat.getPersonnelList()) {
            if (p == null) continue;
            if (sameIdentity(requester.getMatricule(), p.getMatricule())) return true;
            if (sameIdentity(requester.getEmail(), p.getEmail())) return true;
        }
        return false;
    }

    public String describeMandatViewRule() {
        return "Seul le personnel affecté à ce mandat de mission, ou la DRH/l'administrateur, peut le consulter.";
    }

    /** Only the system administrator may permanently delete users or directions. */
    public boolean canDeleteOrgEntities(User u) {
        return isAdmin(u);
    }

    /**
     * A mission mandate may only be initiated by staff holding the designation CEA
     * (Chargé d'Études Assistant), Chef de Cellule, Sous-Directeur or Directeur. The
     * administrator account keeps every functionality for testing and operations.
     * <p>
     * Real AD titles in this organisation are short rang-derived codes, not full French phrases —
     * confirmed against the Rang/Fonction referential (DataLoader): "CEA1"/"CEA2"/"CEA3"/"CEA No1"
     * for Chargé d'Études Assistant, and "SD"-prefixed codes ("SDM", "SDPSP", "SDGARH", ...) for
     * every Sous-Directeur-level fonction. Matched by prefix, not a bare substring — "sd" as a
     * substring could collide with unrelated text, but as the *start* of a short AD title code it
     * is the organisation's own established naming convention. "Chef de Cellule" and "Directeur"
     * are left on the full-phrase match below rather than guessed at with a new prefix: this
     * organisation also has "Chef de Bureau" and "Chef de Service" — lower, distinct rangs (CB,
     * CS) that must NOT gain this permission — and nothing in the data confirms whether their AD
     * titles share a "Chef "-style prefix with genuine Chef de Cellule titles, so extending the
     * same precise-prefix treatment to them needs that confirmed first, not assumed.
     */
    public boolean canInitiateMandat(User u) {
        if (u == null) return false;
        if (isAdmin(u)) return true;
        String d = designation(u);
        return d.startsWith("cea")
                || d.contains("(cea)")
                || d.contains("charge d'etudes assistant")
                || d.contains("chargé d'études assistant")
                || d.contains("chargé d'etudes assistant")
                || d.contains("charge d'études assistant")
                || d.contains("chef de cellule")
                || d.startsWith("sd")
                || d.contains("sous-directeur")
                || d.contains("sous directeur")
                || d.contains("directeur");
    }

    /**
     * A mission report is validated by staff explicitly identified as DRH in Active Directory —
     * i.e. whose {@code role} was resolved to {@code ROLE_HR_OFFICER} at login, from a real AD
     * signal (LdapDirectoryService#mapAdAttributesToRole: membership of an AD HR group, or an AD
     * title containing "RH"/"ressources humaines"/"HR") — or the administrator.
     * <p>
     * This used to also accept a free-text match on the AD {@code title} alone ("SP", "(SP)",
     * "service du personnel"), on the assumption that meant "Service du Personnel" (DRH). In this
     * organisation's actual AD data, "SP" is reused across unrelated directorates — Secrétariat
     * Particulier, a personal secretary role that exists under any director (Direction Générale,
     * Direction Financière, regional delegations, etc.) — so that fallback let staff with no DRH
     * connection at all validate/reject mission reports. Verified in production: every account
     * with ROLE_HR_OFFICER has a genuinely HR-related AD title, while several plain ROLE_AGENT
     * accounts in unrelated directorates also carry the title "SP" — confirming the role is the
     * reliable signal and the title-text fallback was the bug, not a case worth also keeping.
     */
    public boolean canValidateReport(User u) {
        return isAdmin(u) || isHrOfficer(u);
    }

    /**
     * A mission report may be viewed by whoever manages report validation (same population as
     * {@link #canValidateReport}), or by the agent the report belongs to — same matricule-or-email
     * cross-reference used throughout this class. Anyone else must not see another agent's report
     * title, description, or rejection justification.
     */
    public boolean canViewReport(User requester, RapportMission report) {
        if (requester == null || report == null) return false;
        if (canValidateReport(requester)) return true;
        Personnel author = report.getPersonnel();
        if (author == null) return false;
        if (sameIdentity(requester.getMatricule(), author.getMatricule())) return true;
        return sameIdentity(requester.getEmail(), author.getEmail());
    }

    public String describeMandatRule() {
        return "Seuls les agents ayant la désignation CEA, Chef de Cellule, Sous-Directeur ou Directeur "
                + "(ou le compte administrateur) peuvent établir un mandat de mission.";
    }

    public String describeReportRule() {
        return "Seul le personnel explicitement identifié comme appartenant à la DRH (ou l'administrateur) "
                + "peut valider ou rejeter un rapport de mission.";
    }

    /**
     * DRH staff who should be alerted operationally — e.g. when a mission step is completed —
     * without necessarily including the administrator. Same fix as {@link #canValidateReport}:
     * the AD-verified role alone, no free-text title fallback that would leak these DRH-internal
     * notices to unrelated departments whose staff happen to share the "SP" title.
     */
    public boolean isHrOrPersonnelService(User u) {
        return isHrOfficer(u);
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
        Personnel assignee = om.getPersonnel();
        if (sameIdentity(u.getMatricule(), assignee.getMatricule())) return true;
        return sameIdentity(u.getEmail(), assignee.getEmail());
    }
}
