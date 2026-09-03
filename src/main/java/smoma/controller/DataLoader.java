package smoma.controller;

import org.springframework.boot.CommandLineRunner;
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

@Component
public class DataLoader implements CommandLineRunner {

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

        // 2. Users
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin@art.cm", "admin123", "Administrateur Système", "admin@art.cm", "IT", Role.ROLE_ADMIN));
            userRepository.save(new User("gm@art.cm", "password123", "Directeur Général (DG)", "gm@art.cm", "Direction Générale", Role.ROLE_GENERAL_MANAGER));
            userRepository.save(new User("hr@art.cm", "password123", "Responsable RH", "hr@art.cm", "Ressources Humaines", Role.ROLE_HR_OFFICER));
            userRepository.save(new User("dept@art.cm", "password123", "Chef de Département", "dept@art.cm", "Direction Technique", Role.ROLE_DEPARTMENT_REPRESENTATIVE));
            userRepository.save(new User("staff@art.cm", "password123", "Agent de Mission", "staff@art.cm", "Contrôle & Régulation", Role.ROLE_STAFF_MEMBER));
        }

        // 3. Departments / Structures
        if (departmentRepository.count() == 0) {
            departmentRepository.save(new Department("Direction des Ressources Humaines", "DRH", "M. MBARGA Lucien"));
            departmentRepository.save(new Department("Direction Technique", "DT", "Mme NNANG Alice"));
            departmentRepository.save(new Department("Direction de la Gestion Financière", "DGF", "M. KOUAM Emmanuel"));
            departmentRepository.save(new Department("Direction de la Législation et Coopération", "DLCI", "Mme MOUKOURI Nadia"));
            departmentRepository.save(new Department("Direction des Prestations et Suivi", "DPS", "M. FOTSING Joseph"));
        }

        // 4. Referentiels: Functions, Grades, Rangs, Motifs
        if (fonctionRepository.count() == 0) {
            fonctionRepository.save(new Fonction("Directeur de Structure", "DIRECTEUR", ""));
            fonctionRepository.save(new Fonction("Chef de Service", "CHEF_SERVICE", ""));
            fonctionRepository.save(new Fonction("Ingénieur de Contrôle", "INGENIEUR", ""));
            fonctionRepository.save(new Fonction("Juriste Régulateur", "JURISTE", ""));
            fonctionRepository.save(new Fonction("Agent Technique", "AGENT_TECH", ""));
        }

        // Grade entity removed; the personnel grade is an enum inside Personnel.

        if (rangRepository.count() == 0) {
            rangRepository.save(new Rang("RANG_1", "Directeur / Chef de Département", 1));
            rangRepository.save(new Rang("RANG_2", "Chef de Service / Ingénieur Principal", 2));
            rangRepository.save(new Rang("RANG_3", "Cadre / Agent de Contrôle", 3));
        }

        if (motifRepository.count() == 0) {
            motifRepository.save(new MotifReglementaire("CTRL_4G5G", "Contrôle de la Qualité de Service & Couverture Réseau 4G/5G", ""));
            motifRepository.save(new MotifReglementaire("CONF_UIT", "Représentation à la Conférence de l'Union Internationale des Télécommunications", ""));
            motifRepository.save(new MotifReglementaire("AUDIT_REG", "Inspection et Audit Technique des Installations Régionales", ""));
            motifRepository.save(new MotifReglementaire("SEMINAIRE_JUR", "Séminaire d'harmonisation de la législation télécom", ""));
        }

        // 5. Rate Scales (BaremeIndemnite)
        if (baremeRepository.count() == 0) {
            BaremeIndemnite b1 = new BaremeIndemnite();
            b1.setGrade("GRADE_A");
            b1.setRang("RANG_1");
            b1.setFonction("DIRECTEUR");
            b1.setTypeMission(OrdreDeMission.TypeMission.INTERNE);
            b1.setMontantJournalier(new BigDecimal("100000"));
            baremeRepository.save(b1);

            BaremeIndemnite b2 = new BaremeIndemnite();
            b2.setGrade("GRADE_A");
            b2.setRang("RANG_1");
            b2.setFonction("DIRECTEUR");
            b2.setTypeMission(OrdreDeMission.TypeMission.EXTERNE);
            b2.setMontantJournalier(new BigDecimal("300000"));
            baremeRepository.save(b2);

            BaremeIndemnite b3 = new BaremeIndemnite();
            b3.setGrade("GRADE_A");
            b3.setRang("RANG_2");
            b3.setFonction("CHEF_SERVICE");
            b3.setTypeMission(OrdreDeMission.TypeMission.INTERNE);
            b3.setMontantJournalier(new BigDecimal("60000"));
            baremeRepository.save(b3);

            BaremeIndemnite b4 = new BaremeIndemnite();
            b4.setGrade("GRADE_A");
            b4.setRang("RANG_2");
            b4.setFonction("CHEF_SERVICE");
            b4.setTypeMission(OrdreDeMission.TypeMission.EXTERNE);
            b4.setMontantJournalier(new BigDecimal("200000"));
            baremeRepository.save(b4);

            BaremeIndemnite b5 = new BaremeIndemnite();
            b5.setGrade("GRADE_B");
            b5.setRang("RANG_3");
            b5.setFonction("INGENIEUR");
            b5.setTypeMission(OrdreDeMission.TypeMission.INTERNE);
            b5.setMontantJournalier(new BigDecimal("40000"));
            baremeRepository.save(b5);

            BaremeIndemnite b6 = new BaremeIndemnite();
            b6.setGrade("GRADE_B");
            b6.setRang("RANG_3");
            b6.setFonction("INGENIEUR");
            b6.setTypeMission(OrdreDeMission.TypeMission.EXTERNE);
            b6.setMontantJournalier(new BigDecimal("150000"));
            baremeRepository.save(b6);
        }

        // 6. Personnel (~6 sample staff members)
        if (personnelRepository.count() == 0) {
            Personnel p1 = new Personnel("MBARGA", "Lucien", "ART-2026-001", "mbarga@art.cm", "", null);
            Personnel p2 = new Personnel("NNANG", "Alice", "ART-2026-002", "annang@art.cm", "", null);
            Personnel p3 = new Personnel("TCHOUA", "Pierre", "ART-2026-003", "ptchoua@art.cm", "", null);
            Personnel p4 = new Personnel("MOUKOURI", "Nadia", "ART-2026-004", "nmoukouri@art.cm", "", null);
            Personnel p5 = new Personnel("KOUAM", "Emmanuel", "ART-2026-005", "ekouam@art.cm", "", null);
            Personnel p6 = new Personnel("FOTSING", "Joseph", "ART-2026-006", "jfotsing@art.cm", "", null);

            personnelRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
        }

        // 7. Mandats, Steps, OMs, Advances, Reports
        if (mandatRepository.count() == 0) {
            Personnel pMbarga = personnelRepository.findByMatricule("ART-2026-001").orElse(null);
            Personnel pNnang = personnelRepository.findByMatricule("ART-2026-002").orElse(null);
            Personnel pTchoua = personnelRepository.findByMatricule("ART-2026-003").orElse(null);
            Personnel pMoukouri = personnelRepository.findByMatricule("ART-2026-004").orElse(null);
            Personnel pKouam = personnelRepository.findByMatricule("ART-2026-005").orElse(null);

            // Mandat 1: Interne Avec Frais (Douala & Kribi)
            MandatDeMission m1 = new MandatDeMission();
            m1.setReferenceMandat("MANDAT-ART-2026-001");
            m1.setObjetGeneral("Mission de contrôle de la qualité de service et couverture réseau 4G/5G à Douala et Kribi");
            m1.setMotifReglementaire("Contrôle de la Qualité de Service & Couverture Réseau 4G/5G");
            m1.setDateDebut(LocalDate.now().plusDays(5));
            m1.setDateFin(LocalDate.now().plusDays(15));
            m1.setTypeMission(MandatDeMission.TypeMission.INTERNE);
            m1.setSansFrais(false);
            m1.setPersonnelList(new ArrayList<>(Arrays.asList(pNnang, pTchoua)));
            m1.setStatut(MandatDeMission.StatutMandat.ACTIF);
            mandatRepository.save(m1);
            // Steps for Mandat 1
            EtapeMission e1 = new EtapeMission();
            e1.setMandatDeMission(m1);
            e1.setLieu("Douala");
            e1.setDateDebut(LocalDate.now().plusDays(5));
            e1.setDateFin(LocalDate.now().plusDays(10));
            e1.setCommentaire("Contrôle des antennes d'opérateurs Mobile à Douala");

            EtapeMission e2 = new EtapeMission();
            e2.setMandatDeMission(m1);
            e2.setLieu("Kribi");
            e2.setDateDebut(LocalDate.now().plusDays(11));
            e2.setDateFin(LocalDate.now().plusDays(15));
            e2.setCommentaire("Audit de la couverture littorale à Kribi");

            etapeRepository.saveAll(Arrays.asList(e1, e2));

            // Mandat 2: Externe Avec Frais (Genève)
            MandatDeMission m2 = new MandatDeMission();
            m2.setReferenceMandat("MANDAT-ART-2026-002");
            m2.setObjetGeneral("Conférence de l'Union Internationale des Télécommunications (UIT) à Genève");
            m2.setMotifReglementaire("Représentation à la Conférence de l'Union Internationale des Télécommunications");
            m2.setDateDebut(LocalDate.now().plusDays(20));
            m2.setDateFin(LocalDate.now().plusDays(28));
            m2.setTypeMission(MandatDeMission.TypeMission.EXTERNE);
            m2.setSansFrais(false);
            m2.setPersonnelList(new ArrayList<>(Arrays.asList(pMbarga, pMoukouri)));
            m2.setStatut(MandatDeMission.StatutMandat.ACTIF);
            mandatRepository.save(m2);

            // Steps for Mandat 2
            EtapeMission e3 = new EtapeMission();
            e3.setMandatDeMission(m2);
            e3.setLieu("Genève (Suisse)");
            e3.setDateDebut(LocalDate.now().plusDays(20));
            e3.setDateFin(LocalDate.now().plusDays(28));
            e3.setCommentaire("Participation aux travaux de régulation des fréquences internationales");
            etapeRepository.save(e3);

            // Mandat 3: Interne SANS FRAIS (Yaoundé) - Sceau Rouge
            MandatDeMission m3 = new MandatDeMission();
            m3.setReferenceMandat("MANDAT-ART-2026-003");
            m3.setObjetGeneral("Inspection de routine et vérification des équipements au siège régional à Yaoundé");
            m3.setMotifReglementaire("Inspection et Audit Technique des Installations Régionales");
            m3.setDateDebut(LocalDate.now().minusDays(3));
            m3.setDateFin(LocalDate.now().minusDays(1));
            m3.setTypeMission(MandatDeMission.TypeMission.INTERNE);
            m3.setSansFrais(true); // SANS FRAIS
            m3.setPersonnelList(new ArrayList<>(Arrays.asList(pKouam)));
            m3.setStatut(MandatDeMission.StatutMandat.ACTIF);
            mandatRepository.save(m3);

            // Steps for Mandat 3
            EtapeMission e4 = new EtapeMission();
            e4.setMandatDeMission(m3);
            e4.setLieu("Yaoundé");
            e4.setDateDebut(LocalDate.now().minusDays(3));
            e4.setDateFin(LocalDate.now().minusDays(1));
            e4.setCommentaire("Vérification des registres internes");
            etapeRepository.save(e4);

            // OMs
            OrdreDeMission om1 = new OrdreDeMission();
            om1.setReferenceOrdre("OM-ART-2026-001");
            om1.setMandatDeMission(m1);
            om1.setPersonnel(pNnang);
            om1.setObjectifsSpecifiques("Contrôle Qualité Réseau Douala");
            om1.setTypeMission(OrdreDeMission.TypeMission.INTERNE);
            om1.setLieuDepart("Yaoundé");
            om1.setLieuDestination("Douala");
            om1.setDateDebut(LocalDate.now().plusDays(5));
            om1.setDateFin(LocalDate.now().plusDays(10));
            om1.setMoyenTransport("Véhicule de Service ART");
            om1.setMontantIndemnite(new BigDecimal("360000"));
            om1.setMontantAvance(new BigDecimal("270000")); // 75%
            om1.setMontantSolde(new BigDecimal("90000"));
            om1.setSansFrais(false);
            om1.setStatut(OrdreDeMission.StatutOrdre.SIGNE);
            ordreRepository.save(om1);

            OrdreDeMission om2 = new OrdreDeMission();
            om2.setReferenceOrdre("OM-ART-2026-002");
            om2.setMandatDeMission(m2);
            om2.setPersonnel(pMbarga);
            om2.setObjectifsSpecifiques("Conférence UIT Genève");
            om2.setTypeMission(OrdreDeMission.TypeMission.EXTERNE);
            om2.setLieuDepart("Yaoundé");
            om2.setLieuDestination("Genève (Suisse)");
            om2.setDateDebut(LocalDate.now().plusDays(20));
            om2.setDateFin(LocalDate.now().plusDays(28));
            om2.setMoyenTransport("Avion Commercial");
            om2.setMontantIndemnite(new BigDecimal("2400000"));
            om2.setMontantAvance(new BigDecimal("2160000")); // 90%
            om2.setMontantSolde(new BigDecimal("240000"));
            om2.setSansFrais(false);
            om2.setStatut(OrdreDeMission.StatutOrdre.SIGNE);
            ordreRepository.save(om2);

            OrdreDeMission om3 = new OrdreDeMission();
            om3.setReferenceOrdre("OM-ART-2026-003");
            om3.setMandatDeMission(m3);
            om3.setPersonnel(pKouam);
            om3.setObjectifsSpecifiques("Inspection Siège Yaoundé");
            om3.setTypeMission(OrdreDeMission.TypeMission.INTERNE);
            om3.setLieuDepart("Yaoundé");
            om3.setLieuDestination("Yaoundé");
            om3.setDateDebut(LocalDate.now().minusDays(3));
            om3.setDateFin(LocalDate.now().minusDays(1));
            om3.setMoyenTransport("À Pied");
            om3.setMontantIndemnite(BigDecimal.ZERO);
            om3.setMontantAvance(BigDecimal.ZERO);
            om3.setMontantSolde(BigDecimal.ZERO);
            om3.setSansFrais(true); // SANS FRAIS
            om3.setStatut(OrdreDeMission.StatutOrdre.SIGNE);
            ordreRepository.save(om3);

            // Avances
            AvanceSurFrais av1 = new AvanceSurFrais();
            av1.setOrdreDeMission(om1);
            av1.setPersonnel(pNnang);
            av1.setMontantTotal(new BigDecimal("360000"));
            av1.setPourcentageAvance(75);
            av1.setMontant(new BigDecimal("270000"));
            av1.setMontantSolde(new BigDecimal("90000"));
            av1.setDateDemande(LocalDate.now());
            av1.setStatut(AvanceSurFrais.StatutAvance.VALIDEE);
            av1.setValidee(true);
            avanceRepository.save(av1);

            AvanceSurFrais av2 = new AvanceSurFrais();
            av2.setOrdreDeMission(om2);
            av2.setPersonnel(pMbarga);
            av2.setMontantTotal(new BigDecimal("2400000"));
            av2.setPourcentageAvance(90);
            av2.setMontant(new BigDecimal("2160000"));
            av2.setMontantSolde(new BigDecimal("240000"));
            av2.setDateDemande(LocalDate.now());
            av2.setStatut(AvanceSurFrais.StatutAvance.DEMANDEE);
            av2.setValidee(false);
            avanceRepository.save(av2);

            // Rapport de mission pour OM3
            RapportMission r3 = new RapportMission();
            r3.setOrdreDeMission(om3);
            r3.setPersonnel(pKouam);
            r3.setTitre("Rapport d'inspection des registres internes du siège");
            r3.setDescription("Vérification achevée avec succès. Aucun manquement constaté.");
            r3.setCategorie("CONTRÔLE");
            r3.setFichierPath("/uploads/rapports/rapport_om3.pdf");
            r3.setDateDepot(LocalDate.now().minusDays(1));
            r3.setStatutValidation("VALIDE");
            rapportRepository.save(r3);
        }

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
                    User u = new User(login, "ldap-managed", display, email, structure, mappedRole);
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
            System.out.println("AD sync skipped: " + e.getMessage());
        }
    }
}