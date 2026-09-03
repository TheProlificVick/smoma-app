package smoma.controller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "company_settings")
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String slogan;
    private String delegation;
    private String headerLeftFr;
    private String headerLeftEn;

    @Column(length = 500)
    private String logoPath;

    private String acronym;
    private String city;
    private String address;
    private String phone;
    private String email;

    public CompanySettings() {
        this.companyName = "AGENCE DE REGULATION DES TELECOMMUNICATIONS";
        this.slogan = "Paix - Travail - Patrie / Peace - Work - Fatherland";
        this.delegation = "DELEGATION REGIONALE CENTRE-SUD-EST";
        this.headerLeftFr = "REPUBLIQUE DU CAMEROUN\nAGENCE DE REGULATION DES TELECOMMUNICATIONS";
        this.headerLeftEn = "REPUBLIC OF CAMEROON\nTELECOMMUNICATIONS REGULATORY BOARD";
        this.logoPath = "/images/ART logo.jpg";
        this.acronym = "ART";
        this.city = "Yaoundé, Cameroun";
        this.address = "B.P. 6132 Yaoundé - Immeuble ART, Quartier Bastos";
        this.phone = "+237 222 23 03 80 / 222 23 21 64";
        this.email = "contact@art.cm";
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getDelegation() {
        return delegation;
    }

    public void setDelegation(String delegation) {
        this.delegation = delegation;
    }

    public String getHeaderLeftFr() {
        return headerLeftFr;
    }

    public void setHeaderLeftFr(String headerLeftFr) {
        this.headerLeftFr = headerLeftFr;
    }

    public String getHeaderLeftEn() {
        return headerLeftEn;
    }

    public void setHeaderLeftEn(String headerLeftEn) {
        this.headerLeftEn = headerLeftEn;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
}
