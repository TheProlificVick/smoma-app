package smoma.dto;

public class LdapUserDTO {
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String codeFonction;
    private String nomStructure;
    private String login;

    public LdapUserDTO() {
    }

    public LdapUserDTO(String matricule, String nom, String prenom, String email, String codeFonction, String nomStructure, String login) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.codeFonction = codeFonction;
        this.nomStructure = nomStructure;
        this.login = login;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeFonction() {
        return codeFonction;
    }

    public void setCodeFonction(String codeFonction) {
        this.codeFonction = codeFonction;
    }

    public String getNomStructure() {
        return nomStructure;
    }

    public void setNomStructure(String nomStructure) {
        this.nomStructure = nomStructure;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}