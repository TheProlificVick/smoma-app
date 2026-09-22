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
    private void ensureDepartment(String name, String nameEn, String acronym, String headName, String headNameEn) {
        if (name == null || name.isBlank()) return;
        Department d = departmentRepository.findByName(name).orElse(null);
        if (d == null) {
            d = new Department();
            d.setName(name);
            d.setNameEn(nameEn);
            d.setAcronym(acronym);
            d.setHeadName(headName);
            d.setHeadNameEn(headNameEn);
            departmentRepository.save(d);
            return;
        }
        boolean dirty = false;
        if ((d.getAcronym() == null || d.getAcronym().isBlank()) && acronym != null) { d.setAcronym(acronym); dirty = true; }
        if ((d.getHeadName() == null || d.getHeadName().isBlank()) && headName != null) { d.setHeadName(headName); dirty = true; }
        if ((d.getNameEn() == null || d.getNameEn().isBlank()) && nameEn != null) { d.setNameEn(nameEn); dirty = true; }
        if ((d.getHeadNameEn() == null || d.getHeadNameEn().isBlank()) && headNameEn != null) { d.setHeadNameEn(headNameEn); dirty = true; }
        if (dirty) departmentRepository.save(d);
    }

    /**
     * Forces a department's "Responsable" back to its generic official designation — unlike
     * {@link #ensureDepartment}, which only backfills a blank headName so a real manager name
     * imported from the Active Directory is never clobbered, this always overwrites. Use it only to
     * clean up a specific row that was manually set to an actual person's name at some point (e.g.
     * through the admin panel), so it displays the same way as the rest of the Directions/Structures
     * list — a title, not a name — instead of leaving that inconsistency to reappear on every restart.
     */
    private void resetDepartmentHeadDesignation(String departmentName, String headName, String headNameEn) {
        departmentRepository.findByName(departmentName).ifPresent(d -> {
            if (!headName.equals(d.getHeadName()) || !headNameEn.equals(d.getHeadNameEn())) {
                d.setHeadName(headName);
                d.setHeadNameEn(headNameEn);
                departmentRepository.save(d);
            }
        });
    }

    /** Ensures a regulatory-justification motif exists (matched by code), creating it if missing;
     *  backfills a blank English translation on an existing row without touching anything else. */
    private void ensureMotif(String code, String libelle, String libelleEn) {
        if (code == null || code.isBlank()) return;
        MotifReglementaire existing = motifRepository.findByCode(code).orElse(null);
        if (existing == null) {
            motifRepository.save(new MotifReglementaire(code, libelle, libelleEn, ""));
            return;
        }
        if ((existing.getLibelleEn() == null || existing.getLibelleEn().isBlank()) && libelleEn != null) {
            existing.setLibelleEn(libelleEn);
            motifRepository.save(existing);
        }
    }

    /** Ensures a rank tier exists (matched by code), creating it if missing; backfills a blank
     *  English translation on an existing row without touching anything else. */
    private void ensureRang(String code, String libelle, String libelleEn, int niveau) {
        if (code == null || code.isBlank()) return;
        Rang existing = rangRepository.findByCode(code).orElse(null);
        if (existing == null) {
            Rang r = new Rang(libelle, code, niveau);
            r.setLibelleEn(libelleEn);
            rangRepository.save(r);
            return;
        }
        if ((existing.getLibelleEn() == null || existing.getLibelleEn().isBlank()) && libelleEn != null) {
            existing.setLibelleEn(libelleEn);
            rangRepository.save(existing);
        }
    }

    /** Ensures a job title exists (matched by code), creating it if missing; backfills a blank
     *  English translation on an existing row without touching anything else. */
    private void ensureFonction(String libelle, String libelleEn, String code, String description) {
        if (code == null || code.isBlank()) return;
        Fonction existing = fonctionRepository.findByCode(code).orElse(null);
        if (existing == null) {
            fonctionRepository.save(new Fonction(libelle, libelleEn, code, description));
            return;
        }
        if ((existing.getLibelleEn() == null || existing.getLibelleEn().isBlank()) && libelleEn != null) {
            existing.setLibelleEn(libelleEn);
            fonctionRepository.save(existing);
        }
    }

    /**
     * Ensures a real, organigramme-linked poste exists (matched by code): a specific position
     * ("Chef de Service de la Trésorerie") tied to the exact directorate/structure it belongs to
     * and the rang it carries — unlike the generic {@link #ensureFonction(String, String, String, String)}
     * catalogue above, this is what the Personnel form's cascading Fonction dropdown reads (see
     * ReferentielController#getFonctions and IndemniteService, which prices missions off
     * Personnel.rang). Never overwrites an existing row's department/rang/libelle so an admin's
     * edit survives restarts; only backfills a blank English translation.
     */
    private void ensureLinkedFonction(String libelle, String libelleEn, String code, String departmentName, String rangCode) {
        if (code == null || code.isBlank()) return;
        Fonction existing = fonctionRepository.findByCode(code).orElse(null);
        if (existing == null) {
            Department dept = departmentRepository.findByName(departmentName).orElse(null);
            Rang rang = rangRepository.findByCode(rangCode).orElse(null);
            Fonction f = new Fonction(libelle, libelleEn, code, null);
            f.setDepartment(dept);
            f.setRang(rang);
            fonctionRepository.save(f);
            return;
        }
        if ((existing.getLibelleEn() == null || existing.getLibelleEn().isBlank()) && libelleEn != null) {
            existing.setLibelleEn(libelleEn);
            fonctionRepository.save(existing);
        }
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
            {"Conseil d'Administration", "Board of Directors", "CA", "Président du Conseil d'Administration", "Chairperson of the Board of Directors"},
            {"Direction Générale", "General Management", "DG", "Directeur Général", "Director General"},
            // Structure n°3 de l'organigramme (v2) : Conseillers Techniques, Inspecteurs et le
            // personnel d'appui du Cabinet — distincte de la Direction Générale elle-même.
            {"Cabinet du Directeur Général", "Office of the Director General", "CAB", "Conseiller Technique", "Technical Adviser"},
            // Services rattachés à la Direction Générale
            {"Conseillers Techniques", "Technical Advisers", "CT", "Conseiller Technique", "Technical Adviser"},
            {"Audit Interne", "Internal Audit", "AI", "Responsable de l'Audit Interne", "Head of Internal Audit"},
            {"Division du Suivi", "Monitoring Division", "DS", "Chef de la Division du Suivi", "Head of the Monitoring Division"},
            {"Division du Contrôle de Gestion", "Management Control Division", "DCG", "Chef de la Division du Contrôle de Gestion", "Head of the Management Control Division"},
            {"Attaché de Direction", "Directorate Attaché", "AD", "Attaché de Direction", "Directorate Attaché"},
            {"Cellule des Systèmes d'Information", "Information Systems Unit", "CSI", "Chef de la Cellule des Systèmes d'Information", "Head of the Information Systems Unit"},
            // Structure n°8 de l'organigramme (v2), absente de la version précédente.
            {"Cellule des Plateformes Techniques", "Technical Platforms Unit", "CPT", "Chef de la Cellule des Plateformes Techniques", "Head of the Technical Platforms Unit"},
            {"Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "Translation, Interpretation and Bilingualism Promotion Unit", "CTIB", "Chef de la Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "Head of the Translation, Interpretation and Bilingualism Promotion Unit"},
            {"Sous-Direction de l'Accueil, du Courrier et de la Liaison", "Reception, Mail and Liaison Sub-Directorate", "SDACL", "Sous-Directeur de l'Accueil, du Courrier et de la Liaison", "Deputy Director of Reception, Mail and Liaison"},
            {"Comptables Matières", "Property Accountants", "CM", "Comptable-Matières Principal", "Principal Property Accountant"},
            // Services centraux — Direction Technique
            {"Direction Technique", "Technical Directorate", "DT", "Directeur Technique", "Technical Director"},
            {"Sous-Direction de la Gestion des Ressources Techniques et du Service Universel", "Technical Resources Management and Universal Service Sub-Directorate", "SDGRTSU", "Sous-Directeur de la Gestion des Ressources Techniques et du Service Universel", "Deputy Director of Technical Resources Management and Universal Service"},
            {"Sous-Direction des Normes, de la Sécurité Électronique et des Agréments", "Standards, Electronic Security and Approvals Sub-Directorate", "SDNSEA", "Sous-Directeur des Normes, de la Sécurité Électronique et des Agréments", "Deputy Director of Standards, Electronic Security and Approvals"},
            // Direction de la Gestion des Fréquences
            {"Direction de la Gestion des Fréquences", "Frequency Management Directorate", "DGF", "Directeur de la Gestion des Fréquences", "Director of Frequency Management"},
            {"Sous-Direction des Études, de la Planification et de l'Ingénierie du Spectre", "Spectrum Studies, Planning and Engineering Sub-Directorate", "SDEPIS", "Sous-Directeur des Études, de la Planification et de l'Ingénierie du Spectre", "Deputy Director of Spectrum Studies, Planning and Engineering"},
            {"Sous-Direction de la Gestion Administrative du Spectre", "Spectrum Administrative Management Sub-Directorate", "SDGAS", "Sous-Directeur de la Gestion Administrative du Spectre", "Deputy Director of Spectrum Administrative Management"},
            // Direction des Licences, de la Concurrence et de l'Interconnexion
            {"Direction des Licences, de la Concurrence et de l'Interconnexion", "Licensing, Competition and Interconnection Directorate", "DLCI", "Directeur des Licences, de la Concurrence et de l'Interconnexion", "Director of Licensing, Competition and Interconnection"},
            {"Sous-Direction des Licences", "Licensing Sub-Directorate", "SDL", "Sous-Directeur des Licences", "Deputy Director of Licensing"},
            {"Sous-Direction de l'Analyse et de l'Évaluation Économique", "Economic Analysis and Evaluation Sub-Directorate", "SDAEE", "Sous-Directeur de l'Analyse et de l'Évaluation Économique", "Deputy Director of Economic Analysis and Evaluation"},
            {"Sous-Direction de l'Interconnexion et des Infrastructures des Communications Électroniques", "Interconnection and Electronic Communications Infrastructure Sub-Directorate", "SDIICE", "Sous-Directeur de l'Interconnexion et des Infrastructures des Communications Électroniques", "Deputy Director of Interconnection and Electronic Communications Infrastructure"},
            // Brigade des Contrôles
            {"Brigade des Contrôles", "Control Brigade", "BC", "Chef de la Brigade des Contrôles", "Head of the Control Brigade"},
            {"Unité de Contrôle des Titres d'Exploitation et des Ressources Rares", "Operating Licenses and Scarce Resources Control Unit", "UCTERR", "Chef de l'Unité de Contrôle des Titres d'Exploitation et des Ressources Rares", "Head of the Operating Licenses and Scarce Resources Control Unit"},
            {"Unité de Contrôle de la Qualité de Service et des Infrastructures", "Quality of Service and Infrastructure Control Unit", "UCQSI", "Chef de l'Unité de Contrôle de la Qualité de Service et des Infrastructures", "Head of the Quality of Service and Infrastructure Control Unit"},
            {"Unité des Contrôles Administratifs et Tarifaires", "Administrative and Tariff Controls Unit", "UCAT", "Chef de l'Unité des Contrôles Administratifs et Tarifaires", "Head of the Administrative and Tariff Controls Unit"},
            // Direction de la Stratégie et de la Prospective
            {"Direction de la Stratégie et de la Prospective", "Strategy and Foresight Directorate", "DSP", "Directeur de la Stratégie et de la Prospective", "Director of Strategy and Foresight"},
            {"Sous-Direction de la Planification Stratégique et de la Prospective", "Strategic Planning and Foresight Sub-Directorate", "SDPSP", "Sous-Directeur de la Planification Stratégique et de la Prospective", "Deputy Director of Strategic Planning and Foresight"},
            {"Sous-Direction du Développement des Communications Électroniques", "Electronic Communications Development Sub-Directorate", "SDDCE", "Sous-Directeur du Développement des Communications Électroniques", "Deputy Director of Electronic Communications Development"},
            // Sous-direction de l'organigramme (v2), absente de la version précédente.
            {"Sous-Direction des Technologies Innovantes", "Innovative Technologies Sub-Directorate", "SDTI", "Sous-Directeur des Technologies Innovantes", "Deputy Director of Innovative Technologies"},
            // Division des Affaires Juridiques et de la Protection du Consommateur
            {"Division des Affaires Juridiques et de la Protection du Consommateur", "Legal Affairs and Consumer Protection Division", "DAJPC", "Chef de la Division des Affaires Juridiques et de la Protection du Consommateur", "Head of the Legal Affairs and Consumer Protection Division"},
            {"Cellule de la Réglementation", "Regulations Unit", "CR", "Chef de la Cellule de la Réglementation", "Head of the Regulations Unit"},
            {"Cellule du Contentieux", "Litigation Unit", "CC", "Chef de la Cellule du Contentieux", "Head of the Litigation Unit"},
            {"Cellule de la Protection du Consommateur", "Consumer Protection Unit", "CPC", "Chef de la Cellule de la Protection du Consommateur", "Head of the Consumer Protection Unit"},
            // Division de la Communication et de la Coopération
            {"Division de la Communication et de la Coopération", "Communication and Cooperation Division", "DCC", "Chef de la Division de la Communication et de la Coopération", "Head of the Communication and Cooperation Division"},
            {"Cellule de la Communication et des Relations Publiques", "Communication and Public Relations Unit", "CCRP", "Chef de la Cellule de la Communication et des Relations Publiques", "Head of the Communication and Public Relations Unit"},
            {"Cellule de la Coopération", "Cooperation Unit", "CCoop", "Chef de la Cellule de la Coopération", "Head of the Cooperation Unit"},
            {"Centre de la Documentation et des Archives", "Documentation and Archives Center", "CDA", "Chef du Centre de la Documentation et des Archives", "Head of the Documentation and Archives Center"},
            // Direction des Finances
            {"Direction des Finances", "Finance Directorate", "DF", "Directeur des Finances", "Director of Finance"},
            {"Sous-Direction du Budget", "Budget Sub-Directorate", "SDB", "Sous-Directeur du Budget", "Deputy Director of Budget"},
            {"Sous-Direction de la Comptabilité", "Accounting Sub-Directorate", "SDC", "Sous-Directeur de la Comptabilité", "Deputy Director of Accounting"},
            {"Sous-Direction de la Trésorerie", "Treasury Sub-Directorate", "SDT", "Sous-Directeur de la Trésorerie", "Deputy Director of Treasury"},
            {"Sous-Direction des Marchés", "Procurement Sub-Directorate", "SDM", "Sous-Directeur des Marchés", "Deputy Director of Procurement"},
            // Direction du Patrimoine
            {"Direction du Patrimoine", "Property Directorate", "DP", "Directeur du Patrimoine", "Director of Property"},
            // Direction du Recouvrement
            {"Direction du Recouvrement", "Collection Directorate", "DR", "Directeur du Recouvrement", "Director of Collection"},
            {"Sous-Direction de la Facturation", "Billing Sub-Directorate", "SDFac", "Sous-Directeur de la Facturation", "Deputy Director of Billing"},
            {"Sous-Direction du Suivi du Recouvrement", "Collection Monitoring Sub-Directorate", "SDSR", "Sous-Directeur du Suivi du Recouvrement", "Deputy Director of Collection Monitoring"},
            // Direction des Ressources Humaines
            {"Direction des Ressources Humaines", "Human Resources Directorate", "DRH", "Directeur des Ressources Humaines", "Director of Human Resources"},
            {"Sous-Direction de la Gestion Administrative des Ressources Humaines", "HR Administrative Management Sub-Directorate", "SDGARH", "Sous-Directeur de la Gestion Administrative des Ressources Humaines", "Deputy Director of HR Administrative Management"},
            {"Sous-Direction du Développement des Ressources Humaines", "HR Development Sub-Directorate", "SDDRH", "Sous-Directeur du Développement des Ressources Humaines", "Deputy Director of HR Development"},
            {"Sous-Direction de la Solde", "Payroll Sub-Directorate", "SDS", "Sous-Directeur de la Solde", "Deputy Director of Payroll"},
            // Services déconcentrés — Délégations Régionales
            {"Délégation Régionale de Douala", "Douala Regional Delegation", "DR-DLA", "Délégué Régional de Douala", "Regional Delegate for Douala"},
            {"Délégation Régionale de Yaoundé", "Yaoundé Regional Delegation", "DR-YDE", "Délégué Régional de Yaoundé", "Regional Delegate for Yaoundé"},
            {"Délégation Régionale de Garoua", "Garoua Regional Delegation", "DR-GAR", "Délégué Régional de Garoua", "Regional Delegate for Garoua"},
            {"Délégation Régionale de Bamenda", "Bamenda Regional Delegation", "DR-BDA", "Délégué Régional de Bamenda", "Regional Delegate for Bamenda"},
            {"Sous-Direction Technique (Services Déconcentrés)", "Technical Sub-Directorate (Decentralized Services)", "SDT-D", "Sous-Directeur Technique (Services Déconcentrés)", "Deputy Director for Technical Affairs (Decentralized Services)"},
            {"Sous-Direction des Ressources Humaines, Financières et du Patrimoine (Services Déconcentrés)", "Human, Financial and Property Resources Sub-Directorate (Decentralized Services)", "SDRHFP-D", "Sous-Directeur des Ressources Humaines, Financières et du Patrimoine (Services Déconcentrés)", "Deputy Director of Human, Financial and Property Resources (Decentralized Services)"},
            {"Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur (Services Déconcentrés)", "Legal Affairs, Litigation and Consumer Protection Service (Decentralized Services)", "SAJCPC-D", "Chef du Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur (Services Déconcentrés)", "Head of the Legal Affairs, Litigation and Consumer Protection Service (Decentralized Services)"},
            {"Centres d'Exploitation Spécialisés", "Specialized Operations Centers", "CES", "Chef de Centre d'Exploitation Spécialisé", "Head of Specialized Operations Center"},
        };
        for (String[] s : artStructures) {
            ensureDepartment(s[0], s[1], s[2], s[3], s[4]);
        }
        // Direction des Ressources Humaines and Direction Technique were at some point given an
        // actual person's name as "Responsable" (unlike every other row in Directions / Structures,
        // which shows the generic official designation) — reset them so the list is consistent.
        resetDepartmentHeadDesignation("Direction des Ressources Humaines", "Directeur des Ressources Humaines", "Director of Human Resources");
        resetDepartmentHeadDesignation("Direction Technique", "Directeur Technique", "Technical Director");
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
            {"PCA", "Président du Conseil d'Administration", "Chairperson of the Board of Directors", "1"},
            {"MCA", "Membre du Conseil d'Administration", "Member of the Board of Directors", "2"},
            {"DG", "Directeur Général", "Director General", "3"},
            {"DGA", "Directeur Général Adjoint", "Deputy Director General", "4"},
            {"D", "Directeur", "Director", "5"},
            {"SD", "Sous-Directeur", "Deputy Director", "6"},
            {"CS", "Chef de Service", "Head of Service", "7"},
            {"CB", "Chef de Bureau", "Head of Bureau", "8"},
            {"CA", "Cadre d'Appui", "Support Officer", "9"},
            {"PA", "Personnel d'Appui", "Support Staff", "10"},
            {"AL", "Agent de Liaison / Chauffeur", "Liaison Officer / Driver", "11"},
        };
        for (String[] r : rangs) {
            ensureRang(r[0], r[1], r[2], Integer.parseInt(r[3]));
        }
        // Retire the three placeholder rangs from the very first prototype seed. On some
        // already-seeded databases these rows were saved with code/libelle swapped (a bug in that
        // original prototype, long fixed), so "RANG_1" can be sitting in either column — match both
        // rather than only code, or the stale row survives every restart and clutters the tariff grid.
        java.util.Set<String> legacyRangTokens = java.util.Set.of("RANG_1", "RANG_2", "RANG_3");
        rangRepository.findAll().stream()
                .filter(r -> legacyRangTokens.contains(r.getCode()) || legacyRangTokens.contains(r.getLibelle()))
                .forEach(rangRepository::delete);

        String[][] fonctions = {
            // {libelle, code, rang parent}
            {"Président du Conseil d'Administration", "Chairperson of the Board of Directors", "PCA_TITULAIRE", "PCA"},
            {"Membre du Conseil d'Administration", "Member of the Board of Directors", "MCA_TITULAIRE", "MCA"},
            {"Directeur Général", "Director General", "DG_TITULAIRE", "DG"},
            {"Directeur Général Adjoint", "Deputy Director General", "DGA_TITULAIRE", "DGA"},
            {"Conseiller Technique", "Technical Adviser", "CONSEILLER_TECH", "D"},
            {"Inspecteur", "Inspector", "INSPECTEUR", "D"},
            {"Directeur", "Director", "DIRECTEUR_FN", "D"},
            {"Responsable de l'Audit Interne", "Head of Internal Audit", "RESP_AUDIT", "D"},
            {"Chef de Brigade", "Head of Brigade", "CHEF_BRIGADE", "D"},
            {"Chef de Division", "Head of Division", "CHEF_DIVISION", "D"},
            // Organigramme v2 : tête des Délégations Régionales — absente du catalogue précédent
            // alors que 4 Délégations Régionales existent déjà comme structures.
            {"Délégué Régional", "Regional Delegate", "DELEGUE_REGIONAL", "D"},
            {"Sous-Directeur", "Deputy Director", "SOUS_DIRECTEUR", "SD"},
            {"Auditeur Junior (Sous-Direction)", "Junior Auditor (Sub-Directorate)", "AUDITEUR_JUNIOR_SD", "SD"},
            {"Chargé d'Étude", "Research Officer", "CHARGE_ETUDE", "SD"},
            {"Cadre PCA", "Staff Officer, Chairperson's Office", "CADRE_PCA", "SD"},
            {"Chef de Cellule", "Head of Unit", "CHEF_CELLULE", "SD"},
            {"Chef de Brigade Adjoint", "Deputy Head of Brigade", "CHEF_BRIGADE_ADJ", "SD"},
            {"Attaché de Direction", "Directorate Attaché", "ATTACHE_DIRECTION", "SD"},
            // Organigramme v2 : encadrement des Délégations Régionales sous le Délégué Régional.
            {"Délégué Régional Adjoint", "Deputy Regional Delegate", "DELEGUE_REGIONAL_ADJOINT", "SD"},
            {"Chef d'Antenne", "Branch Office Head", "CHEF_ANTENNE", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_SERVICE_CS", "CS"},
            {"Chargée d'Étude Assistant", "Assistant Research Officer", "CHARGE_ETUDE_ASSISTANT", "CS"},
            {"Chef Secrétariat PCA", "Head of Secretariat, Chairperson's Office", "CHEF_SECRETARIAT_PCA", "CS"},
            {"Chef d'Unité", "Head of Unit", "CHEF_UNITE", "CS"},
            {"Chef Secrétariat DG", "Head of Secretariat, DG's Office", "CHEF_SECRETARIAT_DG", "CS"},
            {"Comptable Matière DG", "Property Accountant, DG's Office", "COMPTABLE_MATIERE_DG", "CS"},
            {"Auditeur Junior (Bureau)", "Junior Auditor (Bureau)", "AUDITEUR_JUNIOR_CB", "CB"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_BUREAU", "CB"},
            {"Chef Secrétariat", "Head of Secretariat", "CHEF_SECRETARIAT", "CB"},
            {"Comptable Matière", "Property Accountant", "COMPTABLE_MATIERE", "CB"},
            {"Cadre", "Staff Officer", "CADRE", "CA"},
            {"Agent de Maîtrise", "Supervisory Officer", "AGENT_MAITRISE", "PA"},
            {"Agent de Liaison", "Liaison Officer", "AGENT_LIAISON", "AL"},
            {"Chauffeur", "Driver", "CHAUFFEUR", "AL"},
        };
        for (String[] f : fonctions) {
            ensureFonction(f[0], f[1], f[2], "Rang : " + f[3]);
        }
        // Retire the placeholder fonctions from the very first prototype seed (distinct codes from
        // the real catalogue above, so this never touches a real entry).
        for (String legacyCode : new String[]{"DIRECTEUR", "CHEF_SERVICE", "INGENIEUR", "JURISTE", "AGENT_TECH"}) {
            fonctionRepository.findByCode(legacyCode).ifPresent(fonctionRepository::delete);
        }

        // 4b. Full organigramme-linked poste catalogue (source: "structure rang fonction gestion
        // des OM-v2.xlsx", sheet "structure-fonction-rang") — every real, named position in the
        // ART organigramme, tied to the exact directorate/structure it belongs to and the rang it
        // carries. Unlike the generic catalogue above, this is what the Personnel form's Fonction
        // dropdown filters by department for, and what auto-fills the correct rang (see
        // ReferentielController#getFonctions and Fonction.department / Fonction.rang). Seeded
        // idempotently (matched by code) so an admin's later edit is never overwritten on restart.
        // {libellé, code, direction/structure parente, code rang}
        String[][] organigrammePostes = {
            {"Président du Conseil d'Administration", "Chairperson of the Board of Directors", "PRESIDENT_DU_CONSEIL_D_ADMINISTRATION_CA", "Conseil d'Administration", "PCA"},
            {"Administrateur", "Board Member", "ADMINISTRATEUR_CA", "Conseil d'Administration", "MCA"},
            {"Cadre attaché au Cabinet du PCA (Affaires réservées)", "Staff Officer attached to the Chairperson's Office (Confidential Affairs)", "CADRE_ATTACHE_AU_CABINET_DU_PCA_AFFAIRES_CA", "Conseil d'Administration", "SD"},
            {"Assistante de Direction du PCA", "Executive Assistant to the Chairperson", "ASSISTANTE_DE_DIRECTION_DU_PCA_CA", "Conseil d'Administration", "CS"},
            {"Chef de bureau/CAB", "Head of Bureau/CAB", "CHEF_DE_BUREAU_CAB_CA", "Conseil d'Administration", "CB"},
            {"Personnel d'Appui/PCA", "Support Staff/PCA", "PERSONNEL_D_APPUI_PCA_CA", "Conseil d'Administration", "PA"},
            {"Agent d'éxécution/PCA", "Execution Staff/PCA", "AGENT_D_EXECUTION_PCA_CA", "Conseil d'Administration", "AL"},
            {"Chauffeur/PCA", "Driver/PCA", "CHAUFFEUR_PCA_CA", "Conseil d'Administration", "AL"},
            {"Directeur Général (DG)", "Director General (DG)", "DIRECTEUR_GENERAL_DG_DG", "Direction Générale", "DG"},
            {"Directeur Général Adjoint (DGA)", "Deputy Director General (DGA)", "DIRECTEUR_GENERAL_ADJOINT_DGA_DG", "Direction Générale", "DGA"},
            {"Attaché de Direction (Cabinet du DG)", "Directorate Attaché (Office of the DG)", "ATTACHE_DE_DIRECTION_CABINET_DU_DG_AD", "Attaché de Direction", "SD"},
            {"Assistante de Direction du DG", "Executive Assistant to the DG", "ASSISTANTE_DE_DIRECTION_DU_DG_DG", "Direction Générale", "CS"},
            {"Secrétaire du DGA", "Secretary to the DGA", "SECRETAIRE_DU_DGA_DG", "Direction Générale", "CS"},
            {"Cadre/CAB", "Staff Officer/CAB", "CADRE_CAB_DG", "Direction Générale", "CA"},
            {"Chef de bureau/CAB", "Head of Bureau/CAB", "CHEF_DE_BUREAU_CAB_DG", "Direction Générale", "CB"},
            {"Personnel d'Appui/CAB", "Support Staff/CAB", "PERSONNEL_D_APPUI_CAB_DG", "Direction Générale", "PA"},
            {"Agent d'éxécution/CAB", "Execution Staff/CAB", "AGENT_D_EXECUTION_CAB_DG", "Direction Générale", "AL"},
            {"Chauffeur/CAB", "Driver/CAB", "CHAUFFEUR_CAB_DG", "Direction Générale", "AL"},
            {"Conseiller Technique N°1", "Technical Adviser No. 1", "CONSEILLER_TECHNIQUE_N1_CDG", "Cabinet du Directeur Général", "D"},
            {"Conseiller Technique N°2", "Technical Adviser No. 2", "CONSEILLER_TECHNIQUE_N2_CDG", "Cabinet du Directeur Général", "D"},
            {"Conseiller Technique N°3", "Technical Adviser No. 3", "CONSEILLER_TECHNIQUE_N3_CDG", "Cabinet du Directeur Général", "D"},
            {"Inspecteur N°1", "Inspector No. 1", "INSPECTEUR_N1_CDG", "Cabinet du Directeur Général", "D"},
            {"Inspecteur N°2", "Inspector No. 2", "INSPECTEUR_N2_CDG", "Cabinet du Directeur Général", "D"},
            {"Inspecteur N°3", "Inspector No. 3", "INSPECTEUR_N3_CDG", "Cabinet du Directeur Général", "D"},
            {"Secrétaire du DGA", "Secretary to the DGA", "SECRETAIRE_DU_DGA_CDG", "Cabinet du Directeur Général", "CS"},
            {"Cadre/CAB", "Staff Officer/CAB", "CADRE_CAB_CDG", "Cabinet du Directeur Général", "CA"},
            {"Chef de bureau/CAB", "Head of Bureau/CAB", "CHEF_DE_BUREAU_CAB_CDG", "Cabinet du Directeur Général", "CB"},
            {"Personnel d'Appui/CAB", "Support Staff/CAB", "PERSONNEL_D_APPUI_CAB_CDG", "Cabinet du Directeur Général", "PA"},
            {"Agent d'éxécution/CAB", "Execution Staff/CAB", "AGENT_D_EXECUTION_CAB_CDG", "Cabinet du Directeur Général", "AL"},
            {"Chauffeur/CAB", "Driver/CAB", "CHAUFFEUR_CAB_CDG", "Cabinet du Directeur Général", "AL"},
            {"Responsable de l'Audit Interne", "Head of Internal Audit", "RESPONSABLE_DE_L_AUDIT_INTERNE_AI", "Audit Interne", "D"},
            {"Auditeur Interne N°1", "Internal Auditor No. 1", "AUDITEUR_INTERNE_N1_AI", "Audit Interne", "SD"},
            {"Auditeur Interne N°2", "Internal Auditor No. 2", "AUDITEUR_INTERNE_N2_AI", "Audit Interne", "SD"},
            {"Auditeur Interne N°3", "Internal Auditor No. 3", "AUDITEUR_INTERNE_N3_AI", "Audit Interne", "SD"},
            {"Auditeur Junior N°1", "Junior Auditor No. 1", "AUDITEUR_JUNIOR_N1_AI", "Audit Interne", "CS"},
            {"Auditeur Junior N°2", "Junior Auditor No. 2", "AUDITEUR_JUNIOR_N2_AI", "Audit Interne", "CS"},
            {"Auditeur Junior N°3", "Junior Auditor No. 3", "AUDITEUR_JUNIOR_N3_AI", "Audit Interne", "CS"},
            {"Auditeur Junior N°4", "Junior Auditor No. 4", "AUDITEUR_JUNIOR_N4_AI", "Audit Interne", "CS"},
            {"Secrétaire du AI", "Secretary of Internal Audit", "SECRETAIRE_DU_AI_AI", "Audit Interne", "CB"},
            {"Cadre/AI", "Staff Officer/AI", "CADRE_AI_AI", "Audit Interne", "CA"},
            {"Personnel d'Appui/AI", "Support Staff/AI", "PERSONNEL_D_APPUI_AI_AI", "Audit Interne", "PA"},
            {"Agent d'éxécution/AI", "Execution Staff/AI", "AGENT_D_EXECUTION_AI_AI", "Audit Interne", "AL"},
            {"Chauffeur/AI", "Driver/AI", "CHAUFFEUR_AI_AI", "Audit Interne", "AL"},
            {"Chef de Division", "Head of Division", "CHEF_DE_DIVISION_DS", "Division du Suivi", "D"},
            {"Chargé d'Études N°1", "Research Officer No. 1", "CHARGE_D_ETUDES_N1_DS", "Division du Suivi", "SD"},
            {"Chargé d'Études N°2", "Research Officer No. 2", "CHARGE_D_ETUDES_N2_DS", "Division du Suivi", "SD"},
            {"Chargé d'Études N°3", "Research Officer No. 3", "CHARGE_D_ETUDES_N3_DS", "Division du Suivi", "SD"},
            {"Chargé d'Études Assistant N°1", "Assistant Research Officer No. 1", "CHARGE_D_ETUDES_ASSISTANT_N1_DS", "Division du Suivi", "CS"},
            {"Chargé d'Études Assistant N°2", "Assistant Research Officer No. 2", "CHARGE_D_ETUDES_ASSISTANT_N2_DS", "Division du Suivi", "CS"},
            {"Chargé d'Études Assistant N°3", "Assistant Research Officer No. 3", "CHARGE_D_ETUDES_ASSISTANT_N3_DS", "Division du Suivi", "CS"},
            {"Chargé d'Études Assistant N°4", "Assistant Research Officer No. 4", "CHARGE_D_ETUDES_ASSISTANT_N4_DS", "Division du Suivi", "CS"},
            {"Secrétaire de la DS", "Secretary of the DS", "SECRETAIRE_DE_LA_DS_DS", "Division du Suivi", "CB"},
            {"Cadre/DS", "Staff Officer/DS", "CADRE_DS_DS", "Division du Suivi", "CA"},
            {"Personnel d'Appui/DS", "Support Staff/DS", "PERSONNEL_D_APPUI_DS_DS", "Division du Suivi", "PA"},
            {"Agent d'éxécution/DS", "Execution Staff/DS", "AGENT_D_EXECUTION_DS_DS", "Division du Suivi", "AL"},
            {"Chauffeur/DS", "Driver/DS", "CHAUFFEUR_DS_DS", "Division du Suivi", "AL"},
            {"Chef de Division", "Head of Division", "CHEF_DE_DIVISION_DCG", "Division du Contrôle de Gestion", "D"},
            {"Chef de Cellule du Contrôle Budgétaire", "Head of Budget Control Unit", "CHEF_DE_CELLULE_DU_CONTROLE_BUDGETAIRE_DCG", "Division du Contrôle de Gestion", "SD"},
            {"Chef de Cellule du Suivi de la Performance", "Head of Performance Monitoring Unit", "CHEF_DE_CELLULE_DU_SUIVI_DE_LA_PERFORMAN_DCG", "Division du Contrôle de Gestion", "SD"},
            {"Chargé d'Études Assistant N°1", "Assistant Research Officer No. 1", "CHARGE_D_ETUDES_ASSISTANT_N1_DCG", "Division du Contrôle de Gestion", "CS"},
            {"Chargé d'Études Assistant N°2", "Assistant Research Officer No. 2", "CHARGE_D_ETUDES_ASSISTANT_N2_DCG", "Division du Contrôle de Gestion", "CS"},
            {"Chargé d'Études Assistant N°3", "Assistant Research Officer No. 3", "CHARGE_D_ETUDES_ASSISTANT_N3_DCG", "Division du Contrôle de Gestion", "CS"},
            {"Chargé d'Études Assistant N°4", "Assistant Research Officer No. 4", "CHARGE_D_ETUDES_ASSISTANT_N4_DCG", "Division du Contrôle de Gestion", "CS"},
            {"Chargé d'Études Assistant N°5", "Assistant Research Officer No. 5", "CHARGE_D_ETUDES_ASSISTANT_N5_DCG", "Division du Contrôle de Gestion", "CS"},
            {"Chargé d'Études Assistant N°6", "Assistant Research Officer No. 6", "CHARGE_D_ETUDES_ASSISTANT_N6_DCG", "Division du Contrôle de Gestion", "CS"},
            {"Secrétaire de la DCG", "Secretary of the DCG", "SECRETAIRE_DE_LA_DCG_DCG", "Division du Contrôle de Gestion", "CB"},
            {"Cadre/DCG", "Staff Officer/DCG", "CADRE_DCG_DCG", "Division du Contrôle de Gestion", "CA"},
            {"Personnel d'Appui/DCG", "Support Staff/DCG", "PERSONNEL_D_APPUI_DCG_DCG", "Division du Contrôle de Gestion", "PA"},
            {"Agent d'éxécution/DCG", "Execution Staff/DCG", "AGENT_D_EXECUTION_DCG_DCG", "Division du Contrôle de Gestion", "AL"},
            {"Chauffeur/DCG", "Driver/DCG", "CHAUFFEUR_DCG_DCG", "Division du Contrôle de Gestion", "AL"},
            {"Chef de Cellule", "Head of Unit", "CHEF_DE_CELLULE_CSI", "Cellule des Systèmes d'Information", "SD"},
            {"Chargé d'Études Assistant N°1", "Assistant Research Officer No. 1", "CHARGE_D_ETUDES_ASSISTANT_N1_CSI", "Cellule des Systèmes d'Information", "CS"},
            {"Chargé d'Études Assistant N°2", "Assistant Research Officer No. 2", "CHARGE_D_ETUDES_ASSISTANT_N2_CSI", "Cellule des Systèmes d'Information", "CS"},
            {"Chargé d'Études Assistant N°3", "Assistant Research Officer No. 3", "CHARGE_D_ETUDES_ASSISTANT_N3_CSI", "Cellule des Systèmes d'Information", "CS"},
            {"Secrétaire de la CSI", "Secretary of the CSI", "SECRETAIRE_DE_LA_CSI_CSI", "Cellule des Systèmes d'Information", "CB"},
            {"Cadre/CSI", "Staff Officer/CSI", "CADRE_CSI_CSI", "Cellule des Systèmes d'Information", "CA"},
            {"Personnel d'Appui/CSI", "Support Staff/CSI", "PERSONNEL_D_APPUI_CSI_CSI", "Cellule des Systèmes d'Information", "PA"},
            {"Agent d'éxécution/CSI", "Execution Staff/CSI", "AGENT_D_EXECUTION_CSI_CSI", "Cellule des Systèmes d'Information", "AL"},
            {"Chauffeur/CSI", "Driver/CSI", "CHAUFFEUR_CSI_CSI", "Cellule des Systèmes d'Information", "AL"},
            {"Chef de Cellule des Plateformes Techniques", "Head of Technical Platforms Unit", "CHEF_DE_CELLULE_DES_PLATEFORMES_TECHNIQU_CPT", "Cellule des Plateformes Techniques", "SD"},
            {"Chargé d'Études Assistant N°1", "Assistant Research Officer No. 1", "CHARGE_D_ETUDES_ASSISTANT_N1_CPT", "Cellule des Plateformes Techniques", "CS"},
            {"Chargé d'Études Assistant N°2", "Assistant Research Officer No. 2", "CHARGE_D_ETUDES_ASSISTANT_N2_CPT", "Cellule des Plateformes Techniques", "CS"},
            {"Chargé d'Études Assistant N°3", "Assistant Research Officer No. 3", "CHARGE_D_ETUDES_ASSISTANT_N3_CPT", "Cellule des Plateformes Techniques", "CS"},
            {"Secrétaire de la CPT", "Secretary of the CPT", "SECRETAIRE_DE_LA_CPT_CPT", "Cellule des Plateformes Techniques", "CB"},
            {"Cadre/CPT", "Staff Officer/CPT", "CADRE_CPT_CPT", "Cellule des Plateformes Techniques", "CA"},
            {"Personnel d'Appui/CPT", "Support Staff/CPT", "PERSONNEL_D_APPUI_CPT_CPT", "Cellule des Plateformes Techniques", "PA"},
            {"Agent d'éxécution/CPT", "Execution Staff/CPT", "AGENT_D_EXECUTION_CPT_CPT", "Cellule des Plateformes Techniques", "AL"},
            {"Chauffeur/CPT", "Driver/CPT", "CHAUFFEUR_CPT_CPT", "Cellule des Plateformes Techniques", "AL"},
            {"Chef de Cellule", "Head of Unit", "CHEF_DE_CELLULE_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "SD"},
            {"Chargé d'Études Assistant N°1", "Assistant Research Officer No. 1", "CHARGE_D_ETUDES_ASSISTANT_N1_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "CS"},
            {"Chargé d'Études Assistant N°2", "Assistant Research Officer No. 2", "CHARGE_D_ETUDES_ASSISTANT_N2_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "CS"},
            {"Chargé d'Études Assistant N°3", "Assistant Research Officer No. 3", "CHARGE_D_ETUDES_ASSISTANT_N3_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "CS"},
            {"Secrétaire de la CTIPB", "Secretary of the CTIPB", "SECRETAIRE_DE_LA_CTIPB_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "CB"},
            {"Cadre/CTIPB", "Staff Officer/CTIPB", "CADRE_CTIPB_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "CA"},
            {"Personnel d'Appui/CTIPB", "Support Staff/CTIPB", "PERSONNEL_D_APPUI_CTIPB_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "PA"},
            {"Agent d'éxécution/CTIPB", "Execution Staff/CTIPB", "AGENT_D_EXECUTION_CTIPB_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "AL"},
            {"Chauffeur/CTIPB", "Driver/CTIPB", "CHAUFFEUR_CTIPB_CTIPB", "Cellule de la Traduction, de l'Interprétariat et de la Promotion du Bilinguisme", "AL"},
            {"Sous-directeur de l'Accueil, du Courrier et de Liaison", "Deputy Director of Reception, Mail and Liaison", "SOUS_DIRECTEUR_DE_L_ACCUEIL_DU_COURRIER__SDACL", "Sous-Direction de l'Accueil, du Courrier et de la Liaison", "SD"},
            {"Chef de Service du courrier", "Head of Mail Service", "CHEF_DE_SERVICE_DU_COURRIER_SDACL", "Sous-Direction de l'Accueil, du Courrier et de la Liaison", "CS"},
            {"Chef de Service du courrier electronique", "Head of Electronic Mail Service", "CHEF_DE_SERVICE_DU_COURRIER_ELECTRONIQUE_SDACL", "Sous-Direction de l'Accueil, du Courrier et de la Liaison", "CS"},
            {"Chef de Bureau relance", "Head of Follow-up Bureau", "CHEF_DE_BUREAU_RELANCE_SDACL", "Sous-Direction de l'Accueil, du Courrier et de la Liaison", "CB"},
            {"Chef de Bureau fomalisation", "Head of Formalization Bureau", "CHEF_DE_BUREAU_FOMALISATION_SDACL", "Sous-Direction de l'Accueil, du Courrier et de la Liaison", "CB"},
            {"Comptable Matières N°1", "Property Accountant No. 1", "COMPTABLE_MATIERES_N1_CM", "Comptables Matières", "CS"},
            {"Comptable Matières N°2", "Property Accountant No. 2", "COMPTABLE_MATIERES_N2_CM", "Comptables Matières", "CB"},
            {"Comptable Matières N°3", "Property Accountant No. 3", "COMPTABLE_MATIERES_N3_CM", "Comptables Matières", "CB"},
            {"Directeur", "Director", "DIRECTEUR_DT", "Direction Technique", "D"},
            {"Sous-direction de la Gestion des Ressources Techniques et du Service Universel", "Deputy Director of Technical Resources Management and Universal Service", "SOUS_DIRECTION_DE_LA_GESTION_DES_RESSOUR_SDGRT", "Sous-Direction de la Gestion des Ressources Techniques et du Service Universel", "SD"},
            {"Chef de Service de la Gestion des Ressources en numerotation", "Head of Numbering Resources Management Service", "CHEF_DE_SERVICE_DE_LA_GESTION_DES_RESSOU_SDGRT", "Sous-Direction de la Gestion des Ressources Techniques et du Service Universel", "CS"},
            {"Chef de Service des Obligations Technique", "Head of Technical Obligations Service", "CHEF_DE_SERVICE_DES_OBLIGATIONS_TECHNIQU_SDGRT", "Sous-Direction de la Gestion des Ressources Techniques et du Service Universel", "CS"},
            {"Chef de Service du Service Universel", "Head of Universal Service", "CHEF_DE_SERVICE_DU_SERVICE_UNIVERSEL_SDGRT", "Sous-Direction de la Gestion des Ressources Techniques et du Service Universel", "CS"},
            {"Sous-direction des Normes, des Agrément de la Sécurité Electronique", "Deputy Director of Standards and Electronic Security Approvals", "SOUS_DIRECTION_DES_NORMES_DES_AGREMENT_D_SDNSE", "Sous-Direction des Normes, de la Sécurité Électronique et des Agréments", "SD"},
            {"Chef de Service des Homologations", "Head of Homologation Service", "CHEF_DE_SERVICE_DES_HOMOLOGATIONS_SDNSE", "Sous-Direction des Normes, de la Sécurité Électronique et des Agréments", "CS"},
            {"Chef de Service des Agréments", "Head of Approvals Service", "CHEF_DE_SERVICE_DES_AGREMENTS_SDNSE", "Sous-Direction des Normes, de la Sécurité Électronique et des Agréments", "CS"},
            {"Chef de Service de la sécurité Electronique", "Head of Electronic Security Service", "CHEF_DE_SERVICE_DE_LA_SECURITE_ELECTRONI_SDNSE", "Sous-Direction des Normes, de la Sécurité Électronique et des Agréments", "CS"},
            {"Secrétaire DT", "Secretary, DT", "SECRETAIRE_DT_DT", "Direction Technique", "CB"},
            {"Cadre/DT", "Staff Officer/DT", "CADRE_DT_DT", "Direction Technique", "CA"},
            {"Personnel d'Appui/DT", "Support Staff/DT", "PERSONNEL_D_APPUI_DT_DT", "Direction Technique", "PA"},
            {"Agent d'éxécution/DT", "Execution Staff/DT", "AGENT_D_EXECUTION_DT_DT", "Direction Technique", "AL"},
            {"Chauffeur/DT", "Driver/DT", "CHAUFFEUR_DT_DT", "Direction Technique", "AL"},
            {"Directeur", "Director", "DIRECTEUR_DGF", "Direction de la Gestion des Fréquences", "D"},
            {"Sous-direction des Études, de la Planification et de l'Ingénierie du Spectre", "Deputy Director of Spectrum Studies, Planning and Engineering", "SOUS_DIRECTION_DES_ETUDES_DE_LA_PLANIFIC_SDEPI", "Sous-Direction des Études, de la Planification et de l'Ingénierie du Spectre", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDEPI", "Sous-Direction des Études, de la Planification et de l'Ingénierie du Spectre", "CS"},
            {"Sous-direction de la Gestion Administrative du Spectre", "Deputy Director of Spectrum Administrative Management", "SOUS_DIRECTION_DE_LA_GESTION_ADMINISTRAT_SDGAS", "Sous-Direction de la Gestion Administrative du Spectre", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDGAS", "Sous-Direction de la Gestion Administrative du Spectre", "CS"},
            {"Secrétaire DT", "Secretary, DT", "SECRETAIRE_DT_DGF", "Direction de la Gestion des Fréquences", "CB"},
            {"Cadre/DT", "Staff Officer/DT", "CADRE_DT_DGF", "Direction de la Gestion des Fréquences", "CA"},
            {"Personnel d'Appui/DT", "Support Staff/DT", "PERSONNEL_D_APPUI_DT_DGF", "Direction de la Gestion des Fréquences", "PA"},
            {"Agent d'éxécution/DT", "Execution Staff/DT", "AGENT_D_EXECUTION_DT_DGF", "Direction de la Gestion des Fréquences", "AL"},
            {"Chauffeur/DT", "Driver/DT", "CHAUFFEUR_DT_DGF", "Direction de la Gestion des Fréquences", "AL"},
            {"Directeur", "Director", "DIRECTEUR_DLCI", "Direction des Licences, de la Concurrence et de l'Interconnexion", "D"},
            {"Sous-direction des Licences", "Deputy Director of Licensing", "SOUS_DIRECTION_DES_LICENCES_SDL", "Sous-Direction des Licences", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDL", "Sous-Direction des Licences", "CS"},
            {"Sous-direction de l'Analyse et de l'Évaluation Économique", "Deputy Director of Economic Analysis and Evaluation", "SOUS_DIRECTION_DE_L_ANALYSE_ET_DE_L_EVAL_SDAEE", "Sous-Direction de l'Analyse et de l'Évaluation Économique", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDAEE", "Sous-Direction de l'Analyse et de l'Évaluation Économique", "CS"},
            {"Sous-direction de l'Interconnexion et des Infrastructures", "Deputy Director of Interconnection and Infrastructure", "SOUS_DIRECTION_DE_L_INTERCONNEXION_ET_DE_SDIIC", "Sous-Direction de l'Interconnexion et des Infrastructures des Communications Électroniques", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDIIC", "Sous-Direction de l'Interconnexion et des Infrastructures des Communications Électroniques", "CS"},
            {"Secrétaire DLCI", "Secretary, DLCI", "SECRETAIRE_DLCI_DLCI", "Direction des Licences, de la Concurrence et de l'Interconnexion", "CB"},
            {"Cadre/DLCI", "Staff Officer/DLCI", "CADRE_DLCI_DLCI", "Direction des Licences, de la Concurrence et de l'Interconnexion", "CA"},
            {"Personnel d'Appui/DLCI", "Support Staff/DLCI", "PERSONNEL_D_APPUI_DLCI_DLCI", "Direction des Licences, de la Concurrence et de l'Interconnexion", "PA"},
            {"Agent d'éxécution/DLCI", "Execution Staff/DLCI", "AGENT_D_EXECUTION_DLCI_DLCI", "Direction des Licences, de la Concurrence et de l'Interconnexion", "AL"},
            {"Chauffeur/DLCI", "Driver/DLCI", "CHAUFFEUR_DLCI_DLCI", "Direction des Licences, de la Concurrence et de l'Interconnexion", "AL"},
            {"Chef de Brigade", "Head of Brigade", "CHEF_DE_BRIGADE_BC", "Brigade des Contrôles", "D"},
            {"Unité de Contrôle des Titres d'Exploitation et des Ressources Rares", "Head of Operating Licenses and Scarce Resources Control Unit", "UNITE_DE_CONTROLE_DES_TITRES_D_EXPLOITAT_UCTER", "Unité de Contrôle des Titres d'Exploitation et des Ressources Rares", "SD"},
            {"Chef d'Unité Adjoint", "Deputy Unit Head", "CHEF_D_UNITE_ADJOINT_UCTER", "Unité de Contrôle des Titres d'Exploitation et des Ressources Rares", "CS"},
            {"Unité de Contrôle de la Qualité de Service et des Infrastructures", "Head of Quality of Service and Infrastructure Control Unit", "UNITE_DE_CONTROLE_DE_LA_QUALITE_DE_SERVI_UCQSI", "Unité de Contrôle de la Qualité de Service et des Infrastructures", "SD"},
            {"Chef d'Unité Adjoint", "Deputy Unit Head", "CHEF_D_UNITE_ADJOINT_UCQSI", "Unité de Contrôle de la Qualité de Service et des Infrastructures", "CS"},
            {"Unité des Contrôles Administratifs et Tarifaires", "Head of Administrative and Tariff Controls Unit", "UNITE_DES_CONTROLES_ADMINISTRATIFS_ET_TA_UCAT", "Unité des Contrôles Administratifs et Tarifaires", "SD"},
            {"Chef d'Unité Adjoint", "Deputy Unit Head", "CHEF_D_UNITE_ADJOINT_UCAT", "Unité des Contrôles Administratifs et Tarifaires", "CS"},
            {"Secrétaire BC", "Secretary, BC", "SECRETAIRE_BC_BC", "Brigade des Contrôles", "CB"},
            {"Cadre/BC", "Staff Officer/BC", "CADRE_BC_BC", "Brigade des Contrôles", "CA"},
            {"Personnel d'Appui/BC", "Support Staff/BC", "PERSONNEL_D_APPUI_BC_BC", "Brigade des Contrôles", "PA"},
            {"Agent d'éxécution/BC", "Execution Staff/BC", "AGENT_D_EXECUTION_BC_BC", "Brigade des Contrôles", "AL"},
            {"Chauffeur/BC", "Driver/BC", "CHAUFFEUR_BC_BC", "Brigade des Contrôles", "AL"},
            {"Directeur", "Director", "DIRECTEUR_DSP", "Direction de la Stratégie et de la Prospective", "D"},
            {"Sous-direction de la Planification Stratégique et de la Prospective", "Deputy Director of Strategic Planning and Foresight", "SOUS_DIRECTION_DE_LA_PLANIFICATION_STRAT_SDPSP", "Sous-Direction de la Planification Stratégique et de la Prospective", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDPSP", "Sous-Direction de la Planification Stratégique et de la Prospective", "CS"},
            {"Sous-direction du Développement des Communications Électroniques", "Deputy Director of Electronic Communications Development", "SOUS_DIRECTION_DU_DEVELOPPEMENT_DES_COMM_SDDCE", "Sous-Direction du Développement des Communications Électroniques", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDDCE", "Sous-Direction du Développement des Communications Électroniques", "CS"},
            {"Sous-direction des Technologies Innovantes", "Deputy Director of Innovative Technologies", "SOUS_DIRECTION_DES_TECHNOLOGIES_INNOVANT_SDTI", "Sous-Direction des Technologies Innovantes", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDTI", "Sous-Direction des Technologies Innovantes", "CS"},
            {"Secrétaire DSP", "Secretary, DSP", "SECRETAIRE_DSP_DSP", "Direction de la Stratégie et de la Prospective", "CB"},
            {"Cadre/DSP", "Staff Officer/DSP", "CADRE_DSP_DSP", "Direction de la Stratégie et de la Prospective", "CA"},
            {"Personnel d'Appui/DSP", "Support Staff/DSP", "PERSONNEL_D_APPUI_DSP_DSP", "Direction de la Stratégie et de la Prospective", "PA"},
            {"Agent d'éxécution/DSP", "Execution Staff/DSP", "AGENT_D_EXECUTION_DSP_DSP", "Direction de la Stratégie et de la Prospective", "AL"},
            {"Chauffeur/DSP", "Driver/DSP", "CHAUFFEUR_DSP_DSP", "Direction de la Stratégie et de la Prospective", "AL"},
            {"Chef de Division", "Head of Division", "CHEF_DE_DIVISION_DAJPC", "Division des Affaires Juridiques et de la Protection du Consommateur", "D"},
            {"Chef Cellule de la Réglementation", "Head of Regulations Unit", "CHEF_CELLULE_DE_LA_REGLEMENTATION_CR", "Cellule de la Réglementation", "SD"},
            {"Chargé d'Études Assistant", "Assistant Research Officer", "CHARGE_D_ETUDES_ASSISTANT_CR", "Cellule de la Réglementation", "CS"},
            {"Chef de Cellule du Contentieux", "Head of Litigation Unit", "CHEF_DE_CELLULE_DU_CONTENTIEUX_CC", "Cellule du Contentieux", "SD"},
            {"Chargé d'Études Assistant", "Assistant Research Officer", "CHARGE_D_ETUDES_ASSISTANT_CC", "Cellule du Contentieux", "CS"},
            {"Chef de Cellule de la Protection du Consommateur", "Head of Consumer Protection Unit", "CHEF_DE_CELLULE_DE_LA_PROTECTION_DU_CONS_CPC", "Cellule de la Protection du Consommateur", "SD"},
            {"Chargé d'Études Assistant", "Assistant Research Officer", "CHARGE_D_ETUDES_ASSISTANT_CPC", "Cellule de la Protection du Consommateur", "CS"},
            {"Secrétaire DAJPC", "Secretary, DAJPC", "SECRETAIRE_DAJPC_DAJPC", "Division des Affaires Juridiques et de la Protection du Consommateur", "CB"},
            {"Cadre/DAJPC", "Staff Officer/DAJPC", "CADRE_DAJPC_DAJPC", "Division des Affaires Juridiques et de la Protection du Consommateur", "CA"},
            {"Personnel d'Appui/DAJPC", "Support Staff/DAJPC", "PERSONNEL_D_APPUI_DAJPC_DAJPC", "Division des Affaires Juridiques et de la Protection du Consommateur", "PA"},
            {"Agent d'éxécution/DAJPC", "Execution Staff/DAJPC", "AGENT_D_EXECUTION_DAJPC_DAJPC", "Division des Affaires Juridiques et de la Protection du Consommateur", "AL"},
            {"Chauffeur/DAJPC", "Driver/DAJPC", "CHAUFFEUR_DAJPC_DAJPC", "Division des Affaires Juridiques et de la Protection du Consommateur", "AL"},
            {"Chef de Division", "Head of Division", "CHEF_DE_DIVISION_DCC", "Division de la Communication et de la Coopération", "D"},
            {"Chef de Cellule de la Communication et des Relations Publiques", "Head of Communication and Public Relations Unit", "CHEF_DE_CELLULE_DE_LA_COMMUNICATION_ET_D_CCRP", "Cellule de la Communication et des Relations Publiques", "SD"},
            {"Chargé d'Études Assistant", "Assistant Research Officer", "CHARGE_D_ETUDES_ASSISTANT_CCRP", "Cellule de la Communication et des Relations Publiques", "CS"},
            {"Chef de Cellule de la Coopération", "Head of Cooperation Unit", "CHEF_DE_CELLULE_DE_LA_COOPERATION_CC", "Cellule de la Coopération", "SD"},
            {"Chargé d'Études Assistant", "Assistant Research Officer", "CHARGE_D_ETUDES_ASSISTANT_CC_2", "Cellule de la Coopération", "CS"},
            {"Chef de Centre de Documentation et des Archives", "Head of Documentation and Archives Center", "CHEF_DE_CENTRE_DE_DOCUMENTATION_ET_DES_A_CDA", "Centre de la Documentation et des Archives", "SD"},
            {"Chargé d'Études Assistant", "Assistant Research Officer", "CHARGE_D_ETUDES_ASSISTANT_CDA", "Centre de la Documentation et des Archives", "CS"},
            {"Secrétaire DCC", "Secretary, DCC", "SECRETAIRE_DCC_DCC", "Division de la Communication et de la Coopération", "CB"},
            {"Cadre/DCC", "Staff Officer/DCC", "CADRE_DCC_DCC", "Division de la Communication et de la Coopération", "CA"},
            {"Personnel d'Appui/DCC", "Support Staff/DCC", "PERSONNEL_D_APPUI_DCC_DCC", "Division de la Communication et de la Coopération", "PA"},
            {"Agent d'éxécution/DCC", "Execution Staff/DCC", "AGENT_D_EXECUTION_DCC_DCC", "Division de la Communication et de la Coopération", "AL"},
            {"Chauffeur/DCC", "Driver/DCC", "CHAUFFEUR_DCC_DCC", "Division de la Communication et de la Coopération", "AL"},
            {"Sous-directeur de Sous-direction du Budget", "Deputy Director of the Budget Sub-Directorate", "SOUS_DIRECTEUR_DE_SOUS_DIRECTION_DU_BUDG_SDB", "Sous-Direction du Budget", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDB", "Sous-Direction du Budget", "CS"},
            {"Sous-directeur de la Comptabilité", "Deputy Director of Accounting", "SOUS_DIRECTEUR_DE_LA_COMPTABILITE_SDC", "Sous-Direction de la Comptabilité", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDC", "Sous-Direction de la Comptabilité", "CS"},
            {"Sous-directeur de la Trésorerie", "Deputy Director of Treasury", "SOUS_DIRECTEUR_DE_LA_TRESORERIE_SDT", "Sous-Direction de la Trésorerie", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDT", "Sous-Direction de la Trésorerie", "CS"},
            {"Sous-directeur des Marchés", "Deputy Director of Procurement", "SOUS_DIRECTEUR_DES_MARCHES_SDM", "Sous-Direction des Marchés", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDM", "Sous-Direction des Marchés", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_DF", "Direction des Finances", "CS"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_DE_BUREAU_DF", "Direction des Finances", "CB"},
            {"Secrétaire DF", "Secretary, DF", "SECRETAIRE_DF_DF", "Direction des Finances", "CB"},
            {"Cadre/DF", "Staff Officer/DF", "CADRE_DF_DF", "Direction des Finances", "CA"},
            {"Personnel d'Appui/DF", "Support Staff/DF", "PERSONNEL_D_APPUI_DF_DF", "Direction des Finances", "PA"},
            {"Agent d'éxécution/DF", "Execution Staff/DF", "AGENT_D_EXECUTION_DF_DF", "Direction des Finances", "AL"},
            {"Chauffeur/DF", "Driver/DF", "CHAUFFEUR_DF_DF", "Direction des Finances", "AL"},
            {"Directeur", "Director", "DIRECTEUR_DP", "Direction du Patrimoine", "D"},
            {"Sous-directeur des Moyens et Équipements Techniques", "Deputy Director of Technical Resources and Equipment", "SOUS_DIRECTEUR_DES_MOYENS_ET_EQUIPEMENTS_SDMET", "Sous-Direction des Moyens et Équipements Techniques", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDMET", "Sous-Direction des Moyens et Équipements Techniques", "CS"},
            {"Sous-directeur des Biens Meubles et Immeubles", "Deputy Director of Movable and Immovable Property", "SOUS_DIRECTEUR_DES_BIENS_MEUBLES_ET_IMME_SDBMI", "Sous-Direction des Biens Meubles et Immeubles", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDBMI", "Sous-Direction des Biens Meubles et Immeubles", "CS"},
            {"Sous-directeur de la Comptabilité-Matières", "Deputy Director of Property Accounting", "SOUS_DIRECTEUR_DE_LA_COMPTABILITE_MATIER_SDCM", "Sous-Direction de la Comptabilité-Matières", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDCM", "Sous-Direction de la Comptabilité-Matières", "CS"},
            {"Secrétaire DP", "Secretary, DP", "SECRETAIRE_DP_DP", "Direction du Patrimoine", "CB"},
            {"Cadre/DP", "Staff Officer/DP", "CADRE_DP_DP", "Direction du Patrimoine", "CA"},
            {"Personnel d'Appui/DP", "Support Staff/DP", "PERSONNEL_D_APPUI_DP_DP", "Direction du Patrimoine", "PA"},
            {"Agent d'éxécution/DP", "Execution Staff/DP", "AGENT_D_EXECUTION_DP_DP", "Direction du Patrimoine", "AL"},
            {"Chauffeur/DP", "Driver/DP", "CHAUFFEUR_DP_DP", "Direction du Patrimoine", "AL"},
            {"Directeur", "Director", "DIRECTEUR_DR", "Direction du Recouvrement", "D"},
            {"Sous-directeur de la Facturation", "Deputy Director of Billing", "SOUS_DIRECTEUR_DE_LA_FACTURATION_SDF", "Sous-Direction de la Facturation", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDF", "Sous-Direction de la Facturation", "CS"},
            {"Sous-directeur du Suivi du Recouvrement", "Deputy Director of Collection Monitoring", "SOUS_DIRECTEUR_DU_SUIVI_DU_RECOUVREMENT_SDSR", "Sous-Direction du Suivi du Recouvrement", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDSR", "Sous-Direction du Suivi du Recouvrement", "CS"},
            {"Secrétaire DREC", "Secretary, DREC", "SECRETAIRE_DREC_DR", "Direction du Recouvrement", "CB"},
            {"Cadre/DREC", "Staff Officer/DREC", "CADRE_DREC_DR", "Direction du Recouvrement", "CA"},
            {"Personnel d'Appui/DREC", "Support Staff/DREC", "PERSONNEL_D_APPUI_DREC_DR", "Direction du Recouvrement", "PA"},
            {"Agent d'éxécution/DREC", "Execution Staff/DREC", "AGENT_D_EXECUTION_DREC_DR", "Direction du Recouvrement", "AL"},
            {"Chauffeur/DREC", "Driver/DREC", "CHAUFFEUR_DREC_DR", "Direction du Recouvrement", "AL"},
            {"Directeur", "Director", "DIRECTEUR_DRH", "Direction des Ressources Humaines", "D"},
            {"Sous-directeur de la Gestion Administrative des RH", "Deputy Director of HR Administration", "SOUS_DIRECTEUR_DE_LA_GESTION_ADMINISTRAT_SDGAR", "Sous-Direction de la Gestion Administrative des Ressources Humaines", "SD"},
            {"Chef de Service du personnel", "Head of Personnel Service", "CHEF_DE_SERVICE_DU_PERSONNEL_SDGAR", "Sous-Direction de la Gestion Administrative des Ressources Humaines", "CS"},
            {"Chef de Service du perfectionnement", "Head of Staff Development Service", "CHEF_DE_SERVICE_DU_PERFECTIONNEMENT_SDGAR", "Sous-Direction de la Gestion Administrative des Ressources Humaines", "CS"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_DE_BUREAU_DRH", "Direction des Ressources Humaines", "CB"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDGAR", "Sous-Direction de la Gestion Administrative des Ressources Humaines", "CS"},
            {"Sous-directeur du Développement des RH", "Deputy Director of HR Development", "SOUS_DIRECTEUR_DU_DEVELOPPEMENT_DES_RH_SDDRH", "Sous-Direction du Développement des Ressources Humaines", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDDRH", "Sous-Direction du Développement des Ressources Humaines", "CS"},
            {"Sous-directeur de la Solde", "Deputy Director of Payroll", "SOUS_DIRECTEUR_DE_LA_SOLDE_SDS", "Sous-Direction de la Solde", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_SDS", "Sous-Direction de la Solde", "CS"},
            {"Secrétaire DRH", "Secretary, DRH", "SECRETAIRE_DRH_DRH", "Direction des Ressources Humaines", "CB"},
            {"Cadre/DRH", "Staff Officer/DRH", "CADRE_DRH_DRH", "Direction des Ressources Humaines", "CA"},
            {"Personnel d'Appui/DRH", "Support Staff/DRH", "PERSONNEL_D_APPUI_DRH_DRH", "Direction des Ressources Humaines", "PA"},
            {"Agent d'éxécution/DRH", "Execution Staff/DRH", "AGENT_D_EXECUTION_DRH_DRH", "Direction des Ressources Humaines", "AL"},
            {"Chauffeur/DRH", "Driver/DRH", "CHAUFFEUR_DRH_DRH", "Direction des Ressources Humaines", "AL"},
            {"Délégué Régional", "Regional Delegate", "DELEGUE_REGIONAL_DRD", "Délégation Régionale de Douala", "D"},
            {"Délégué Régional", "Regional Delegate", "DELEGUE_REGIONAL_DRY", "Délégation Régionale de Yaoundé", "D"},
            {"Délégué Régional", "Regional Delegate", "DELEGUE_REGIONAL_DRG", "Délégation Régionale de Garoua", "D"},
            {"Délégué Régional", "Regional Delegate", "DELEGUE_REGIONAL_DRB", "Délégation Régionale de Bamenda", "D"},
            {"Délégué Régional Adjoint", "Deputy Regional Delegate", "DELEGUE_REGIONAL_ADJOINT_DRD", "Délégation Régionale de Douala", "SD"},
            {"Délégué Régional Adjoint", "Deputy Regional Delegate", "DELEGUE_REGIONAL_ADJOINT_DRY", "Délégation Régionale de Yaoundé", "SD"},
            {"Délégué Régional Adjoint", "Deputy Regional Delegate", "DELEGUE_REGIONAL_ADJOINT_DRG", "Délégation Régionale de Garoua", "SD"},
            {"Délégué Régional Adjoint", "Deputy Regional Delegate", "DELEGUE_REGIONAL_ADJOINT_DRB", "Délégation Régionale de Bamenda", "SD"},
            {"Sous-directeur Technique", "Deputy Director for Technical Affairs", "SOUS_DIRECTEUR_TECHNIQUE_DRD", "Délégation Régionale de Douala", "SD"},
            {"Sous-directeur Technique", "Deputy Director for Technical Affairs", "SOUS_DIRECTEUR_TECHNIQUE_DRY", "Délégation Régionale de Yaoundé", "SD"},
            {"Sous-directeur Technique", "Deputy Director for Technical Affairs", "SOUS_DIRECTEUR_TECHNIQUE_DRG", "Délégation Régionale de Garoua", "SD"},
            {"Sous-directeur Technique", "Deputy Director for Technical Affairs", "SOUS_DIRECTEUR_TECHNIQUE_DRB", "Délégation Régionale de Bamenda", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Sous-directeur des Ressources Humaines, Financières et du Patrimoine", "Deputy Director of Human, Financial and Property Resources", "SOUS_DIRECTEUR_DES_RESSOURCES_HUMAINES_F_DRD", "Délégation Régionale de Douala", "SD"},
            {"Sous-directeur des Ressources Humaines, Financières et du Patrimoine", "Deputy Director of Human, Financial and Property Resources", "SOUS_DIRECTEUR_DES_RESSOURCES_HUMAINES_F_DRY", "Délégation Régionale de Yaoundé", "SD"},
            {"Sous-directeur des Ressources Humaines, Financières et du Patrimoine", "Deputy Director of Human, Financial and Property Resources", "SOUS_DIRECTEUR_DES_RESSOURCES_HUMAINES_F_DRG", "Délégation Régionale de Garoua", "SD"},
            {"Sous-directeur des Ressources Humaines, Financières et du Patrimoine", "Deputy Director of Human, Financial and Property Resources", "SOUS_DIRECTEUR_DES_RESSOURCES_HUMAINES_F_DRB", "Délégation Régionale de Bamenda", "SD"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_RH_FINANCES_PATRIMOINE_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_RH_FINANCES_PATRIMOINE_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_RH_FINANCES_PATRIMOINE_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Service", "Head of Service", "CHEF_DE_SERVICE_RH_FINANCES_PATRIMOINE_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef de Service du Contrôle Budgétaire", "Head of Budget Control Service", "CHEF_DE_SERVICE_DU_CONTROLE_BUDGETAIRE_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Service du Contrôle Budgétaire", "Head of Budget Control Service", "CHEF_DE_SERVICE_DU_CONTROLE_BUDGETAIRE_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Service du Contrôle Budgétaire", "Head of Budget Control Service", "CHEF_DE_SERVICE_DU_CONTROLE_BUDGETAIRE_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Service du Contrôle Budgétaire", "Head of Budget Control Service", "CHEF_DE_SERVICE_DU_CONTROLE_BUDGETAIRE_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef de Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur", "Head of Legal Affairs, Litigation and Consumer Protection Service", "CHEF_DE_SERVICE_DES_AFFAIRES_JURIDIQUES__DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur", "Head of Legal Affairs, Litigation and Consumer Protection Service", "CHEF_DE_SERVICE_DES_AFFAIRES_JURIDIQUES__DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur", "Head of Legal Affairs, Litigation and Consumer Protection Service", "CHEF_DE_SERVICE_DES_AFFAIRES_JURIDIQUES__DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Service des Affaires Juridiques, du Contentieux et de la Protection du Consommateur", "Head of Legal Affairs, Litigation and Consumer Protection Service", "CHEF_DE_SERVICE_DES_AFFAIRES_JURIDIQUES__DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef de Service de la Comptabilité-Matières", "Head of Property Accounting Service", "CHEF_DE_SERVICE_DE_LA_COMPTABILITE_MATIE_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Service de la Comptabilité-Matières", "Head of Property Accounting Service", "CHEF_DE_SERVICE_DE_LA_COMPTABILITE_MATIE_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Service de la Comptabilité-Matières", "Head of Property Accounting Service", "CHEF_DE_SERVICE_DE_LA_COMPTABILITE_MATIE_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Service de la Comptabilité-Matières", "Head of Property Accounting Service", "CHEF_DE_SERVICE_DE_LA_COMPTABILITE_MATIE_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef de Centre d'Exploitation Spécialisé", "Head of Specialized Operations Center", "CHEF_DE_CENTRE_D_EXPLOITATION_SPECIALISE_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Centre d'Exploitation Spécialisé", "Head of Specialized Operations Center", "CHEF_DE_CENTRE_D_EXPLOITATION_SPECIALISE_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Centre d'Exploitation Spécialisé", "Head of Specialized Operations Center", "CHEF_DE_CENTRE_D_EXPLOITATION_SPECIALISE_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Centre d'Exploitation Spécialisé", "Head of Specialized Operations Center", "CHEF_DE_CENTRE_D_EXPLOITATION_SPECIALISE_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef de Centre de Contrôle des Fréquences", "Head of Frequency Control Center", "CHEF_DE_CENTRE_DE_CONTROLE_DES_FREQUENCE_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Centre de Contrôle des Fréquences", "Head of Frequency Control Center", "CHEF_DE_CENTRE_DE_CONTROLE_DES_FREQUENCE_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Centre de Contrôle des Fréquences", "Head of Frequency Control Center", "CHEF_DE_CENTRE_DE_CONTROLE_DES_FREQUENCE_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Centre de Contrôle des Fréquences", "Head of Frequency Control Center", "CHEF_DE_CENTRE_DE_CONTROLE_DES_FREQUENCE_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef d'Antenne", "Branch Office Head", "CHEF_D_ANTENNE_DRD", "Délégation Régionale de Douala", "SD"},
            {"Chef d'Antenne", "Branch Office Head", "CHEF_D_ANTENNE_DRY", "Délégation Régionale de Yaoundé", "SD"},
            {"Chef d'Antenne", "Branch Office Head", "CHEF_D_ANTENNE_DRG", "Délégation Régionale de Garoua", "SD"},
            {"Chef d'Antenne", "Branch Office Head", "CHEF_D_ANTENNE_DRB", "Délégation Régionale de Bamenda", "SD"},
            {"Chef de Service (Technique / Administratif)", "Head of Service (Technical / Administrative)", "CHEF_DE_SERVICE_TECHNIQUE_ADMINISTRATIF_DRD", "Délégation Régionale de Douala", "CS"},
            {"Chef de Service (Technique / Administratif)", "Head of Service (Technical / Administrative)", "CHEF_DE_SERVICE_TECHNIQUE_ADMINISTRATIF_DRY", "Délégation Régionale de Yaoundé", "CS"},
            {"Chef de Service (Technique / Administratif)", "Head of Service (Technical / Administrative)", "CHEF_DE_SERVICE_TECHNIQUE_ADMINISTRATIF_DRG", "Délégation Régionale de Garoua", "CS"},
            {"Chef de Service (Technique / Administratif)", "Head of Service (Technical / Administrative)", "CHEF_DE_SERVICE_TECHNIQUE_ADMINISTRATIF_DRB", "Délégation Régionale de Bamenda", "CS"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_DE_BUREAU_DRD", "Délégation Régionale de Douala", "CB"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_DE_BUREAU_DRY", "Délégation Régionale de Yaoundé", "CB"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_DE_BUREAU_DRG", "Délégation Régionale de Garoua", "CB"},
            {"Chef de Bureau", "Head of Bureau", "CHEF_DE_BUREAU_DRB", "Délégation Régionale de Bamenda", "CB"},
            {"Secrétaire DRH", "Secretary, DRH", "SECRETAIRE_DRH_DRD", "Délégation Régionale de Douala", "CB"},
            {"Secrétaire DRH", "Secretary, DRH", "SECRETAIRE_DRH_DRY", "Délégation Régionale de Yaoundé", "CB"},
            {"Secrétaire DRH", "Secretary, DRH", "SECRETAIRE_DRH_DRG", "Délégation Régionale de Garoua", "CB"},
            {"Secrétaire DRH", "Secretary, DRH", "SECRETAIRE_DRH_DRB", "Délégation Régionale de Bamenda", "CB"},
            {"Cadre/DRH", "Staff Officer/DRH", "CADRE_DRH_DRD", "Délégation Régionale de Douala", "CA"},
            {"Cadre/DRH", "Staff Officer/DRH", "CADRE_DRH_DRY", "Délégation Régionale de Yaoundé", "CA"},
            {"Cadre/DRH", "Staff Officer/DRH", "CADRE_DRH_DRG", "Délégation Régionale de Garoua", "CA"},
            {"Cadre/DRH", "Staff Officer/DRH", "CADRE_DRH_DRB", "Délégation Régionale de Bamenda", "CA"},
            {"Personnel d'Appui/DRH", "Support Staff/DRH", "PERSONNEL_D_APPUI_DRH_DRD", "Délégation Régionale de Douala", "PA"},
            {"Personnel d'Appui/DRH", "Support Staff/DRH", "PERSONNEL_D_APPUI_DRH_DRY", "Délégation Régionale de Yaoundé", "PA"},
            {"Personnel d'Appui/DRH", "Support Staff/DRH", "PERSONNEL_D_APPUI_DRH_DRG", "Délégation Régionale de Garoua", "PA"},
            {"Personnel d'Appui/DRH", "Support Staff/DRH", "PERSONNEL_D_APPUI_DRH_DRB", "Délégation Régionale de Bamenda", "PA"},
            {"Agent d'éxécution/DRH", "Execution Staff/DRH", "AGENT_D_EXECUTION_DRH_DRD", "Délégation Régionale de Douala", "AL"},
            {"Agent d'éxécution/DRH", "Execution Staff/DRH", "AGENT_D_EXECUTION_DRH_DRY", "Délégation Régionale de Yaoundé", "AL"},
            {"Agent d'éxécution/DRH", "Execution Staff/DRH", "AGENT_D_EXECUTION_DRH_DRG", "Délégation Régionale de Garoua", "AL"},
            {"Agent d'éxécution/DRH", "Execution Staff/DRH", "AGENT_D_EXECUTION_DRH_DRB", "Délégation Régionale de Bamenda", "AL"},
            {"Chauffeur/DRH", "Driver/DRH", "CHAUFFEUR_DRH_DRD", "Délégation Régionale de Douala", "AL"},
            {"Chauffeur/DRH", "Driver/DRH", "CHAUFFEUR_DRH_DRY", "Délégation Régionale de Yaoundé", "AL"},
            {"Chauffeur/DRH", "Driver/DRH", "CHAUFFEUR_DRH_DRG", "Délégation Régionale de Garoua", "AL"},
            {"Chauffeur/DRH", "Driver/DRH", "CHAUFFEUR_DRH_DRB", "Délégation Régionale de Bamenda", "AL"},
        };
        for (String[] p : organigrammePostes) {
            ensureLinkedFonction(p[0], p[1], p[2], p[3], p[4]);
        }

        // Motifs / justifications réglementaires — catalogue complet des types de mission
        // (cf. cahier des charges §4.1 : contrôle, formation, étude, représentation, maintenance,
        // suivi, autres) enrichi des motifs propres à la régulation télécom. Idempotent.
        String[][] motifsReglementaires = {
            // --- Contrôle & surveillance ---
            {"CTRL_QOS", "Contrôle de la qualité de service (QoS)", "Quality of Service (QoS) control"},
            {"CTRL_COUVERTURE", "Contrôle de la couverture réseau (2G / 3G / 4G / 5G)", "Network coverage control (2G / 3G / 4G / 5G)"},
            {"CTRL_INFRA", "Contrôle des infrastructures et installations techniques", "Control of infrastructure and technical facilities"},
            {"MONITORING", "Monitoring / surveillance du spectre des fréquences", "Frequency spectrum monitoring / surveillance"},
            {"CTRL_TITRES", "Contrôle des titres d'exploitation et des ressources rares", "Control of operating licenses and scarce resources"},
            {"CTRL_ADMIN_TARIF", "Contrôles administratifs et tarifaires", "Administrative and tariff controls"},
            {"CTRL_CONFORMITE", "Contrôle de conformité des équipements et agréments", "Equipment and approvals compliance control"},
            {"CTRL_INOPINE", "Contrôle inopiné / mission de constat sur site", "Unannounced inspection / on-site assessment mission"},
            // --- Inspection / audit / enquête ---
            {"INSPECTION", "Inspection technique des installations régionales", "Technical inspection of regional facilities"},
            {"AUDIT", "Audit technique et réglementaire", "Technical and regulatory audit"},
            {"ENQUETE", "Enquête / investigation (brouillage, plainte, litige)", "Inquiry / investigation (interference, complaint, dispute)"},
            {"EXPERTISE", "Expertise technique et relevés de mesures", "Technical expertise and measurement surveys"},
            // --- Études ---
            {"ETUDE", "Étude et analyse technique", "Technical study and analysis"},
            {"BENCHMARKING", "Benchmarking / étude comparative (parangonnage)", "Benchmarking / comparative study"},
            {"ETUDE_ECO", "Étude et évaluation économique du secteur", "Economic study and evaluation of the sector"},
            {"ETUDE_MARCHE", "Étude de marché / observatoire du secteur des télécommunications", "Market study / telecommunications sector observatory"},
            {"PROSPECTIVE", "Étude prospective et planification stratégique", "Foresight study and strategic planning"},
            {"VEILLE_TECHNO", "Veille technologique et réglementaire", "Technology and regulatory watch"},
            // --- Formation & renforcement des capacités ---
            {"FORMATION", "Formation / stage / perfectionnement", "Training / internship / professional development"},
            {"RENFORCEMENT", "Renforcement des capacités", "Capacity building"},
            {"ATELIER", "Atelier / groupe de travail technique", "Workshop / technical working group"},
            // --- Représentation & coopération ---
            {"REPRESENTATION", "Représentation officielle de l'Agence", "Official representation of the Agency"},
            {"CONF_UIT", "Participation aux travaux de l'UIT et conférences internationales", "Participation in ITU proceedings and international conferences"},
            {"COOPERATION", "Mission de coopération bilatérale ou multilatérale", "Bilateral or multilateral cooperation mission"},
            {"SEMINAIRE", "Séminaire / colloque / conférence", "Seminar / colloquium / conference"},
            {"NEGOCIATION", "Négociation / réunion institutionnelle", "Negotiation / institutional meeting"},
            // --- Exploitation & maintenance ---
            {"MAINTENANCE", "Maintenance des équipements et systèmes techniques", "Maintenance of equipment and technical systems"},
            {"DEPLOIEMENT", "Déploiement / installation d'équipements", "Deployment / installation of equipment"},
            {"SUIVI_PROJET", "Suivi et évaluation de projet", "Project monitoring and evaluation"},
            {"SUPERVISION", "Supervision et coordination d'activités régionales", "Supervision and coordination of regional activities"},
            // --- Autres missions réglementaires ---
            {"SENSIBILISATION", "Sensibilisation et protection du consommateur", "Consumer awareness and protection"},
            {"RECOUVREMENT", "Mission de recouvrement / facturation", "Collection / billing mission"},
            {"CONTENTIEUX", "Mission liée au contentieux et aux affaires juridiques", "Mission related to litigation and legal affairs"},
            {"APPUI", "Appui technique aux délégations régionales", "Technical support to regional delegations"},
            {"MISSION_SERVICE", "Mission de service (motif général)", "Service mission (general purpose)"},
            {"REGULARISATION", "Régularisation (force majeure)", "Regularization (force majeure)"},
            {"DECISION_DG", "Décision du Directeur Général", "Director General's decision"},
            {"AUTRE", "Autre motif dûment habilité", "Other duly authorized reason"},
        };
        for (String[] mtf : motifsReglementaires) {
            ensureMotif(mtf[0], mtf[1], mtf[2]);
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

                // Map role from AD (string) to Role enum safely
                Role mappedRoleTemp = Role.ROLE_AGENT;
                try {
                    String roleStr = row.getOrDefault("role", "ROLE_AGENT");
                    mappedRoleTemp = Role.valueOf(roleStr);
                } catch (Exception ignored) {}
                final Role mappedRole = mappedRoleTemp;

                // Create or update User. Note: searchUsers() always inserts a "matricule" key into
                // `row` (via LdapDirectoryService#extractMatricule), even when AD has none of the
                // matricule-like attributes — in which case its value is "", not absent. So
                // row.getOrDefault("matricule", "LDAP-" + login) never falls back (the key is
                // present) and used to leave User.matricule blank while the Personnel record below
                // correctly fell back to "LDAP-" + login — the two records for the same person then
                // disagreed, which broke matching a notification (created against Personnel.matricule)
                // back to the logged-in User. Compute the fallback-aware matricule once, up front, so
                // both records use the same value.
                String adMatricule = row.getOrDefault("matricule", "");
                String resolvedMatricule = adMatricule.isBlank() ? "LDAP-" + login : adMatricule;
                if (resolvedMatricule.length() > 64) resolvedMatricule = resolvedMatricule.substring(0, 64);
                final String userMatricule = resolvedMatricule;

                User existingAdUser = userRepository.findByUsername(login).orElse(null);
                if (existingAdUser == null) {
                    String display = row.getOrDefault("nom", login);
                    String email = row.getOrDefault("email", "");
                    String structure = row.getOrDefault("nomStructure", "");
                    // AD-managed accounts always sign in via a live LDAP bind (AuthController tries
                    // that first); the local password column is never meant to be used for them, so
                    // it gets an unguessable, unusable random hash rather than a shared default.
                    User u = new User(login, passwordEncoder.encode(UUID.randomUUID().toString()), display, email, structure, mappedRole);
                    u.setMatricule(userMatricule);
                    u.setPrenom(row.getOrDefault("prenom", ""));
                    u.setTitle(row.getOrDefault("codeFonction", ""));
                    userRepository.save(u);
                } else if (existingAdUser.getMatricule() == null || existingAdUser.getMatricule().isBlank()) {
                    // Backfill an already-synced account that was created before this fix, or hit the
                    // getOrDefault bug above — never touches a matricule that's already set.
                    existingAdUser.setMatricule(userMatricule);
                    userRepository.save(existingAdUser);
                }

                // Ensure Personnel exists — matched by the same fallback-aware matricule computed
                // above (userMatricule), never by the raw (possibly blank) AD value. Matching on the
                // raw value used to skip this lookup entirely for any AD account with none of the
                // matricule-like attributes (description/info/comment/employeeID/employeeNumber),
                // silently creating a brand-new duplicate Personnel row on every single AD sync —
                // one real account had accumulated 20 duplicate rows this way before this fix.
                // findAllByMatricule (not findByMatricule) because those very duplicates are still
                // there, unconsolidated — findByMatricule throws once more than one row matches.
                Personnel existing = personnelRepository.findAllByMatricule(userMatricule)
                        .stream().findFirst().orElse(null);
                if (existing == null) {
                    String nom = row.getOrDefault("nom", "");
                    String prenom = row.getOrDefault("prenom", "");
                    String email = row.getOrDefault("email", "");
                    String phone = row.getOrDefault("telephone", "");

                    Personnel p = new Personnel(nom, prenom, userMatricule, email, phone, null);
                    p.setDepartement(row.getOrDefault("nomStructure", ""));
                    p.setFonction(row.getOrDefault("codeFonction", ""));
                    personnelRepository.save(p);
                }
            }
        } catch (Exception e) {
            log.warn("AD sync ignoré au démarrage (LDAP indisponible ?): {}", e.getMessage());
        }

        // 8b. Repair any User left with a blank matricule by the AD-sync bug fixed above — runs
        // unconditionally (not inside the AD-sync try/catch above), so it still fixes already-broken
        // accounts even when LDAP itself is unreachable right now. A blank User.matricule means
        // notifications created against the person's Personnel.matricule (e.g. mission/mandate
        // assignment) can never be matched back to their logged-in session — see
        // NotificationService#forRecipient and my-missions.html's matricule field, which is
        // populated from User.matricule at login. Matched by email first, then by the deterministic
        // "LDAP-<username>" pattern DataLoader gives an AD-synced Personnel with no real matricule.
        // Never touches a User whose matricule is already set.
        List<User> usersMissingMatricule = userRepository.findAll().stream()
                .filter(u -> u.getMatricule() == null || u.getMatricule().isBlank())
                .toList();
        int repairedCount = 0;
        for (User u : usersMissingMatricule) {
            // findAllBy... (not findBy...) throughout: some accounts have several duplicate
            // Personnel rows sharing one matricule or email (the very issue this backfill exists to
            // work around) — findByX would throw NonUniqueResultException on those instead of
            // repairing the account.
            Personnel match = null;
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                match = personnelRepository.findAllByEmailIgnoreCase(u.getEmail()).stream().findFirst().orElse(null);
            }
            if (match == null && u.getUsername() != null && !u.getUsername().isBlank()) {
                match = personnelRepository.findAllByMatricule("LDAP-" + u.getUsername()).stream().findFirst().orElse(null);
            }
            if (match != null && match.getMatricule() != null && !match.getMatricule().isBlank()) {
                u.setMatricule(match.getMatricule());
                userRepository.save(u);
                repairedCount++;
            }
        }
        if (repairedCount > 0) {
            log.warn("SMOMA: {} compte(s) utilisateur avaient un matricule manquant — corrigé(s) au démarrage.", repairedCount);
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