/**
 * SMOMA Internationalization (i18n) Engine
 * Bilingual support: French (Français - FR, default) and English (EN)
 * Agence de Régulation des Télécommunications (ART Cameroun)
 */

const SMOMA_TRANSLATIONS = {
    fr: {
        // App Brand & Navigation
        appBrand: "SMOMA - ART Cameroun",
        appTitle: "AGENCE DE REGULATION DES TELECOMMUNICATIONS",
        appSubtitle: "Système de Gestion des Ordres de Mission (SMOMA)",
        navDashboard: "Tableau de bord",
        navMandats: "Mandats DG",
        navOrdres: "Ordres de Mission",
        navPersonnel: "Personnel",
        navDepartments: "Directions & Depts",
        navValidation: "Guichet de Validation",
        navReports: "Impression & Rapports",
        navPayments: "Paiements de Mission",
        navAnnual: "Bilan Annuel",
        navAdmin: "Admin & AD",
        logout: "Déconnexion",
        connectedSession: "Session Connectée",
        welcomeUser: "Bienvenue",

        // Login Module
        loginPageTitle: "ART - Connexion au Système",
        republicOfCameroon: "République du Cameroun",
        peaceWorkFatherland: "Paix - Travail - Patrie",
        telecomRegBoard: "Agence de Régulation des Télécommunications",
        portalSubTitle: "Portail d'authentification institutionnelle",
        identityLabel: "Identifiant (Email, Nom d'utilisateur ou Matricule)",
        identityPlaceholder: "nom.prenom@art.cm ou matricule (ex: ART-002)",
        passwordLabel: "Mot de passe sécurisé",
        loginSubmitBtn: "Se connecter au Portail",
        loginErrorMsg: "Identifiants professionnels incorrects ou accès refusé.",
        networkErrorMsg: "Erreur réseau : Impossible de joindre le serveur Spring Boot.",
        loginCredits: "2026 Agence de Régulation des Télécommunications (ART) - République du Cameroun",

        // Main Index Page
        mainHeader: "Système de Gestion des Ordres de Mission (SMOMA)",
        mainSubheader: "Agence de Régulation des Télécommunications (ART) - Siège de Yaoundé. Plateforme centralisée pour la gestion des mandats signés du DG, des ordres de mission, de la structuration des départements, du personnel et du suivi budgétaire.",
        sectionOrg: "Structure Organisationnelle & Personnel",
        sectionMissions: "Gestion Opérationnelle des Missions",
        sectionStats: "Vue d'Ensemble du Système",
        accessBtn: "Accéder au Module",

        modDepartmentsTitle: "Directions & Depts",
        modDepartmentsDesc: "Gérer l'organigramme de l'ART (DRH, DGF, DT, DLCI, DPS, etc.).",
        modPersonnelTitle: "Gestion du Personnel",
        modPersonnelDesc: "Créer et mettre à jour le profil des agents, fonctions et matricules.",
        modAdminTitle: "Admin & Synchro AD",
        modAdminDesc: "Synchronisation Active Directory et comptes d'accès local.",
        modAdminReserved: "Administrateur uniquement",
        modAnnualDashboardTitle: "Bilan Annuel Agent",
        modAnnualDashboardDesc: "Historique annuel des missions effectuées par agent pour l'exercice.",
        modMandatsTitle: "Mandats (DG)",
        modMandatsDesc: "Mandats de mission généraux signés par le Directeur Général avec réf. unique.",
        modOrdresTitle: "Ordres de Mission",
        modOrdresDesc: "Types précis, objectifs, étapes itinéraire, transport et frais (avec/sans).",
        modValidationTitle: "Guichet de Validation",
        modValidationDesc: "Validation hiérarchique par les Chefs de Département et la Direction Générale.",
        modReportsTitle: "Impression & Rapports",
        modReportsDesc: "Génération PDF officiels et numérisation des rapports de fin de mission.",
        modPaymentsTitle: "Paiements de Mission",
        modPaymentsDesc: "Suivi des frais, validation des paiements et contrôle de conformité pour les missions.",

        statMandats: "Mandats DG Signés",
        statOrders: "Ordres de Mission",
        statPending: "En Attente de Validation",
        statStaff: "Personnel Inscrit (AD)",

        // Common Table & Form terms
        thRef: "Référence",
        thMandatRef: "Mandat Réf.",
        thAgent: "Agent Concerné",
        thObject: "Objet Général",
        thMotif: "Motif Réglementaire",
        thTypeTransport: "Type & Transport",
        thPeriod: "Période Globale",
        thExecPeriod: "Période d'Exécution",
        thFinancialRegime: "Régime Financier",
        thStatus: "Statut & Modifiabilité",
        thActions: "Actions",
        thMatricule: "Matricule",
        thName: "Nom & Prénom",
        thDept: "Département",
        thFunction: "Fonction",
        thGrade: "Grade",
        thEmail: "Email",
        thPhone: "Téléphone",
        thDeparture: "Départ",
        thDestination: "Destination",
        thTransport: "Transport",
        thStep: "Étape",

        // Mission Order & Mandat actions & buttons
        btnCreateMandat: "Créer un Nouveau Mandat",
        btnCreateDirectOm: "Créer un OM Direct",
        btnUploadScan: "Importer Scan Signé",
        btnViewSideBySide: "Voir côte à côte",
        btnDownloadPdf: "PDF",
        btnViewForm: "Consulter Formulaire OM",
        btnPrintForm: "Imprimer le Formulaire",
        btnCancel: "Annuler",
        btnSave: "Enregistrer",
        btnClose: "Fermer",
        btnSearch: "Rechercher",
        btnFilter: "Filtrer",
        btnReset: "Réinitialiser",

        // Mission attributes & labels
        lblMandatRef: "Référence du Mandat",
        lblMotifReglementaire: "Motif Réglementaire",
        lblRefJustification: "Réf. / Article de Justification",
        refJustifHint: "Référence de l'acte qui autorise la mission (Agence / Direction Générale / structure initiatrice / n° d'ordre). Générée automatiquement, modifiable.",
        stepCityPlaceholder: "Ville de l'étape (grandes villes / antennes ART)",
        stepCityPrompt: "Saisir la ville / le lieu de l'étape (hors liste) :",
        stepAgentsLabel: "Agents affectés à cette étape",
        stepAgentsEmptyHint: "Sélectionnez d'abord l'équipe du mandat ci-dessus.",
        lblObjetGeneral: "Objet Général de la Mission",
        lblObjectifsSpecifiques: "Objectifs Spécifiques",
        lblTypeMission: "Type de Mission",
        lblFinancialRegime: "Régime Financier",
        lblTransportMode: "Moyen(s) de Transport",
        lblStartDate: "Date de Début",
        lblEndDate: "Date de Fin",
        lblSansFrais: "Mandat SANS FRAIS (Sceau Rouge)",
        lblForceMajeure: "Régularisation Force Majeure (< 48h)",
        lblTeamSelection: "Équipe d'Agents Concernés",
        lblSearchMatriculePlaceholder: "Rechercher par matricule ou nom...",
        lblSelectAgent: "Sélectionner un agent",
        lblSelectMandat: "Sélectionner un mandat de rattachement",
        lblItinerarySteps: "Étapes & Itinéraire de la Mission",
        lblAddStep: "Ajouter une Étape",
        lblTravelDate: "Date du Trajet",
        lblStepTransport: "Transport de l'étape",

        // Mission Types
        typeInterne: "Interne (Cameroun)",
        typeExterne: "Externe (International)",
        typeFormation: "Formation",
        typeEtude: "Étude / Benchmarking",
        typeRepresentation: "Représentation",

        // Transport Modes
        transportServiceVehicle: "Véhicule de service",
        transportPlane: "Avion",
        transportMixed: "Mixte (Véhicule et Avion)",
        transportTrain: "Train (Camrail)",
        transportPersonalVehicle: "Véhicule personnel",

        // Financial Regimes & Badges
        regimeAvecFrais: "AVEC FRAIS DE MISSION",
        regimeSansFrais: "SANS FRAIS (Sceau Rouge)",
        badgeSigned: "Signé & Figé",
        badgeDraft: "Brouillon Modifiable",
        badgePending: "En Attente de Scan",
        badgeValidated: "Validé",

        // Detailed Mission Order Official Form
        officialDocHeadingLeft: "RÉPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE RÉGULATION\nDES TÉLÉCOMMUNICATIONS\nDirection Générale",
        officialDocHeadingRight: "REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS\nREGULATORY BOARD\nDirectorate General",
        omFormTitle: "ORDRE DE MISSION INDIVIDUEL",
        omFormRef: "Réf. Officielle N°",
        omSectionStaff: "I. IDENTIFICATION DE L'AGENT",
        omSectionMission: "II. DÉTAILS DE LA MISSION",
        omSectionItinerary: "III. ITINÉRAIRE ET ÉTAPES DE LA MISSION",
        omSectionFinancial: "IV. RÉGIME FINANCIER & PRISE EN CHARGE",
        omSectionSignatures: "V. VISAS & SIGNATURES OFFICIELLES",
        omSignatureStaff: "Visa du Titulaire de la Mission",
        omSignatureDept: "Le Chef de Département / DRH",
        omSignatureGm: "Le Directeur Général (Signataire Habilité)",

        // Personnel Module
        personnelTitle: "Répertoire du Personnel",
        personnelSubtitle: "Gestion des fiches agents, affectations organisationnelles, grades et consultation du compte individuel",
        btnAddAgent: "Ajouter un Agent",
        searchPersonnelPlaceholder: "Rechercher par nom, prénom ou matricule...",
        allDepartments: "Toutes les directions / structures",
        allGrades: "Tous les grades",
        btnCompteIndividuel: "Compte",

        // Departments Module
        deptsTitle: "Organigramme & Structures ART",
        deptsSubtitle: "Référentiel officiel des Directions, Divisions, Services et Agences Régionales",
        deptsBrandSub: "Organigramme & Structures",
        deptsPageTitle: "Organisation de l'Agence de Régulation des Télécommunications",
        deptsPageSub: "Structure organisationnelle conformément au décret n° 2020/727 du 03 décembre 2020",
        deptsSearchPlaceholder: "Rechercher un département, une direction, une unité...",
        deptsStatCentral: "Directions Centrales",
        deptsStatAttached: "Services Rattachés",
        deptsStatSub: "Sous-Directions",
        deptsStatRegional: "Services Déconcentrés",
        deptsNoResults: "Aucun résultat trouvé",
        deptsNoResultsHint: "Essayez avec un autre terme de recherche.",
        btnPrintShort: "Imprimer",
        navBackDashboard: "Retour au Tableau de Bord",

        // Validation Portal
        validationTitle: "Guichet de Validation & Signatures (DG)",
        validationSubtitle: "Validation hiérarchique et signature officielle des mandats et ordres de mission",
        btnApprove: "Approuver & Signer",
        btnReject: "Rejeter",

        // HR & Reports Module
        reportsTitle: "Impression & Dépôt des Rapports de Mission",
        reportsSubtitle: "Dépôt des rapports de mission et justificatifs pour liquidation des soldes (25% ou 10%)",
        btnDepositReport: "Déposer un Rapport",

        // Payments Module
        paymentsTitle: "Guichet des Avances & Indemnités de Mission",
        paymentsSubtitle: "Gestion des avances sur frais (75% Interne / 90% Externe) et liquidation des soldes",
        btnRequestAdvance: "Solliciter une Avance",
        thAdvance75: "Avance (75% / 90%)",
        thRemainingSolde: "Solde Restant",

        // Annual Dashboard
        annualTitle: "Bilan Annuel d'Activité des Missions",
        annualSubtitle: "Consolidation annuelle des missions, effectifs déployés et dépenses budgétaires",
        btnPrintAnnual: "Imprimer le Bilan",
        cardSignedMandats: "Mandats DG Signés",
        cardIndividualOrders: "Ordres de Mission Individuels",
        cardMissionReports: "Rapports de Mission Déposés",
        cardNoExpenseMissions: "Missions SANS FRAIS (Sceau Rouge)",
        annualRecapTitle: "Récapitulatif par Direction & Service",
        annualRecapDesc: "Vue consolidée annuelle disponible pour l'exercice budgétaire 2026.",

        // Annual Dashboard — gender & mission analysis
        analysisTitle: "Analyse par Genre & Indicateurs des Missions",
        analysisStaffMale: "Effectif Masculin",
        analysisStaffFemale: "Effectif Féminin",
        analysisAvgDuration: "Durée Moyenne des Missions",
        analysisDaysUnit: "jours",
        analysisTotalIndemnites: "Total Indemnités Calculées",
        currencyXaf: "FCFA",
        analysisMissionsByGender: "Missions Effectuées par Genre",
        analysisGender: "Genre",
        analysisMissionCount: "Nombre de Missions",
        analysisAgentCount: "Agents Distincts",
        analysisShare: "Part (%)",
        analysisMissionsByType: "Missions par Type",
        analysisType: "Type",
        analysisByDepartment: "Missions par Direction / Structure (Top 10)",

        // Admin Panel
        adminTitle: "Administration & Synchronisation Active Directory",
        tabUsers: "Utilisateurs Synchronisés",
        tabGroups: "Groupes & Rôles AD",
        tabDirectory: "Annuaire Complet AD",
        btnSyncNow: "Lancer Synchro AD",
        adStatusConnected: "Connecté à l'AD",
        adStatusOffline: "Mode Hors-ligne",

        // Shared / generic
        commonLoading: "Chargement...",
        commonLoadError: "Erreur de chargement.",
        commonNoData: "Aucune donnée disponible.",
        commonTo: "au",
        commonYes: "Oui",
        commonNo: "Non",
        commonActive: "ACTIF",
        badgeWithFeesShort: "AVEC FRAIS",
        badgeNoFeesShort: "SANS FRAIS",
        badgeSignedLocked: "SIGNÉ (FIGÉ)",
        badgeDraftShort: "Brouillon",
        badgeAwaitingScan: "En attente scan",
        badgeScanValidated: "Scan Validé",

        // Common table headers
        thReportTitle: "Titre du Rapport",
        thWriter: "Agent Rédacteur",
        thCategory: "Catégorie",
        thDepositDate: "Date de Dépôt",
        thValidationStatus: "Statut Validation",
        thOmRef: "Réf. OM",
        thMissionTypeShort: "Type Mission",
        thTotalIndemnity: "Total Indemnité",
        thRegime: "Régime",

        // Validation portal
        hdrValidationSub: "Guichet de Validation Hiérarchique & Direction Générale (DG)",
        valDeskHeading: "Guichet de Validation & Signatures",
        valDeskSub: "Validation des mandats de mission en attente de signature par le Directeur Général",
        valPendingCard: "Mandats en Attente de Signature",
        valNoPending: "Aucun mandat en attente de validation.",
        valProcessBtn: "Traiter dans le Module Mandats",

        // Reports / HR processing
        hdrReportsSub: "Dépôt des Rapports de Mission & Pièces Justificatives",
        repHeading: "Dépôt & Consultation des Rapports de Mission",
        repSub: "Le dépôt du rapport de mission et des justificatifs conditionne le paiement du solde des frais (25% ou 10%)",
        repNoReports: "Aucun rapport de mission déposé.",
        repModalTitle: "Déposer un Rapport de Mission",
        repOmConcerned: "Ordre de Mission Concerné",
        repCategoryLabel: "Catégorie du Rapport",
        repFileLabel: "Fichier PDF du Rapport",
        repSummaryLabel: "Résumé / Synthèse des Déplacements",
        repSubmitBtn: "Soumettre le Rapport",
        repValidateBtn: "Valider",
        repValidated: "Validé (Solde Dégagé)",
        repPendingCheck: "En attente contrôle",
        catControl: "Contrôle / Régulation",
        catTraining: "Formation / Stage",
        catStudy: "Étude et Analyse",
        catRepresentation: "Représentation Officielle",
        catOther: "Autre",

        // Payments / advances
        hdrPaymentsSub: "Gestion des Avances sur Frais (75% Interne / 90% Externe) & Liquidation des Soldes",
        payHeading: "Guichet des Avances & Indemnités",
        paySub: "Calcul des barèmes d'indemnités et gestion des demandes d'avances sur frais de mission",
        payModalTitle: "Demander une Avance sur Frais",
        paySelectOm: "Sélectionner l'Ordre de Mission",
        payRequestDate: "Date de la Demande d'Avance",
        payRequestDateHint: "Règle de gestion : La demande doit impérativement être faite AVANT le départ en mission.",
        payRateHint: "Taux d'avance automatique : 75% pour mission Interne, 90% pour mission Externe.",
        paySubmitBtn: "Soumettre la Demande",
        payBtnAdvance: "Avance",
        payBtnPayBalance: "Verser le solde",
        payBalancePaid: "Solde versé",
        payLiquidated: "Liquidé",
        payFinanceRouteNote: "Toute demande d'avance ou de solde est transmise à la Direction des Finances pour approbation. Le bénéficiaire est ensuite averti : retrait en espèces à la Direction des Finances ou virement bancaire.",
        payApprovalTitle: "Approbation — Direction des Finances",
        payPaymentChannel: "Mode de règlement",
        payChannelCash: "Espèces — retrait à la Direction des Finances",
        payChannelTransfer: "Virement bancaire",
        payTransferRef: "Référence du virement",
        payTransferRefRequired: "Veuillez saisir la référence du virement bancaire.",
        payApprovalNote: "Le bénéficiaire recevra une notification dans « Mes Missions » indiquant le mode de règlement.",
        payApproveConfirm: "Approuver & Notifier",
        payApproveAdvance: "Approuver l'avance",
        payAwaitingFinance: "En attente Direction des Finances",
        payAdvancePaid: "Avance versée",
        payApprovedNotified: "Demande approuvée. Le bénéficiaire a été notifié du mode de règlement.",
        payRequestSentFinance: "Demande enregistrée et transmise à la Direction des Finances pour approbation.",

        // Personnel
        hdrPersonnelSub: "Répertoire du Personnel & Comptes Individuels",
        persHeading: "Répertoire du Personnel",
        persSub: "Gestion des fiches agents, affectations organisationnelles, grades et consultation du compte individuel",
        persNoMatch: "Aucun agent ne correspond aux critères de recherche.",
        persAddTitle: "Ajouter un Nouvel Agent",
        persColLastFirst: "Nom & Prénom",
        persColStructure: "Structure / Direction",
        persColFunction: "Fonction",
        persColGrade: "Grade",
        persColContact: "Contact",
        persColStatus: "Statut",
        persAccountBtn: "Compte",
        persNomLabel: "Nom",
        persPrenomLabel: "Prénom",
        persRangLabel: "Rang / Habilitation",
        persMatriculeLabel: "Matricule Unique",
        btnSaveAgent: "Enregistrer l'Agent",
        btnEdit: "Modifier",
        persEditTitle: "Modifier la Fiche de l'Agent",
        persGenreLabel: "Genre",
        persDobLabel: "Date de Naissance",
        persContractLabel: "Type de Contrat",
        persHireDateLabel: "Date d'Embauche",
        persHirePlaceLabel: "Lieu d'Embauche",
        persRetirementEligible: "Éligible à la retraite",
        persRetirementHint: "Cet agent a atteint ou dépassé l'âge de départ à la retraite (60 ans).",
        persCompteTitle: "Compte Individuel du Personnel",
        persMissionsDone: "Missions Effectuées",
        persDaysOnMission: "Jours en Mission",
        persTotalIndemnities: "Total Indemnités",
        persAdvancesReceived: "Avances Perçues",
        persBalancesRemaining: "Soldes Restants",
        persOmHistory: "Historique des Ordres de Mission",

        // Departments
        hdrDeptsSub: "Organigramme, Directions, Divisions et Services de l'ART",

        // Annual dashboard
        hdrAnnualSub: "Bilan Annuel d'Activité des Missions par Agent et par Structure",

        // Admin panel
        hdrAdminSub: "Comptes, Rôles, Droits et Synchronisation Active Directory",
        adCreateUser: "Créer un Nouvel Utilisateur",
        adCreateDept: "Créer une Nouvelle Direction",
        adSyncedDbUsers: "Utilisateurs de la Base Synchronisés",
        adUsername: "Nom d'utilisateur",
        adPassword: "Mot de passe",
        adEmailLabel: "Email",
        adRole: "Rôle",
        adFirstName: "Prénom",
        adLastName: "Nom",
        adDeptStructure: "Direction / Structure",
        adCreateUserBtn: "Créer l'Utilisateur",
        adDeptName: "Nom de la Direction",
        adAcronym: "Sigle",
        adHeadOfDept: "Responsable de la Direction",
        adCreateDeptBtn: "Créer la Direction",
        adDeptDupHint: "Les directions existantes s'affichent pendant la saisie — vérifiez avant de créer, pour éviter un doublon.",
        adDeptDupBlock: "Cette direction existe déjà — sélectionnez-la dans la liste plutôt que de créer un doublon.",
        adGenre: "Genre",
        adGenreUnspecified: "Non précisé",
        adGenreMale: "Masculin",
        adGenreFemale: "Féminin",
        adRang: "Rang",
        adFonction: "Fonction",
        adOptionNone: "—",
        adSave: "Enregistrer",
        adTariffTitle: "Grille Tarifaire des Indemnités de Mission (par Rang)",
        adTariffAdminOnly: "Réservé à l'administrateur système.",
        adTariffHint: "Taux journalier (FCFA) appliqué automatiquement selon le rang de l'agent, en mission Interne ou Externe. Une modification s'applique immédiatement au calcul des indemnités.",
        adTariffRank: "Rang",
        adTariffInterne: "Taux Interne (FCFA/j)",
        adTariffExterne: "Taux Externe (FCFA/j)",
        adFilterPlaceholder: "Filtrer par nom / CN / département...",
        adTriggerSync: "Lancer la Synchronisation Active Directory",
        adSyncing: "Synchronisation en cours...",
        thId: "ID",
        thFullName: "Nom Complet",
        thStructure: "Structure",
        adDelete: "Supprimer",
        adDeleteConfirm: "Confirmer la suppression définitive ?",
        adDeptListTitle: "Directions / Structures",
        adAccessNoteCreate: "Création réservée à l'administrateur système et au personnel DRH.",
        adAccessNoteDelete: "Suppression réservée à l'administrateur système.",
        adColHead: "Responsable",
        adColActions: "Actions",

        // Dashboard access notices
        dashDrhOnlyNotice: "Important : seul le personnel de la DRH est habilité à établir un ordre de mission. L'établissement d'un mandat de mission est réservé aux agents ayant la désignation CEA, Chef de Cellule, Sous-Directeur ou Directeur (le compte administrateur conserve toutes les fonctionnalités).",
        modMyMissionsTitle: "Mes Missions (Agent)",
        modMyMissionsDesc: "Vos ordres de mission, notifications, PDF officiel et dépôt de votre rapport.",
        modReservedDrh: "Réservé au personnel DRH",
        omRuleShort: "Seul le personnel de la DRH est habilité à établir un ordre de mission.",
        navMyMissions: "Mes Missions",

        // Direction selector on mandate / mission order forms
        lblDirectionInitiatrice: "Direction / Structure Initiatrice",
        selectDirectionPlaceholder: "— Sélectionner une direction —",
        mandatRuleNotice: "Seuls les agents ayant la désignation CEA, Chef de Cellule, Sous-Directeur ou Directeur (ou l'administrateur) peuvent établir un mandat de mission.",
        mandatAccessDenied: "Votre désignation ne vous autorise pas à établir un mandat de mission.",
        mandatOfficialDoc: "Document officiel du mandat (à imprimer / signer)",
        mandatDeleteTitle: "Supprimer ce mandat (en cas d'erreur)",
        mandatDeleteConfirm: "Confirmer la suppression définitive du mandat de mission :",
        mandatDeleteWarn: "Cette action supprime aussi les étapes et les ordres de mission générés à partir de ce mandat (ainsi que leurs avances et rapports).",

        // Staff portal (my-missions)
        myMissionsTitle: "Portail de l'Agent — Mes Missions",
        myMissionsSub: "Vos ordres de mission, notifications, téléchargement du PDF officiel et dépôt du rapport de mission",
        mmNotifications: "Notifications",
        mmNoNotifications: "Aucune notification.",
        mmMarkAllRead: "Tout marquer comme lu",
        mmMyOrders: "Mes Ordres de Mission",
        mmNoOrders: "Aucun ordre de mission ne vous est assigné pour le moment.",
        mmDownloadPdf: "Télécharger le PDF",
        mmUploadReport: "Scanner / Déposer mon rapport",
        mmReportSubmitted: "Rapport déposé (en attente de validation DRH)",
        mmReportValidated: "Rapport validé",
        mmReportModalTitle: "Déposer mon rapport de mission",
        mmReportFileLabel: "Fichier scanné du rapport (PDF)",
        mmIdentifyPrompt: "Saisissez votre matricule pour afficher vos missions",

        // Report validation note (HR)
        repValidateNoteSP: "La validation d'un rapport est réservée au personnel DRH du Service du Personnel (désignation « SP »)."
    },

    en: {
        // App Brand & Navigation
        appBrand: "SMOMA - TRB Cameroon",
        appTitle: "TELECOMMUNICATIONS REGULATORY BOARD",
        appSubtitle: "Staff Mission Order Management Application (SMOMA)",
        navDashboard: "Dashboard",
        navMandats: "DG Mandates",
        navOrdres: "Mission Orders",
        navPersonnel: "Personnel",
        navDepartments: "Directions & Depts",
        navValidation: "Validation Desk",
        navReports: "Print & Reports",
        navPayments: "Mission Payments",
        navAnnual: "Annual Report",
        navAdmin: "Admin & AD",
        logout: "Logout",
        connectedSession: "Connected Session",
        welcomeUser: "Welcome",

        // Login Module
        loginPageTitle: "TRB - System Login",
        republicOfCameroon: "Republic of Cameroon",
        peaceWorkFatherland: "Peace - Work - Fatherland",
        telecomRegBoard: "Telecommunications Regulatory Board",
        portalSubTitle: "Institutional Authentication Portal",
        identityLabel: "Identity (Email, Username or Matricule)",
        identityPlaceholder: "name.surname@art.cm or matricule (e.g. ART-002)",
        passwordLabel: "Secure Password",
        loginSubmitBtn: "Log in to Portal",
        loginErrorMsg: "Incorrect institutional credentials or access denied.",
        networkErrorMsg: "Network error: Unable to connect to the Spring Boot server.",
        loginCredits: "2026 Telecommunications Regulatory Board (TRB) - Republic of Cameroon",

        // Main Index Page
        mainHeader: "Staff Mission Order Management Application (SMOMA)",
        mainSubheader: "Telecommunications Regulatory Board (TRB) - Yaoundé Headquarters. Centralized platform for managing DG-signed mandates, mission orders, department structures, personnel, and budget compliance.",
        sectionOrg: "Organizational Structure & Staff",
        sectionMissions: "Operational Mission Management",
        sectionStats: "System Overview",
        accessBtn: "Access Module",

        modDepartmentsTitle: "Directorates & Depts",
        modDepartmentsDesc: "Manage TRB organizational structure (HRD, DGF, TD, DLCI, DPS, etc.).",
        modPersonnelTitle: "Personnel Management",
        modPersonnelDesc: "Create and update staff profiles, positions, and matricules.",
        modAdminTitle: "Admin & AD Sync",
        modAdminDesc: "Active Directory synchronization and local access accounts.",
        modAdminReserved: "Administrator only",
        modAnnualDashboardTitle: "Staff Annual Report",
        modAnnualDashboardDesc: "Annual record of missions completed per staff member for the fiscal year.",
        modMandatsTitle: "Mandates (DG)",
        modMandatsDesc: "General mission mandates signed by the General Manager with unique ref.",
        modOrdresTitle: "Mission Orders",
        modOrdresDesc: "Precise types, objectives, itinerary steps, transport, and expenses (with/without).",
        modValidationTitle: "Validation Desk",
        modValidationDesc: "Hierarchical validation by Department Heads and General Management.",
        modReportsTitle: "Print & Reports",
        modReportsDesc: "Official PDF generation and digitization of end-of-mission reports.",
        modPaymentsTitle: "Mission Payments",
        modPaymentsDesc: "Expense tracking, payment validation, and mission financial compliance.",

        statMandats: "Signed DG Mandates",
        statOrders: "Mission Orders",
        statPending: "Awaiting Validation",
        statStaff: "Enrolled Staff (AD)",

        // Common Table & Form terms
        thRef: "Reference",
        thMandatRef: "Mandate Ref.",
        thAgent: "Staff Member",
        thObject: "General Purpose",
        thMotif: "Regulatory Justification",
        thTypeTransport: "Type & Transport",
        thPeriod: "Overall Period",
        thExecPeriod: "Execution Period",
        thFinancialRegime: "Financial Regime",
        thStatus: "Status & Immutability",
        thActions: "Actions",
        thMatricule: "Matricule",
        thName: "Full Name",
        thDept: "Department",
        thFunction: "Job Title",
        thGrade: "Grade / Rank",
        thEmail: "Email",
        thPhone: "Phone",
        thDeparture: "Departure",
        thDestination: "Destination",
        thTransport: "Transport",
        thStep: "Step",

        // Mission Order & Mandat actions & buttons
        btnCreateMandat: "Create New Mandate",
        btnCreateDirectOm: "Create Direct OM",
        btnUploadScan: "Upload Signed Scan",
        btnViewSideBySide: "Side by Side View",
        btnDownloadPdf: "PDF",
        btnViewForm: "View OM Form",
        btnPrintForm: "Print Form",
        btnCancel: "Cancel",
        btnSave: "Save",
        btnClose: "Close",
        btnSearch: "Search",
        btnFilter: "Filter",
        btnReset: "Reset",

        // Mission attributes & labels
        lblMandatRef: "Mandate Reference",
        lblMotifReglementaire: "Regulatory Justification",
        lblRefJustification: "Justification Ref. / Article",
        refJustifHint: "Reference of the act authorising the mission (Agency / Directorate General / initiating structure / serial no.). Generated automatically, editable.",
        stepCityPlaceholder: "Step city (major cities / ART antennas)",
        stepCityPrompt: "Enter the step city / location (not in the list):",
        stepAgentsLabel: "Staff assigned to this step",
        stepAgentsEmptyHint: "Select the mandate team above first.",
        lblObjetGeneral: "Mission General Purpose",
        lblObjectifsSpecifiques: "Specific Objectives",
        lblTypeMission: "Mission Type",
        lblFinancialRegime: "Financial Regime",
        lblTransportMode: "Means of Transport",
        lblStartDate: "Start Date",
        lblEndDate: "End Date",
        lblSansFrais: "WITHOUT EXPENSES Mandate (Red Stamp)",
        lblForceMajeure: "Force Majeure Regularization (< 48h)",
        lblTeamSelection: "Designated Staff Members",
        lblSearchMatriculePlaceholder: "Search by matricule or name...",
        lblSelectAgent: "Select a staff member",
        lblSelectMandat: "Select associated framework mandate",
        lblItinerarySteps: "Mission Steps & Itinerary",
        lblAddStep: "Add a Step",
        lblTravelDate: "Travel Date",
        lblStepTransport: "Step Transport Mode",

        // Mission Types
        typeInterne: "Internal (Cameroon)",
        typeExterne: "External (International)",
        typeFormation: "Training",
        typeEtude: "Study / Benchmarking",
        typeRepresentation: "Official Representation",

        // Transport Modes
        transportServiceVehicle: "Service Vehicle",
        transportPlane: "Plane",
        transportMixed: "Mixed (Vehicle and Plane)",
        transportTrain: "Train (Camrail)",
        transportPersonalVehicle: "Personal Vehicle",

        // Financial Regimes & Badges
        regimeAvecFrais: "WITH MISSION EXPENSES",
        regimeSansFrais: "WITHOUT EXPENSES (Red Stamp)",
        badgeSigned: "Signed & Locked",
        badgeDraft: "Editable Draft",
        badgePending: "Pending Signed Scan",
        badgeValidated: "Validated",

        // Detailed Mission Order Official Form
        officialDocHeadingLeft: "RÉPUBLIQUE DU CAMEROUN\nPaix - Travail - Patrie\n---------------\nAGENCE DE RÉGULATION\nDES TÉLÉCOMMUNICATIONS\nDirection Générale",
        officialDocHeadingRight: "REPUBLIC OF CAMEROON\nPeace - Work - Fatherland\n---------------\nTELECOMMUNICATIONS\nREGULATORY BOARD\nDirectorate General",
        omFormTitle: "INDIVIDUAL MISSION ORDER",
        omFormRef: "Official Ref N°",
        omSectionStaff: "I. STAFF IDENTIFICATION",
        omSectionMission: "II. MISSION DETAILS",
        omSectionItinerary: "III. ITINERARY AND MISSION STEPS",
        omSectionFinancial: "IV. FINANCIAL REGIME & ALLOWANCES",
        omSectionSignatures: "V. OFFICIAL VISAS & SIGNATURES",
        omSignatureStaff: "Staff Member Signature / Visa",
        omSignatureDept: "Head of Department / HRD",
        omSignatureGm: "The General Manager (Authorized Signatory)",

        // Personnel Module
        personnelTitle: "Staff Directory",
        personnelSubtitle: "Management of staff records, organizational assignments, grades, and individual accounts",
        btnAddAgent: "Add Staff Member",
        searchPersonnelPlaceholder: "Search by name, surname or matricule...",
        allDepartments: "All Directorates / Structures",
        allGrades: "All Grades",
        btnCompteIndividuel: "Account",

        // Departments Module
        deptsTitle: "TRB Organizational Chart & Structure",
        deptsSubtitle: "Official repository of Directorates, Divisions, Services, and Regional Delegations",

        // Validation Portal
        validationTitle: "Validation Desk & Signatures (GM)",
        validationSubtitle: "Hierarchical validation and official endorsement of mandates and mission orders",
        btnApprove: "Approve & Sign",
        btnReject: "Reject",

        // HR & Reports Module
        reportsTitle: "Printing & End-of-Mission Reports",
        reportsSubtitle: "Submission of mission reports and receipts required for allowance balance liquidation",
        btnDepositReport: "Submit a Report",

        // Payments Module
        paymentsTitle: "Mission Advances & Allowances Desk",
        paymentsSubtitle: "Management of expense advances (75% Internal / 90% External) and balance liquidation",
        btnRequestAdvance: "Request Advance",
        thAdvance75: "Advance (75% / 90%)",
        thRemainingSolde: "Remaining Balance",

        // Annual Dashboard
        annualTitle: "Annual Mission Activity Summary",
        annualSubtitle: "Annual consolidation of signed mandates, deployed personnel, and budget expenditures",
        btnPrintAnnual: "Print Summary",
        cardSignedMandats: "DG Mandates Signed",
        cardIndividualOrders: "Individual Mission Orders",
        cardMissionReports: "Mission Reports Filed",
        cardNoExpenseMissions: "NO-COST Missions (Red Seal)",
        annualRecapTitle: "Summary by Directorate & Service",
        annualRecapDesc: "Consolidated annual view available for the 2026 fiscal year.",

        // Annual Dashboard — gender & mission analysis
        analysisTitle: "Gender Analysis & Mission Indicators",
        analysisStaffMale: "Male Staff",
        analysisStaffFemale: "Female Staff",
        analysisAvgDuration: "Average Mission Duration",
        analysisDaysUnit: "days",
        analysisTotalIndemnites: "Total Indemnities Calculated",
        currencyXaf: "XAF",
        analysisMissionsByGender: "Missions Carried Out by Gender",
        analysisGender: "Gender",
        analysisMissionCount: "Number of Missions",
        analysisAgentCount: "Distinct Staff",
        analysisShare: "Share (%)",
        analysisMissionsByType: "Missions by Type",
        analysisType: "Type",
        analysisByDepartment: "Missions by Directorate / Department (Top 10)",

        // Admin Panel
        adminTitle: "Active Directory Administration & Sync",
        tabUsers: "Synchronized Users",
        tabGroups: "AD Groups & Roles",
        tabDirectory: "Full AD Directory",
        btnSyncNow: "Start AD Sync",
        adStatusConnected: "Connected to AD",
        adStatusOffline: "Offline Mode",

        // Shared / generic
        commonLoading: "Loading...",
        commonLoadError: "Loading error.",
        commonNoData: "No data available.",
        commonTo: "to",
        commonYes: "Yes",
        commonNo: "No",
        commonActive: "ACTIVE",
        badgeWithFeesShort: "WITH EXPENSES",
        badgeNoFeesShort: "NO EXPENSES",
        badgeSignedLocked: "SIGNED (LOCKED)",
        badgeDraftShort: "Draft",
        badgeAwaitingScan: "Awaiting scan",
        badgeScanValidated: "Scan Validated",

        // Common table headers
        thReportTitle: "Report Title",
        thWriter: "Author",
        thCategory: "Category",
        thDepositDate: "Submission Date",
        thValidationStatus: "Validation Status",
        thOmRef: "OM Ref.",
        thMissionTypeShort: "Mission Type",
        thTotalIndemnity: "Total Allowance",
        thRegime: "Regime",

        // Validation portal
        hdrValidationSub: "Hierarchical Validation Desk & General Management (GM)",
        valDeskHeading: "Validation Desk & Signatures",
        valDeskSub: "Validation of mission mandates awaiting the General Manager's signature",
        valPendingCard: "Mandates Awaiting Signature",
        valNoPending: "No mandate awaiting validation.",
        valProcessBtn: "Process in the Mandates Module",

        // Reports / HR processing
        hdrReportsSub: "Submission of Mission Reports & Supporting Documents",
        repHeading: "Mission Reports Submission & Review",
        repSub: "Submission of the mission report and receipts is required to pay the expense balance (25% or 10%)",
        repNoReports: "No mission report submitted.",
        repModalTitle: "Submit a Mission Report",
        repOmConcerned: "Related Mission Order",
        repCategoryLabel: "Report Category",
        repFileLabel: "Report PDF File",
        repSummaryLabel: "Summary / Travel Synthesis",
        repSubmitBtn: "Submit the Report",
        repValidateBtn: "Validate",
        repValidated: "Validated (Balance Released)",
        repPendingCheck: "Pending review",
        catControl: "Control / Regulation",
        catTraining: "Training / Internship",
        catStudy: "Study and Analysis",
        catRepresentation: "Official Representation",
        catOther: "Other",

        // Payments / advances
        hdrPaymentsSub: "Management of Expense Advances (75% Internal / 90% External) & Balance Liquidation",
        payHeading: "Advances & Allowances Desk",
        paySub: "Allowance scale calculation and management of mission expense advance requests",
        payModalTitle: "Request an Expense Advance",
        paySelectOm: "Select the Mission Order",
        payRequestDate: "Advance Request Date",
        payRequestDateHint: "Business rule: the request must be made BEFORE departure on mission.",
        payRateHint: "Automatic advance rate: 75% for Internal missions, 90% for External missions.",
        paySubmitBtn: "Submit the Request",
        payBtnAdvance: "Advance",
        payBtnPayBalance: "Pay the balance",
        payBalancePaid: "Balance paid",
        payLiquidated: "Liquidated",
        payFinanceRouteNote: "Every advance or balance request is forwarded to the Directorate of Finance for approval. The beneficiary is then notified: cash collection at the Directorate of Finance or bank transfer.",
        payApprovalTitle: "Approval — Directorate of Finance",
        payPaymentChannel: "Payment channel",
        payChannelCash: "Cash — collect at the Directorate of Finance",
        payChannelTransfer: "Bank transfer",
        payTransferRef: "Transfer reference",
        payTransferRefRequired: "Please enter the bank-transfer reference.",
        payApprovalNote: "The beneficiary will get a notification in \"My Missions\" stating the payment channel.",
        payApproveConfirm: "Approve & Notify",
        payApproveAdvance: "Approve the advance",
        payAwaitingFinance: "Awaiting Directorate of Finance",
        payAdvancePaid: "Advance paid",
        payApprovedNotified: "Request approved. The beneficiary has been notified of the payment channel.",
        payRequestSentFinance: "Request recorded and forwarded to the Directorate of Finance for approval.",

        // Personnel
        hdrPersonnelSub: "Staff Directory & Individual Accounts",
        persHeading: "Staff Directory",
        persSub: "Management of staff records, organizational assignments, grades, and individual account review",
        persNoMatch: "No staff member matches the search criteria.",
        persAddTitle: "Add a New Staff Member",
        persColLastFirst: "Full Name",
        persColStructure: "Structure / Directorate",
        persColFunction: "Job Title",
        persColGrade: "Grade",
        persColContact: "Contact",
        persColStatus: "Status",
        persAccountBtn: "Account",
        persNomLabel: "Last Name",
        persPrenomLabel: "First Name",
        persRangLabel: "Rank / Authorization",
        persMatriculeLabel: "Unique Matricule",
        btnSaveAgent: "Save Staff Member",
        btnEdit: "Edit",
        persEditTitle: "Edit Staff Member Record",
        persGenreLabel: "Gender",
        persDobLabel: "Date of Birth",
        persContractLabel: "Contract Type",
        persHireDateLabel: "Hire Date",
        persHirePlaceLabel: "Place of Hire",
        persRetirementEligible: "Eligible for retirement",
        persRetirementHint: "This staff member has reached or passed the retirement age (60 years).",
        persCompteTitle: "Staff Individual Account",
        persMissionsDone: "Missions Completed",
        persDaysOnMission: "Days on Mission",
        persTotalIndemnities: "Total Allowances",
        persAdvancesReceived: "Advances Received",
        persBalancesRemaining: "Remaining Balances",
        persOmHistory: "Mission Orders History",

        // Departments
        hdrDeptsSub: "ART Organizational Chart, Directorates, Divisions and Services",
        deptsBrandSub: "Organizational Chart & Structures",
        deptsPageTitle: "Organization of the Telecommunications Regulatory Board",
        deptsPageSub: "Organizational structure in accordance with decree no. 2020/727 of 03 December 2020",
        deptsSearchPlaceholder: "Search for a department, directorate or unit...",
        deptsStatCentral: "Central Directorates",
        deptsStatAttached: "Attached Services",
        deptsStatSub: "Sub-Directorates",
        deptsStatRegional: "Decentralized Services",
        deptsNoResults: "No results found",
        deptsNoResultsHint: "Try another search term.",
        btnPrintShort: "Print",
        navBackDashboard: "Back to Dashboard",

        // Annual dashboard
        hdrAnnualSub: "Annual Mission Activity Summary by Staff Member and Structure",

        // Admin panel
        hdrAdminSub: "Accounts, Roles, Permissions and Active Directory Synchronization",
        adCreateUser: "Create New User",
        adCreateDept: "Create New Department",
        adSyncedDbUsers: "Synchronized Database Users",
        adUsername: "Username",
        adPassword: "Password",
        adEmailLabel: "Email",
        adRole: "Role",
        adFirstName: "First Name",
        adLastName: "Last Name",
        adDeptStructure: "Department / Structure",
        adCreateUserBtn: "Create User",
        adDeptName: "Department Name",
        adAcronym: "Acronym",
        adHeadOfDept: "Head of Department",
        adCreateDeptBtn: "Create Department",
        adDeptDupHint: "Existing departments show up as you type — check before creating, to avoid a duplicate.",
        adDeptDupBlock: "This department already exists — pick it from the list instead of creating a duplicate.",
        adGenre: "Gender",
        adGenreUnspecified: "Unspecified",
        adGenreMale: "Male",
        adGenreFemale: "Female",
        adRang: "Rank",
        adFonction: "Function",
        adOptionNone: "—",
        adSave: "Save",
        adTariffTitle: "Mission Indemnity Tariff Grid (by Rank)",
        adTariffAdminOnly: "Reserved for the system administrator.",
        adTariffHint: "Daily rate (XAF) applied automatically according to the staff member's rank, for Internal or External missions. A change applies immediately to indemnity calculations.",
        adTariffRank: "Rank",
        adTariffInterne: "Internal Rate (XAF/day)",
        adTariffExterne: "External Rate (XAF/day)",
        adFilterPlaceholder: "Filter by name / CN / department...",
        adTriggerSync: "Trigger Active Directory Sync",
        adSyncing: "Synchronizing...",
        thId: "ID",
        thFullName: "Full Name",
        thStructure: "Structure",
        adDelete: "Delete",
        adDeleteConfirm: "Confirm permanent deletion?",
        adDeptListTitle: "Directorates / Structures",
        adAccessNoteCreate: "Creation restricted to the system administrator and HR staff.",
        adAccessNoteDelete: "Deletion restricted to the system administrator.",
        adColHead: "Head",
        adColActions: "Actions",

        // Dashboard access notices
        dashDrhOnlyNotice: "Important: only HR (DRH) staff are authorized to issue a mission order. Creating a mission mandate is restricted to staff holding the designation CEA, Chef de Cellule, Sous-Directeur or Directeur (the administrator account keeps every functionality).",
        modMyMissionsTitle: "My Missions (Staff)",
        modMyMissionsDesc: "Your mission orders, notifications, official PDF and report submission.",
        modReservedDrh: "Restricted to HR (DRH) staff",
        omRuleShort: "Only HR (DRH) staff are authorized to issue a mission order.",
        navMyMissions: "My Missions",

        // Direction selector on mandate / mission order forms
        lblDirectionInitiatrice: "Initiating Directorate / Structure",
        selectDirectionPlaceholder: "— Select a directorate —",
        mandatRuleNotice: "Only staff holding the designation CEA, Chef de Cellule, Sous-Directeur or Directeur (or the administrator) may issue a mission mandate.",
        mandatAccessDenied: "Your designation does not allow you to issue a mission mandate.",
        mandatOfficialDoc: "Official mandate document (to print / sign)",
        mandatDeleteTitle: "Delete this mandate (in case of error)",
        mandatDeleteConfirm: "Confirm permanent deletion of the mission mandate:",
        mandatDeleteWarn: "This also deletes its steps and the mission orders generated from it (along with their advances and reports).",

        // Staff portal (my-missions)
        myMissionsTitle: "Staff Portal — My Missions",
        myMissionsSub: "Your mission orders, notifications, official PDF download and mission report submission",
        mmNotifications: "Notifications",
        mmNoNotifications: "No notifications.",
        mmMarkAllRead: "Mark all as read",
        mmMyOrders: "My Mission Orders",
        mmNoOrders: "No mission order is assigned to you yet.",
        mmDownloadPdf: "Download PDF",
        mmUploadReport: "Scan / Submit my report",
        mmReportSubmitted: "Report submitted (awaiting HR validation)",
        mmReportValidated: "Report validated",
        mmReportModalTitle: "Submit my mission report",
        mmReportFileLabel: "Scanned report file (PDF)",
        mmIdentifyPrompt: "Enter your matricule to display your missions",

        // Report validation note (HR)
        repValidateNoteSP: "Validating a report is restricted to HR staff of the Service du Personnel (designation \"SP\")."
    }
};

