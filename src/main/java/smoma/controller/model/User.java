package smoma.controller.model;

import jakarta.persistence.*;
import smoma.controller.model.Service.Role;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String email;
    private String nom;
    private String prenom;
    private String matricule;
    private String structure;
    private String title;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active = true;

    public User() {}

    // Constructor for legacy seeders and test cases (6 parameters)
    public User(String username, String password, String nom, String email, String structure, Role role) {
        this.username = username;
        this.password = password;
        this.nom = nom;
        this.email = email;
        this.structure = structure;
        this.role = role;
    }

    // Full constructor for Active Directory synchronization (9 parameters)
    public User(String username, String password, String email, String nom, String prenom, 
                String matricule, String structure, String title, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.structure = structure;
        this.title = title;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
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
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}