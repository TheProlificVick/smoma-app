package smoma.controller.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "baremes_indemnites")
public class BaremeIndemnite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String grade;
    private String rang;
    private String fonction;

    @Enumerated(EnumType.STRING)
    private OrdreDeMission.TypeMission typeMission;

    @Column(name = "montant_journalier")
    private BigDecimal montantJournalier;

    @Column(name = "montant_forfaitaire")
    private BigDecimal montantForfaitaire;

    @Column(name = "est_actif")
    private boolean estActif;

    public BaremeIndemnite() {
        this.estActif = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getRang() {
        return rang;
    }

    public void setRang(String rang) {
        this.rang = rang;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public OrdreDeMission.TypeMission getTypeMission() {
        return typeMission;
    }

    public void setTypeMission(OrdreDeMission.TypeMission typeMission) {
        this.typeMission = typeMission;
    }

    public BigDecimal getMontantJournalier() {
        return montantJournalier;
    }

    public void setMontantJournalier(BigDecimal montantJournalier) {
        this.montantJournalier = montantJournalier;
    }

    public BigDecimal getMontantForfaitaire() {
        return montantForfaitaire;
    }

    public void setMontantForfaitaire(BigDecimal montantForfaitaire) {
        this.montantForfaitaire = montantForfaitaire;
    }

    public boolean isEstActif() {
        return estActif;
    }

    public void setEstActif(boolean estActif) {
        this.estActif = estActif;
    }
}
