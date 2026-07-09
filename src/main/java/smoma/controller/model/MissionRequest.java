package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_requests")
public class MissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originatingDepartment; 

    @Column(nullable = false, columnDefinition = "TEXT")
    private String issueDescription; 

    @Column(nullable = false, columnDefinition = "TEXT")
    private String textJustification; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionState state = MissionState.INITIATED; 

    private String gmApprovalStamp; 
    
    @Column(nullable = false)
    private Long staffMemberId; 

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "missionRequest")
    private MissionFormDetail formDetail; 

    private LocalDateTime createdAt = LocalDateTime.now();

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginatingDepartment() { return originatingDepartment; }
    public void setOriginatingDepartment(String dept) { this.originatingDepartment = dept; }
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String desc) { this.issueDescription = desc; }
    public String getTextJustification() { return textJustification; }
    public void setTextJustification(String justification) { this.textJustification = justification; }
    public MissionState getState() { return state; }
    public void setState(MissionState state) { this.state = state; }
    public String getGmApprovalStamp() { return gmApprovalStamp; }
    public void setGmApprovalStamp(String stamp) { this.gmApprovalStamp = stamp; }
    public Long getStaffMemberId() { return staffMemberId; }
    public void setStaffMemberId(Long id) { this.staffMemberId = id; }
    public MissionFormDetail getFormDetail() { return formDetail; }
    public void setFormDetail(MissionFormDetail formDetail) { this.formDetail = formDetail; }
}
