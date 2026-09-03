package smoma.controller.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personnel")
public class Personnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String telephone;
    private String departement;
    private String service;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private Statut statut;

    private String fonction;
    private String rang;
    private LocalDate dateEmbauche;
    private LocalDate dateNaissance;
    private String adresse;
    private String ville;
    private String pays;
    private Long superieurHierarchiqueId;

    @OneToMany(mappedBy = "personnel")
    private List<OrdreDeMission> ordresDeMission = new ArrayList<>();

    @OneToMany(mappedBy = "personnel")
    private List<MandatDeMission> mandatsDeMission = new ArrayList<>();

    public Personnel() {
    }

    public Personnel(String nom, String prenom, String matricule, String email, String telephone, Grade grade) {
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.email = email;
        this.telephone = telephone;
        this.grade = grade;
        this.statut = Statut.ACTIF;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String getRang() {
        return rang;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public Long getSuperieurHierarchiqueId() {
        return superieurHierarchiqueId;
    }

    public void setSuperieurHierarchiqueId(Long superieurHierarchiqueId) {
        this.superieurHierarchiqueId = superieurHierarchiqueId;
    }

    public List<OrdreDeMission> getOrdresDeMission() {
        return ordresDeMission;
    }

    public void setOrdresDeMission(List<OrdreDeMission> ordresDeMission) {
        this.ordresDeMission = ordresDeMission;
    }

    public List<MandatDeMission> getMandatsDeMission() {
        return mandatsDeMission;
    }

    public void setMandatsDeMission(List<MandatDeMission> mandatsDeMission) {
        this.mandatsDeMission = mandatsDeMission;
    }

    public String getFullName() {
        return (nom == null ? "" : nom) + " " + (prenom == null ? "" : prenom);
    }

    public boolean isActive() {
        return statut == null || statut == Statut.ACTIF;
    }

    public enum Grade {
        A,
        B,
        C,
        D,
        E
    }

    public enum Statut {
        ACTIF,
        INACTIF,
        CONGE,
        RETRAIT
    }

    @Override
    public String toString() {
        return getFullName() + " (" + matricule + ")";
    }
}
