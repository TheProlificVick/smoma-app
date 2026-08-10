package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_members")
public class StaffMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "object_guid", unique = true)
    private String objectGuid;

    @Column(name = "sam_account_name", unique = true)
    private String samAccountName;

    @Column(name = "user_principal_name", unique = true)
    private String userPrincipalName;

    private String matricule;
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String department;

    @Column(name = "role_scope")
    private String roleScope;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "is_ad_managed")
    private Boolean isAdManaged = true;

    @Column(name = "last_ad_sync_at")
    private LocalDateTime lastAdSyncAt;

    @Column(name = "ad_custom_attributes", columnDefinition = "json")
    private String adCustomAttributes;

    // Default Constructor
    public StaffMember() {}

    // Getters and Setters
    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getObjectGuid() { return objectGuid; }
    public void setObjectGuid(String objectGuid) { this.objectGuid = objectGuid; }

    public String getSamAccountName() { return samAccountName; }
    public void setSamAccountName(String samAccountName) { this.samAccountName = samAccountName; }

    public String getUserPrincipalName() { return userPrincipalName; }
    public void setUserPrincipalName(String userPrincipalName) { this.userPrincipalName = userPrincipalName; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRoleScope() { return roleScope; }
    public void setRoleScope(String roleScope) { this.roleScope = roleScope; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Boolean getIsAdManaged() { return isAdManaged; }
    public void setIsAdManaged(Boolean adManaged) { isAdManaged = adManaged; }

    public LocalDateTime getLastAdSyncAt() { return lastAdSyncAt; }
    public void setLastAdSyncAt(LocalDateTime lastAdSyncAt) { this.lastAdSyncAt = lastAdSyncAt; }

    public String getAdCustomAttributes() { return adCustomAttributes; }
    public void setAdCustomAttributes(String adCustomAttributes) { this.adCustomAttributes = adCustomAttributes; }
}