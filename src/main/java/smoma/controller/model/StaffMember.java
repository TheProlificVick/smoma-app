package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_member")
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sam_account_name", unique = true, nullable = false)
    private String samAccountName;

    @Column(name = "matricule")
    private String matricule;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "title")
    private String title;

    @Column(name = "department")
    private String department;

    @Column(name = "role_scope")
    private String roleScope = "STAFF";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_ad_managed")
    private Boolean isAdManaged = true;

    @Column(name = "last_ad_sync_at")
    private LocalDateTime lastAdSyncAt;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSamAccountName() { return samAccountName; }
    public void setSamAccountName(String samAccountName) { this.samAccountName = samAccountName; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRoleScope() { return roleScope; }
    public void setRoleScope(String roleScope) { this.roleScope = roleScope; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsAdManaged() { return isAdManaged; }
    public void setIsAdManaged(Boolean isAdManaged) { this.isAdManaged = isAdManaged; }

    public LocalDateTime getLastAdSyncAt() { return lastAdSyncAt; }
    public void setLastAdSyncAt(LocalDateTime lastAdSyncAt) { this.lastAdSyncAt = lastAdSyncAt; }
}