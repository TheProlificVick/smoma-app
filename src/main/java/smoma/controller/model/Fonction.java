package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "fonctions")
public class Fonction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    /** English translation of {@link #libelle}, shown instead of it when the UI language is English. */
    private String libelleEn;

    // Enforced at the DB level too (not just ReferentielController's pre-check): findByCode is
    // Optional-returning and used on every mission-order PDF generation — a duplicate code would
    // throw NonUniqueResultException there and break PDF download for every agent sharing it.
    @Column(unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean actif;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    /**
     * The directorate/structure this specific poste belongs to in the ART organigramme — e.g.
     * "Chef de Service de la Trésorerie" only exists within "Sous-Direction de la Trésorerie".
     * Nullable: a handful of legacy generic entries (seeded before this catalogue was linked to
     * the organigramme) are not tied to one department and stay selectable everywhere.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /** The hierarchical rank this poste carries — drives the mission-indemnity rate once a staff
     * member is assigned this fonction (see IndemniteService, which reads Personnel.rang). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rang_id")
    private Rang rang;

    public Fonction() {
        this.actif = true;
        this.dateCreation = LocalDate.now();
    }

    public Fonction(String libelle, String code, String description) {
        this();
        this.libelle = libelle;
        this.code = code;
        this.description = description;
    }

    public Fonction(String libelle, String libelleEn, String code, String description) {
        this(libelle, code, description);
        this.libelleEn = libelleEn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelleEn() {
        return libelleEn;
    }

    public void setLibelleEn(String libelleEn) {
        this.libelleEn = libelleEn;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Rang getRang() {
        return rang;
    }

    public void setRang(Rang rang) {
        this.rang = rang;
    }
}
