package smoma.dto;

public class FonctionRequest {
    private String libelle;
    private String libelleEn;
    private String code;
    private String description;
    private boolean actif = true;
    private Long departmentId;
    private Long rangId;

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getLibelleEn() { return libelleEn; }
    public void setLibelleEn(String libelleEn) { this.libelleEn = libelleEn; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getRangId() { return rangId; }
    public void setRangId(Long rangId) { this.rangId = rangId; }
}
