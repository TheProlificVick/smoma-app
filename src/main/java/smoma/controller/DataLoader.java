package smoma.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import smoma.controller.model.*;
import smoma.controller.model.Service.Role;
import smoma.repository.*;
import smoma.controller.model.Service.LdapDirectoryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * When false (the production default), the demo accounts, fabricated indemnity rate table,
     * and sample personnel below are skipped so a fresh database starts genuinely empty instead
     * of pre-loaded with guessable-password logins and made-up numbers. Set true only for a
     * local/dev/staging database (see application-local.properties).
     */
    @Value("${smoma.seed-demo-data:false}")
    private boolean seedDemoData;

    private final UserRepository userRepository;
    private final CompanySettingsRepository settingsRepository;
    private final DepartmentRepository departmentRepository;
    private final FonctionRepository fonctionRepository;
    // Grade entity removed from model; no repository
    private final RangRepository rangRepository;
    private final MotifReglementaireRepository motifRepository;
    private final BaremeIndemniteRepository baremeRepository;
    private final PersonnelRepository personnelRepository;
    private final MandatDeMissionRepository mandatRepository;
    private final EtapeMissionRepository etapeRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final AvanceSurFraisRepository avanceRepository;
    private final RapportMissionRepository rapportRepository;
    private final LdapDirectoryService ldapDirectoryService;

    public DataLoader(UserRepository userRepository,
                      CompanySettingsRepository settingsRepository,
                      DepartmentRepository departmentRepository,
                      FonctionRepository fonctionRepository,
                      RangRepository rangRepository,
                      MotifReglementaireRepository motifRepository,
                      BaremeIndemniteRepository baremeRepository,
                      PersonnelRepository personnelRepository,
                      MandatDeMissionRepository mandatRepository,
                      EtapeMissionRepository etapeRepository,
                      OrdreDeMissionRepository ordreRepository,
                      AvanceSurFraisRepository avanceRepository,
                      RapportMissionRepository rapportRepository,
                      LdapDirectoryService ldapDirectoryService) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.departmentRepository = departmentRepository;
        this.fonctionRepository = fonctionRepository;
        this.rangRepository = rangRepository;
        this.motifRepository = motifRepository;
        this.baremeRepository = baremeRepository;
        this.personnelRepository = personnelRepository;
        this.mandatRepository = mandatRepository;
        this.etapeRepository = etapeRepository;
        this.ordreRepository = ordreRepository;
        this.avanceRepository = avanceRepository;
        this.rapportRepository = rapportRepository;
        this.ldapDirectoryService = ldapDirectoryService;
    }

    /**
     * Ensures a demo account exists and carries the given designation (title). Creates it when
     * missing; otherwise only fills in a blank title / role / structure without touching a
     * password an administrator may have changed.
     */
    private void ensureUser(String username, String password, String displayName,
                            String structure, smoma.controller.model.Service.Role role, String designation) {
        ensureUser(username, password, displayName, structure, role, designation, null);
    }

    private void ensureUser(String username, String password, String displayName,
                            String structure, smoma.controller.model.Service.Role role,
                            String designation, String matricule) {
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) {
            u = new User(username, password, displayName, username, structure, role);
            u.setTitle(designation);
            if (matricule != null) u.setMatricule(matricule);
            userRepository.save(u);
            return;
        }
        boolean dirty = false;
        if (u.getTitle() == null || u.getTitle().isBlank()) { u.setTitle(designation); dirty = true; }
        if (u.getRole() == null) { u.setRole(role); dirty = true; }
        if (u.getStructure() == null || u.getStructure().isBlank()) { u.setStructure(structure); dirty = true; }
        if (matricule != null && (u.getMatricule() == null || u.getMatricule().isBlank())) { u.setMatricule(matricule); dirty = true; }
        if (dirty) userRepository.save(u);
    }

    /**
     * Ensures a directorate / structure exists (matched by exact name). Creates it if missing;
     * otherwise backfills a blank acronym / head designation without touching a real value
     * (e.g. a manager name imported from the Active Directory).
     */
    private void ensureDepartment(String name, String acronym, String headName) {
        if (name == null || name.isBlank()) return;
        Department d = departmentRepository.findByName(name).orElse(null);
        if (d == null) {
            d = new Department();
            d.setName(name);
            d.setAcronym(acronym);
            d.setHeadName(headName);
            departmentRepository.save(d);
            return;
        }
        boolean dirty = false;
        if ((d.getAcronym() == null || d.getAcronym().isBlank()) && acronym != null) { d.setAcronym(acronym); dirty = true; }
        if ((d.getHeadName() == null || d.getHeadName().isBlank()) && headName != null) { d.setHeadName(headName); dirty = true; }
        if (dirty) departmentRepository.save(d);
    }

    /** Ensures a regulatory-justification motif exists (matched by code), creating it if missing. */
    private void ensureMotif(String code, String libelle) {
        if (code == null || code.isBlank()) return;
        if (motifRepository.findByCode(code).isPresent()) return;
        motifRepository.save(new MotifReglementaire(code, libelle, ""));
    }

    /** Ensures a rank tier exists (matched by code), creating it if missing. */
    private void ensureRang(String code, String libelle, int niveau) {
        if (code == null || code.isBlank()) return;
        if (rangRepository.findByCode(code).isPresent()) return;
        rangRepository.save(new Rang(libelle, code, niveau));
    }

    /** Ensures a job title exists (matched by code), creating it if missing. */
    private void ensureFonction(String libelle, String code, String description) {
        if (code == null || code.isBlank()) return;
        if (fonctionRepository.findByCode(code).isPresent()) return;
        fonctionRepository.save(new Fonction(libelle, code, description));
    }

    /**
     * Ensures a daily mission-indemnity rate exists for a rank/mission-type pair (matched by
     * rang+typeMission). Creates it if missing; never overwrites a rate an administrator has
     * since edited via Référentiels.
     */
    private void ensureBareme(String rangCode, OrdreDeMission.TypeMission typeMission, java.math.BigDecimal montant) {
        if (rangCode == null || rangCode.isBlank() || montant == null) return;
        if (baremeRepository.findByRangAndTypeMission(rangCode, typeMission).isPresent()) return;
        BaremeIndemnite b = new BaremeIndemnite();
        b.setRang(rangCode);
        b.setTypeMission(typeMission);
        b.setMontantJournalier(montant);
        baremeRepository.save(b);
    }

    @Override
    public void run(String... args) throws Exception {

        // 1. Company Settings
        if (settingsRepository.count() == 0) {
            CompanySettings settings = new CompanySettings();
            settings.setCompanyName("AGENCE DE REGULATION DES TELECOMMUNICATIONS");
            settings.setAcronym("ART");
            settings.setCity("Yaoundé, Cameroun");
            settings.setLogoPath("/images/ART%20logo.jpg");
            settings.setAddress("B.P. 6132 Yaoundé - Immeuble ART, Quartier Bastos");
            settings.setPhone("+237 222 23 03 80 / 222 23 21 64");
            settings.setEmail("contact@art.cm");
            settingsRepository.save(settings);
        }

        // 2. Demo users — only for a local/dev/staging database (smoma.seed-demo-data=true).
        //    A production database must never be pre-loaded with guessable-password logins;
        //    see the bootstrap-admin block near the end of this method for the real prod path.
        if (seedDemoData) {
            // 2. Users — each carries a "designation" (title) so the mandate-initiation and
            //    report-validation rules from the ART organigramme can be exercised end-to-end.
            if (userRepository.count() == 0) {
                User admin = new User("admin@art.cm", "admin123", "Administrateur Système", "admin@art.cm", "IT", Role.ROLE_ADMIN);
                admin.setTitle("Administrateur");
                userRepository.save(admin);

                User gm = new User("gm@art.cm", "password123", "Directeur Général (DG)", "gm@art.cm", "Direction Générale", Role.ROLE_GENERAL_MANAGER);
                gm.setTitle("Directeur Général");
                userRepository.save(gm);

                // DRH officer, Service du Personnel (designation "SP") — validates mission reports.
                User hr = new User("hr@art.cm", "password123", "Responsable Service du Personnel", "hr@art.cm", "Direction des Ressources Humaines", Role.ROLE_HR_OFFICER);
                hr.setTitle("SP");
                userRepository.save(hr);

                // Directeur — allowed to initiate a mission mandate.
                User dept = new User("dept@art.cm", "password123", "Directeur Technique", "dept@art.cm", "Direction Technique", Role.ROLE_DEPARTMENT_REPRESENTATIVE);
                dept.setTitle("Directeur");
                userRepository.save(dept);

                // Sous-Directeur — allowed to initiate a mission mandate.
                User sd = new User("sd@art.cm", "password123", "Sous-Directeur des Licences", "sd@art.cm", "Direction des Licences, de la Concurrence et de l'Interconnexion", Role.ROLE_DEPARTMENT_REPRESENTATIVE);
                sd.setTitle("Sous-Directeur");
                userRepository.save(sd);

                // Chargé d'Études Assistant (CEA) — allowed to initiate a mission mandate.
                User cea = new User("cea@art.cm", "password123", "Chargé d'Études Assistant", "cea@art.cm", "Direction de la Stratégie et de la Prospective", Role.ROLE_STAFF_MEMBER);
                cea.setTitle("Chargé d'Études Assistant (CEA)");
                userRepository.save(cea);

                // Plain agent — may be assigned missions but cannot initiate a mandate.
                User staff = new User("staff@art.cm", "password123", "Agent de Mission", "staff@art.cm", "Contrôle & Régulation", Role.ROLE_STAFF_MEMBER);
                staff.setTitle("Agent Technique");
                userRepository.save(staff);
            }

            // 2b. Idempotent designation backfill — runs on every startup so an existing database
            //     (seeded before designations were introduced) also gets the demo accounts and titles.
            ensureUser("admin@art.cm", "admin123", "Administrateur Système", "IT", Role.ROLE_ADMIN, "Administrateur");
            ensureUser("gm@art.cm", "password123", "Directeur Général (DG)", "Direction Générale", Role.ROLE_GENERAL_MANAGER, "Directeur Général");
            ensureUser("hr@art.cm", "password123", "Responsable Service du Personnel", "Direction des Ressources Humaines", Role.ROLE_HR_OFFICER, "SP");
            // Direction des Finances — approves advance / balance requests and decides the payment channel.
            ensureUser("finance@art.cm", "password123", "Agent Direction des Finances", "Direction des Finances", Role.ROLE_FINANCE_OFFICER, "DF");
            ensureUser("dept@art.cm", "password123", "Directeur Technique", "Direction Technique", Role.ROLE_DEPARTMENT_REPRESENTATIVE, "Directeur");
            ensureUser("sd@art.cm", "password123", "Sous-Directeur des Licences", "Direction des Licences, de la Concurrence et de l'Interconnexion", Role.ROLE_DEPARTMENT_REPRESENTATIVE, "Sous-Directeur");
            // cea@ and staff@ are linked to seeded personnel matricules so "Mes Missions" shows data immediately.
            ensureUser("cea@art.cm", "password123", "Chargé d'Études Assistant", "Direction de la Stratégie et de la Prospective", Role.ROLE_STAFF_MEMBER, "Chargé d'Études Assistant (CEA)", "ART-2026-002");
            ensureUser("staff@art.cm", "password123", "Agent de Mission", "Contrôle & Régulation", Role.ROLE_STAFF_MEMBER, "Agent Technique", "ART-2026-003");
        }

        // 3. Directions / Structures — the full ART organisational chart (decree n° 2020/727 of
        //    03 December 2020). Seeded idempotently on every startup so the "Direction initiatrice"
        //    dropdown of the mission-mandate module always lists every structure of the organigramme.
        String[][] artStructures = {
            // {name, sigle, responsable/head} — the "responsable" is the head's official designation
            // from the organigramme (Article 129 rank + structure). A real name imported from the
            // Active Directory is never overwritten.
            // Gouvernance / Direction Générale
            {"Conseil d'Administration", "CA", "Président du Conseil d'Administration"},
            {"Direction Générale", "DG", "Directeur Général"},
            // Services rattachés à la Direction Générale
            {"Conseillers Techniques", "CT", "Conseiller Technique"},
            {"Audit Interne", "AI", "Responsable de l'Audit Interne"},
            {"Division du Suivi", "DS", "Chef de la Division du Suivi"},
            {"Division du Contrôle de Gestion", "DCG", "Chef de la Division du Contrôle de Gestion"},
            {"Attaché de Direction", "AD", "Attaché de Direction"},
            {"Cellule des Systèmes d'Information", "CSI", "Chef de la Cellule des Systèmes d'Information"},
            {"Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "CTIB", "Chef de la Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme"},
            {"Sous-Direction de l'Accueil, du Courrier et de la Liaison", "SDACL", "Sous-Directeur de l'Accueil, du Courrier et de la Liaison"},
            {"Comptables Matières", "CM", "Comptable-Matières Principal"},
            // Services centraux — Direction Technique
            {"Direction Technique", "DT", "Directeur Technique"},
            {"Sous-Direction de la Gestion des Ressources Techniques et du Service Universel", "SDGRTSU", "Sous-Directeur de la Gestion des Ressources Techniques et du Service Universel"},
            {"Sous-Direction des Normes, de la Sécurité Électronique et des Agréments", "SDNSEA", "Sous-Directeur des Normes, de la Sécurité Électronique et des Agréments"},
            // Direction de la Gestion des Fréquences
            {"Direction de la Gestion des Fréquences", "DGF", "Directeur de la Gestion des Fréquences"},
            {"Sous-Direction des Études, de la Planification et de l'Ingénierie du Spectre", "SDEPIS", "Sous-Directeur des Études, de la Planification et de l'Ingénierie du Spectre"},
            {"Sous-Direction de la Gestion Administrative du Spectre", "SDGAS", "Sous-Directeur de la Gestion Administrative du Spectre"},
            // Direction des Licences, de la Concurrence et de l'Interconnexion
            {"Direction des Licences, de la Concurrence et de l'Interconnexion", "DLCI", "Directeur des Licences, de la Concurrence et de l'Interconnexion"},
            {"Sous-Direction des Licences", "SDL", "Sous-Directeur des Licences"},
            {"Sous-Direction de l'Analyse et de l'Évaluation Économique", "SDAEE", "Sous-Directeur de l'Analyse et de l'Évaluation Économique"},
            {"Sous-Direction de l'Interconnexion et des Infrastructures des Communications Électroniques", "SDIICE", "Sous-Directeur de l'Interconnexion et des Infrastructures des Communications Électroniques"},
            // Brigade des Contrôles
            {"Brigade des Contrôles", "BC", "Chef de la Brigade des Contrôles"},
            {"Unité de Contrôle des Titres d'Exploitation et des Ressources Rares", "UCTERR", "Chef de l'Unité de Contrôle des Titres d'Exploitation et des Ressources Rares"},
            {"Unité de Contrôle de la Qualité de Service et des Infrastructures", "UCQSI", "Chef de l'Unité de Contrôle de la Qualité de Service et des Infrastructures"},
            {"Unité des Contrôles Administratifs et Tarifaires", "UCAT", "Chef de l'Unité des Contrôles Administratifs et Tarifaires"},
            // Direction de la Stratégie et de la Prospective
            {"Direction de la Stratégie et de la Prospective", "DSP", "Directeur de la Stratégie et de la Prospective"},
            {"Sous-Direction de la Planification Stratégique et de la Prospective", "SDPSP", "Sous-Directeur de la Planification Stratégique et de la Prospective"},
            {"Sous-Direction du Développement des Communications Électroniques", "SDDCE", "Sous-Directeur du Développement des Communications Électroniques"},
            // Division des Affaires Juridiques et de la Protection du Consommateur
            {"Division des Affaires Juridiques et de la Protection du Consommateur", "DAJPC", "Chef de la Division des Affaires Juridiques et de la Protection du Consommateur"},
            {"Cellule de la Réglementation", "CR", "Chef de la Cellule de la Réglementation"},
            {"Cellule du Contentieux", "CC", "Chef de la Cellule du Contentieux"},
            {"Cellule de la Protection du Consommateur", "CPC", "Chef de la Cellule de la Protection du Consommateur"},
            // Division de la Communication et de la Coopération
            {"Division de la Communication et de la Coopération", "DCC", "Chef de la Division de la Communication et de la Coopération"},
            {"Cellule de la Communication et des Relations Publiques", "CCRP", "Chef de la Cellule de la Communication et des Relations Publiques"},
            {"Cellule de la Coopération", "CCoop", "Chef de la Cellule de la Coopération"},
            {"Centre de la Documentation et des Archives", "CDA", "Chef du Centre de la Documentation et des Archives"},
            // Direction des Finances
            {"Direction des Finances", "DF", "Directeur des Finances"},
            {"Sous-Direction du Budget", "SDB", "Sous-Directeur du Budget"},
            {"Sous-Direction de la Comptabilité", "SDC", "Sous-Directeur de la Comptabilité"},
            {"Sous-Direction de la Trésorerie", "SDT", "Sous-Directeur de la Trésorerie"},
            {"Sous-Direction des Marchés", "SDM", "Sous-Directeur des Marchés"},
            // Direction du Patrimoine
            {"Direction du Patrimoine", "DP", "Directeur du Patrimoine"},
            // Direction du Recouvrement
            {"Direction du Recouvrement", "DR", "Directeur du Recouvrement"},
            {"Sous-Direction de la Facturation", "SDFac", "Sous-Directeur de la Facturation"},
            {"Sous-Direction du Suivi du Recouvrement", "SDSR", "Sous-Directeur du Suivi du Recouvrement"},
            // Direction des Ressources Humaines
            {"Direction des Ressources Humaines", "DRH", "Directeur des Ressources Humaines"},
            {"Sous-Direction de la Gestion Administrative des Ressources Humaines", "SDGARH", "Sous-Directeur de la Gestion Administrative des Ressources Humaines"},
            {"Sous-Direction du Développement des Ressources Humaines", "SDDRH", "Sous-Directeur du Développement des Ressources Humaines"},
            {"Sous-Direction de la Solde", "SDS", "Sous-Directeur de la Solde"},
            // Services déconcentrés — Délégations Régionales
            {"Délégation Régionale de Douala", "DR-DLA", "Délégué Régional de Douala"},
            {"Délégation Régionale de Yaoundé", "DR-YDE", "Délégué Régional de Yaoundé"},
            {"Délégation Régionale de Garoua", "DR-GAR", "Délégué Régional de Garoua"},
            {"Délégation Régionale de Bamenda", "DR-BDA", "Délégué Régional de Bamenda"},
            {"Sous-Direction Technique (Services Déconcentrés)", "SDT-D", "Sous-Directeur Technique (Services Déconcentrés)"},
            {"Sous-Direction des Ressources Humaines, Financières et du Patrimoine (Services Déconcentrés)", "SDRHFP-D", "Sous-Directeur des Ressources Humaines, Financières et du Patrimoine (Services Déconcentrés)"},
            {"Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur (Services Déconcentrés)", "SAJCPC-D", "Chef du Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur (Services Déconcentrés)"},
            {"Centres d'Exploitation Spécialisés", "CES", "Chef de Centre d'Exploitation Spécialisé"}
        };
        for (String[] s : artStructures) {
            ensureDepartment(s[0], s[1], s.length > 2 ? s[2] : null);
        }
        // Drop the three placeholder directorates from the very first prototype seed (they are not
        // part of the organigramme) — only when no user account is attached to them.
        for (String legacyName : new String[]{
                "Direction de la Gestion Financière",
                "Direction de la Législation et Coopération",
                "Direction des Prestations et Suivi"}) {
            departmentRepository.findByName(legacyName).ifPresent(dep -> {
                boolean referenced = userRepository.findAll().stream()
                        .anyMatch(u -> u.getDepartment() != null && u.getDepartment().getId().equals(dep.getId()));
                if (!referenced) departmentRepository.delete(dep);
            });
        }

        // 4. Referentiels: Rangs, Fonctions and the mission indemnity rate table — ART's real,
        //    official catalogue ("rang et fonctions - gestion des missions - tarification -
        //    catalogue des indemnités des frais de mission du personnel"), not placeholder data.
        //    Seeded idempotently (matched by code) so it applies to an existing database too, and
        //    a rate an admin has since edited via Référentiels is never overwritten on restart.
        String[][] rangs = {
            // {code, libelle, niveau}
            {"PCA", "Président du Conseil d'Administration", "1"},
            {"MCA", "Membre du Conseil d'Administration", "2"},
            {"DG",  "Directeur Général", "3"},
            {"DGA", "Directeur Général Adjoint", "4"},
            {"D",   "Directeur", "5"},
            {"SD",  "Sous-Directeur", "6"},
            {"CS",  "Chef de Service", "7"},
            {"CB",  "Chef de Bureau", "8"},
            {"CA",  "Cadre d'Appui", "9"},
            {"PA",  "Personnel d'Appui", "10"},
            {"AL",  "Agent de Liaison / Chauffeur", "11"}
        };
        for (String[] r : rangs) {
            ensureRang(r[0], r[1], Integer.parseInt(r[2]));
        }
        // Retire the three placeholder rangs from the very first prototype seed.
        for (String legacyCode : new String[]{"RANG_1", "RANG_2", "RANG_3"}) {
            rangRepository.findByCode(legacyCode).ifPresent(rangRepository::delete);
        }

        String[][] fonctions = {
            // {libelle, code, rang parent}
            {"Président du Conseil d'Administration", "PCA_TITULAIRE", "PCA"},
            {"Membre du Conseil d'Administration", "MCA_TITULAIRE", "MCA"},
            {"Directeur Général", "DG_TITULAIRE", "DG"},
            {"Directeur Général Adjoint", "DGA_TITULAIRE", "DGA"},
            {"Conseiller Technique", "CONSEILLER_TECH", "D"},
            {"Inspecteur", "INSPECTEUR", "D"},
            {"Directeur", "DIRECTEUR_FN", "D"},
            {"Responsable de l'Audit Interne", "RESP_AUDIT", "D"},
            {"Chef de Brigade", "CHEF_BRIGADE", "D"},
            {"Chef de Division", "CHEF_DIVISION", "D"},
            {"Sous-Directeur", "SOUS_DIRECTEUR", "SD"},
            {"Auditeur Junior (Sous-Direction)", "AUDITEUR_JUNIOR_SD", "SD"},
            {"Chargé d'Étude", "CHARGE_ETUDE", "SD"},
            {"Cadre PCA", "CADRE_PCA", "SD"},
            {"Chef de Cellule", "CHEF_CELLULE", "SD"},
            {"Chef de Brigade Adjoint", "CHEF_BRIGADE_ADJ", "SD"},
            {"Attaché de Direction", "ATTACHE_DIRECTION", "SD"},
            {"Chef de Service", "CHEF_SERVICE_CS", "CS"},
            {"Chargée d'Étude Assistant", "CHARGE_ETUDE_ASSISTANT", "CS"},
            {"Chef Secrétariat PCA", "CHEF_SECRETARIAT_PCA", "CS"},
            {"Chef d'Unité", "CHEF_UNITE", "CS"},
            {"Chef Secrétariat DG", "CHEF_SECRETARIAT_DG", "CS"},
            {"Comptable Matière DG", "COMPTABLE_MATIERE_DG", "CS"},
            {"Auditeur Junior (Bureau)", "AUDITEUR_JUNIOR_CB", "CB"},
            {"Chef de Bureau", "CHEF_BUREAU", "CB"},
            {"Chef Secrétariat", "CHEF_SECRETARIAT", "CB"},
            {"Comptable Matière", "COMPTABLE_MATIERE", "CB"},
            {"Cadre", "CADRE", "CA"},
            {"Agent de Maîtrise", "AGENT_MAITRISE", "PA"},
            {"Agent de Liaison", "AGENT_LIAISON", "AL"},
            {"Chauffeur", "CHAUFFEUR", "AL"}
        };
        for (String[] f : fonctions) {
            ensureFonction(f[0], f[1], "Rang : " + f[2]);
        }
        // Retire the placeholder fonctions from the very first prototype seed (distinct codes from
        // the real catalogue above, so this never touches a real entry).
        for (String legacyCode : new String[]{"DIRECTEUR", "CHEF_SERVICE", "INGENIEUR", "JURISTE", "AGENT_TECH"}) {
            fonctionRepository.findByCode(legacyCode).ifPresent(fonctionRepository::delete);
        }

        // Motifs / justifications réglementaires — catalogue complet des types de mission
        // (cf. cahier des charges §4.1 : contrôle, formation, étude, représentation, maintenance,
        // suivi, autres) enrichi des motifs propres à la régulation télécom. Idempotent.
        String[][] motifsReglementaires = {
            // --- Contrôle & surveillance ---
            {"CTRL_QOS",        "Contrôle de la qualité de service (QoS)"},
            {"CTRL_COUVERTURE", "Contrôle de la couverture réseau (2G / 3G / 4G / 5G)"},
            {"CTRL_INFRA",      "Contrôle des infrastructures et installations techniques"},
            {"MONITORING",      "Monitoring / surveillance du spectre des fréquences"},
            {"CTRL_TITRES",     "Contrôle des titres d'exploitation et des ressources rares"},
            {"CTRL_ADMIN_TARIF","Contrôles administratifs et tarifaires"},
            {"CTRL_CONFORMITE", "Contrôle de conformité des équipements et agréments"},
            {"CTRL_INOPINE",    "Contrôle inopiné / mission de constat sur site"},
            // --- Inspection / audit / enquête ---
            {"INSPECTION",      "Inspection technique des installations régionales"},
            {"AUDIT",           "Audit technique et réglementaire"},
            {"ENQUETE",         "Enquête / investigation (brouillage, plainte, litige)"},
            {"EXPERTISE",       "Expertise technique et relevés de mesures"},
            // --- Études ---
            {"ETUDE",           "Étude et analyse technique"},
            {"BENCHMARKING",    "Benchmarking / étude comparative (parangonnage)"},
            {"ETUDE_ECO",       "Étude et évaluation économique du secteur"},
            {"ETUDE_MARCHE",    "Étude de marché / observatoire du secteur des télécommunications"},
            {"PROSPECTIVE",     "Étude prospective et planification stratégique"},
            {"VEILLE_TECHNO",   "Veille technologique et réglementaire"},
            // --- Formation & renforcement des capacités ---
            {"FORMATION",       "Formation / stage / perfectionnement"},
            {"RENFORCEMENT",    "Renforcement des capacités"},
            {"ATELIER",         "Atelier / groupe de travail technique"},
            // --- Représentation & coopération ---
            {"REPRESENTATION",  "Représentation officielle de l'Agence"},
            {"CONF_UIT",        "Participation aux travaux de l'UIT et conférences internationales"},
            {"COOPERATION",     "Mission de coopération bilatérale ou multilatérale"},
            {"SEMINAIRE",       "Séminaire / colloque / conférence"},
            {"NEGOCIATION",     "Négociation / réunion institutionnelle"},
            // --- Exploitation & maintenance ---
            {"MAINTENANCE",     "Maintenance des équipements et systèmes techniques"},
            {"DEPLOIEMENT",     "Déploiement / installation d'équipements"},
            {"SUIVI_PROJET",    "Suivi et évaluation de projet"},
            {"SUPERVISION",     "Supervision et coordination d'activités régionales"},
            // --- Autres missions réglementaires ---
            {"SENSIBILISATION", "Sensibilisation et protection du consommateur"},
            {"RECOUVREMENT",    "Mission de recouvrement / facturation"},
            {"CONTENTIEUX",     "Mission liée au contentieux et aux affaires juridiques"},
            {"APPUI",           "Appui technique aux délégations régionales"},
            {"MISSION_SERVICE", "Mission de service (motif général)"},
            {"REGULARISATION",  "Régularisation (force majeure)"},
            {"DECISION_DG",     "Décision du Directeur Général"},
            {"AUTRE",           "Autre motif dûment habilité"}
        };
        for (String[] mtf : motifsReglementaires) {
            ensureMotif(mtf[0], mtf[1]);
        }
        // Retire the first-prototype motif codes now superseded by the full catalogue above.
        // Mandats store the motif as a plain label, so removing the referential row is harmless.
        for (String legacyCode : new String[]{"CTRL_4G5G", "AUDIT_REG", "SEMINAIRE_JUR"}) {
            motifRepository.findByCode(legacyCode).ifPresent(motifRepository::delete);
        }

        // 5. Rate Scales (BaremeIndemnite) — ART's real, official daily indemnity rates per rank,
        //    Internal / External mission (source: same catalogue as the rangs/fonctions above).
        //    Seeded unconditionally (real data, not a demo fixture) and idempotently — an admin's
        //    later edit via Référentiels is never overwritten on restart. Agent de Liaison /
        //    Chauffeur (AL) has no external rate: that tier is not authorised for external missions.
        String[][] baremeRates = {
            // {rang code, interne, externe-or-null}
            {"PCA", "70000", "90000"},
            {"MCA", "65000", "85000"},
            {"DG",  "65000", "85000"},
            {"DGA", "60000", "80000"},
            {"D",   "50000", "75000"},
            {"SD",  "40000", "70000"},
            {"CS",  "35000", "65000"},
            {"CB",  "30000", "60000"},
            {"CA",  "30000", "60000"},
            {"PA",  "25000", "55000"},
            {"AL",  "20000", null}
        };
        for (String[] rate : baremeRates) {
            ensureBareme(rate[0], OrdreDeMission.TypeMission.INTERNE, new BigDecimal(rate[1]));
            if (rate[2] != null) {
                ensureBareme(rate[0], OrdreDeMission.TypeMission.EXTERNE, new BigDecimal(rate[2]));
            }
        }

        // 6. Personnel (~6 sample staff members) — fabricated demo records, dev/staging only.
        if (seedDemoData && personnelRepository.count() == 0) {
            Personnel p1 = new Personnel("MBARGA", "Lucien", "ART-2026-001", "mbarga@art.cm", "", null);
            Personnel p2 = new Personnel("NNANG", "Alice", "ART-2026-002", "annang@art.cm", "", null);
            Personnel p3 = new Personnel("TCHOUA", "Pierre", "ART-2026-003", "ptchoua@art.cm", "", null);
            Personnel p4 = new Personnel("MOUKOURI", "Nadia", "ART-2026-004", "nmoukouri@art.cm", "", null);
            Personnel p5 = new Personnel("KOUAM", "Emmanuel", "ART-2026-005", "ekouam@art.cm", "", null);
            Personnel p6 = new Personnel("FOTSING", "Joseph", "ART-2026-006", "jfotsing@art.cm", "", null);

            personnelRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
        }

        // 7. Example mandates / mission orders — intentionally NOT seeded (removed on request so the
        //    application starts on a clean slate: no demo MandatDeMission, EtapeMission, OrdreDeMission,
        //    AvanceSurFrais or RapportMission). Referentials, personnel, users and departments above are
        //    still seeded normally; only the transactional mission data was a one-time demo fixture.

        // 8. Active Directory sync: import AD users into local `users` and `personnel` tables
        try {
            var adUsers = ldapDirectoryService.searchUsers(null);
            for (Map<String, String> row : adUsers) {
                String login = row.getOrDefault("login", "");
                if (login.isBlank()) continue;

                // Map role from AD (string) to Role enum safely and make it final for lambda capture
                Role mappedRoleTemp = Role.ROLE_AGENT;
                try {
                    String roleStr = row.getOrDefault("role", "ROLE_AGENT");
                    mappedRoleTemp = Role.valueOf(roleStr);
                } catch (Exception ignored) {}
                final Role mappedRole = mappedRoleTemp;

                // Create or update User
                userRepository.findByUsername(login).orElseGet(() -> {
                    String display = row.getOrDefault("nom", login);
                    String email = row.getOrDefault("email", "");
                    String structure = row.getOrDefault("nomStructure", "");
                    // AD-managed accounts always sign in via a live LDAP bind (AuthController tries
                    // that first); the local password column is never meant to be used for them, so
                    // it gets an unguessable, unusable random hash rather than a shared default.
                    User u = new User(login, passwordEncoder.encode(UUID.randomUUID().toString()), display, email, structure, mappedRole);
                    String rawMatricule = row.getOrDefault("matricule", "LDAP-" + login);
                    if (rawMatricule.length() > 64) rawMatricule = rawMatricule.substring(0, 64);
                    u.setMatricule(rawMatricule);
                    u.setPrenom(row.getOrDefault("prenom", ""));
                    u.setTitle(row.getOrDefault("codeFonction", ""));
                    userRepository.save(u);
                    return u;
                });

                // Ensure Personnel exists (match by matricule if present, otherwise by login)
                String matricule = row.getOrDefault("matricule", "");
                Personnel existing = null;
                if (!matricule.isBlank()) {
                    existing = personnelRepository.findByMatricule(matricule).orElse(null);
                }
                if (existing == null) {
                    String nom = row.getOrDefault("nom", "");
                    String prenom = row.getOrDefault("prenom", "");
                    String email = row.getOrDefault("email", "");
                    String phone = row.getOrDefault("telephone", "");
                    String mat = matricule.isBlank() ? "LDAP-" + login : matricule;
                    if (mat.length() > 64) mat = mat.substring(0, 64);

                    Personnel p = new Personnel(nom, prenom, mat, email, phone, null);
                    p.setDepartement(row.getOrDefault("nomStructure", ""));
                    p.setFonction(row.getOrDefault("codeFonction", ""));
                    personnelRepository.save(p);
                }
            }
        } catch (Exception e) {
            log.warn("AD sync ignoré au démarrage (LDAP indisponible ?): {}", e.getMessage());
        }

        // 9. Bootstrap admin — ensures at least one administrator account always exists so the
        //    first operator can sign in and configure the system, even with demo data disabled
        //    and no matching AD group. Never runs if an admin already exists; never overwrites one.
        if (!userRepository.existsByRole(Role.ROLE_ADMIN)) {
            String bootstrapPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            User bootstrapAdmin = new User("admin@art.cm", passwordEncoder.encode(bootstrapPassword),
                    "Administrateur Système", "admin@art.cm", "IT", Role.ROLE_ADMIN);
            bootstrapAdmin.setTitle("Administrateur");
            userRepository.save(bootstrapAdmin);
            log.warn("======================================================================");
            log.warn("SMOMA: aucun compte administrateur trouvé — un compte de démarrage a été créé.");
            log.warn("Identifiant : admin@art.cm");
            log.warn("Mot de passe temporaire (à usage unique, changez-le immédiatement) : {}", bootstrapPassword);
            log.warn("======================================================================");
        }
    }
}