package smoma.controller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "staff_members")
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;
    private String fullName;
    private String email;
    private String department;
    private String grade;

    @Enumerated(EnumType.STRING)
    private RoleScope role;

    private Boolean active = true;

    public StaffMember() {}

    public StaffMember(Long id, String username, String password, String fullName, String email, String department, String grade, RoleScope role, Boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.grade = grade;
        this.role = role;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public RoleScope getRole() { return role; }
    public void setRole(RoleScope role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}