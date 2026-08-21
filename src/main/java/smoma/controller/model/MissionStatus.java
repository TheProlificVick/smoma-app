package smoma.controller.model;

public enum MissionStatus {
    INITIATED,    // Attente DG
    GM_APPROVED,  // Approuvé DG (Attente RH)
    ISSUED_ACTIVE,// Émis / Actif
    REJECTED      // Rejeté DG
}