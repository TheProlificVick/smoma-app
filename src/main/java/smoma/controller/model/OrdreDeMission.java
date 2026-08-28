package smoma.controller.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordres_de_mission")
public class OrdreDeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String referenceOrdre;

    @ManyToOne
    @JoinColumn(name = "mandat_de_mission_id")
    private MandatDeMission mandatDeMission;

    @ManyToOne
    @JoinColumn(name = "personnel_id")
    private Personnel personnel;

    @Enumerated(EnumType.STRING)
    private TypeMission typeMission;

    private String objectifsSpecifiques;
    private boolean avecFrais;
    private BigDecimal montantFrais;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @OneToMany(mappedBy = "ordreDeMission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EtapeMission> etapes = new ArrayList<>();

    private String rapportScannePath;
    private boolean rapportSoumis;

    public OrdreDeMission() {
    }

    public OrdreDeMission(String referenceOrdre, MandatDeMission mandatDeMission, Personnel personnel,
                          TypeMission typeMission, String objectifsSpecifiques, boolean avecFrais,
                          BigDecimal montantFrais, LocalDate dateDebut, LocalDate dateFin) {
        this.referenceOrdre = referenceOrdre;
        this.mandatDeMission = mandatDeMission;
        this.personnel = personnel;
        this.typeMission = typeMission;
        this.objectifsSpecifiques = objectifsSpecifiques;
        this.avecFrais = avecFrais;
        this.montantFrais = montantFrais;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public void addEtape(EtapeMission etape) {
        etapes.add(etape);
        etape.setOrdreDeMission(this);
    }

    public void removeEtape(EtapeMission etape) {
        etapes.remove(etape);
        etape.setOrdreDeMission(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceOrdre() {
        return referenceOrdre;
    }

    public void setReferenceOrdre(String referenceOrdre) {
        this.referenceOrdre = referenceOrdre;
    }

    public MandatDeMission getMandatDeMission() {
        return mandatDeMission;
    }

    public void setMandatDeMission(MandatDeMission mandatDeMission) {
        this.mandatDeMission = mandatDeMission;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public TypeMission getTypeMission() {
        return typeMission;
    }

    public void setTypeMission(TypeMission typeMission) {
        this.typeMission = typeMission;
    }

    public String getObjectifsSpecifiques() {
        return objectifsSpecifiques;
    }

    public void setObjectifsSpecifiques(String objectifsSpecifiques) {
        this.objectifsSpecifiques = objectifsSpecifiques;
    }

    public boolean isAvecFrais() {
        return avecFrais;
    }

    public void setAvecFrais(boolean avecFrais) {
        this.avecFrais = avecFrais;
    }

    public BigDecimal getMontantFrais() {
        return montantFrais;
    }

    public void setMontantFrais(BigDecimal montantFrais) {
        this.montantFrais = montantFrais;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public List<EtapeMission> getEtapes() {
        return etapes;
    }

    public void setEtapes(List<EtapeMission> etapes) {
        this.etapes = etapes;
    }

    public String getRapportScannePath() {
        return rapportScannePath;
    }

    public void setRapportScannePath(String rapportScannePath) {
        this.rapportScannePath = rapportScannePath;
    }

    public boolean isRapportSoumis() {
        return rapportSoumis;
    }

    public void setRapportSoumis(boolean rapportSoumis) {
        this.rapportSoumis = rapportSoumis;
    }

    public enum TypeMission {
        INTERNE,
        EXTERNE,
        SEMINAIRE,
        FORMATION,
        AUTRE
    }

    @Override
    public String toString() {
        return referenceOrdre != null ? referenceOrdre : "Ordre de mission";
    }
}
