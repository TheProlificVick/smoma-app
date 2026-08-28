package smoma.dto;

public class StaffMissionDashboardDTO {
    private String matricule;
    private String nomComplet;
    private String departement;
    private int anneeFiscale;
    private int totalMandats;
    private int totalOrdres;
    private long totalJours;
    private double montantTotal;

    public StaffMissionDashboardDTO() {
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public int getAnneeFiscale() {
        return anneeFiscale;
    }

    public void setAnneeFiscale(int anneeFiscale) {
        this.anneeFiscale = anneeFiscale;
    }

    public int getTotalMandats() {
        return totalMandats;
    }

    public void setTotalMandats(int totalMandats) {
        this.totalMandats = totalMandats;
    }

    public int getTotalOrdres() {
        return totalOrdres;
    }

    public void setTotalOrdres(int totalOrdres) {
        this.totalOrdres = totalOrdres;
    }

    public long getTotalJours() {
        return totalJours;
    }

    public void setTotalJours(long totalJours) {
        this.totalJours = totalJours;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }
}