class SMOMAI18n {
    constructor() {
        this.currentLang = localStorage.getItem('smoma_lang') || 'fr';
    }

    getLang() {
        return this.currentLang;
    }

    t(key) {
        if (!key) return '';
        const dict = SMOMA_TRANSLATIONS[this.currentLang] || SMOMA_TRANSLATIONS['fr'];
        return dict[key] || SMOMA_TRANSLATIONS['fr'][key] || key;
    }

    switchLanguage(lang) {
        if (lang !== 'fr' && lang !== 'en') lang = 'fr';
        this.currentLang = lang;
        localStorage.setItem('smoma_lang', lang);
        document.documentElement.lang = lang;
        this.applyTranslations();

        // Dispatch language change event for any custom listeners
        window.dispatchEvent(new CustomEvent('smoma_lang_changed', { detail: { lang } }));
    }

    applyTranslations() {
        const lang = this.currentLang;
        const dict = SMOMA_TRANSLATIONS[lang] || SMOMA_TRANSLATIONS['fr'];

        // 1. Text elements with data-i18n
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            if (dict[key]) {
                el.textContent = dict[key];
            }
        });

        // 2. HTML elements with data-i18n-html
        document.querySelectorAll('[data-i18n-html]').forEach(el => {
            const key = el.getAttribute('data-i18n-html');
            if (dict[key]) {
                el.innerHTML = dict[key];
            }
        });

        // 3. Placeholders with data-i18n-placeholder
        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            const key = el.getAttribute('data-i18n-placeholder');
            if (dict[key]) {
                el.setAttribute('placeholder', dict[key]);
            }
        });

        // 4. Tooltip / title with data-i18n-title
        document.querySelectorAll('[data-i18n-title]').forEach(el => {
            const key = el.getAttribute('data-i18n-title');
            if (dict[key]) {
                el.setAttribute('title', dict[key]);
            }
        });

        // 5. Update language dropdown label if present
        const labelEl = document.getElementById('currentLangLabel');
        if (labelEl) {
            labelEl.textContent = lang === 'fr' ? 'Français (FR)' : 'English (EN)';
        }
    }

    /**
     * Renders standard bilingual switcher component into navbar or header container
     */
    injectLanguageSwitcher(containerSelector = '#langContainer') {
        const container = document.querySelector(containerSelector);
        if (!container) return;

        const lang = this.currentLang;
        const label = lang === 'fr' ? 'Français (FR)' : 'English (EN)';

        container.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-light dropdown-toggle d-flex align-items-center" type="button" id="langDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="fa-solid fa-globe me-1"></i> <span id="currentLangLabel">${label}</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="langDropdown">
                    <li><a class="dropdown-item ${lang === 'fr' ? 'active' : ''}" href="#" onclick="smomaI18n.switchLanguage('fr'); return false;">🇫🇷 Français (FR)</a></li>
                    <li><a class="dropdown-item ${lang === 'en' ? 'active' : ''}" href="#" onclick="smomaI18n.switchLanguage('en'); return false;">🇬🇧 English (EN)</a></li>
                </ul>
            </div>
        `;
    }
}

// Global instance
const smomaI18n = new SMOMAI18n();
function t(key) { return smomaI18n.t(key); }
function switchLanguage(lang) { smomaI18n.switchLanguage(lang); }

// Auto-run on DOMContentLoaded
document.addEventListener('DOMContentLoaded', () => {
    smomaI18n.applyTranslations();
});
