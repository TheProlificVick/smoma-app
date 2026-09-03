package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rapports_mission")
public class RapportMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordre_de_mission_id", unique = true)
    private OrdreDeMission ordreDeMission;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_depot")
    private LocalDate dateDepot;

    @Column(name = "fichier_path", length = 500)
    private String fichierPath;

    @Column(name = "justificatifs_json", columnDefinition = "TEXT")
    private String justificatifsJson;

    @Enumerated(EnumType.STRING)
    private StatutRapport statut;

    private String statutValidation;

    @Enumerated(EnumType.STRING)
    private CategorieRapport categorie;

    @Column(columnDefinition = "TEXT")
    private String observations;

    public RapportMission() {
        this.statut = StatutRapport.NON_DEPOSE;
        this.statutValidation = "EN_ATTENTE";
        this.dateDepot = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrdreDeMission getOrdreDeMission() {
        return ordreDeMission;
    }

    public void setOrdreDeMission(OrdreDeMission ordreDeMission) {
        this.ordreDeMission = ordreDeMission;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDate dateDepot) {
        this.dateDepot = dateDepot;
    }

    public String getFichierPath() {
        return fichierPath;
    }

    public void setFichierPath(String fichierPath) {
        this.fichierPath = fichierPath;
    }

    public String getJustificatifsJson() {
        return justificatifsJson;
    }

    public void setJustificatifsJson(String justificatifsJson) {
        this.justificatifsJson = justificatifsJson;
    }

    public StatutRapport getStatut() {
        return statut;
    }

    public void setStatut(StatutRapport statut) {
        this.statut = statut;
    }

    public String getStatutValidation() {
        return statutValidation;
    }

    public void setStatutValidation(String statutValidation) {
        this.statutValidation = statutValidation;
    }

    public CategorieRapport getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieRapport categorie) {
        this.categorie = categorie;
    }

    public void setCategorie(String categorieStr) {
        if (categorieStr == null || categorieStr.isBlank()) {
            this.categorie = CategorieRapport.ACTIVITE;
            return;
        }
        try {
            this.categorie = CategorieRapport.valueOf(categorieStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.categorie = CategorieRapport.ACTIVITE;
        }
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public enum StatutRapport {
        NON_DEPOSE,
        DEPOSE,
        VALIDE
    }

    public enum CategorieRapport {
        TECHNIQUE,
        FINANCIER,
        ACTIVITE,
        SYNTHÈSE,
        CONTRÔLE,
        FORMATION,
        ETUDE,
        REPRESENTATION,
        GENERAL,
        AUTRE
    }
}
