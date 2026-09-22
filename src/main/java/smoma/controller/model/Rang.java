package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rangs")
public class Rang {

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

    @Column(name = "niveau")
    private Integer niveau;

    private boolean actif;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    public Rang() {
        this.actif = true;
        this.dateCreation = LocalDate.now();
    }

    public Rang(String libelle, String code, Integer niveau) {
        this();
        this.libelle = libelle;
        this.code = code;
        this.niveau = niveau;
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

    public Integer getNiveau() {
        return niveau;
    }

    public void setNiveau(Integer niveau) {
        this.niveau = niveau;
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
}
