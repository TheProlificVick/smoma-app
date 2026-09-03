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

    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean actif;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

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
}
