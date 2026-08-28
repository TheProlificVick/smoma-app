package smoma.dto;

import smoma.controller.model.Service.Role;

public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String nom;
    private String prenom;
    private String matricule;
    private String structure;
    private String title;
    private Long departmentId;
    private Role role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public String getStructure() { return structure; }
    public void setStructure(String structure) { this.structure = structure; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
