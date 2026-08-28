package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mission_requests")
public class MissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Column(length = 1000)
    private String justification;
    
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    @Column(length = 50)
    private String missionType = "INTERNE";

    @Column(length = 50)
    private String feeType = "WITHOUT_FEES";

    @Column(length = 50)
    private String currentStage = "INITIATED";

    @Column(length = 50)
    private String paymentStatus = "PENDING";

    private boolean mandateApproved = false;
    private boolean mandateSignedByGeneralManager = false;
    private String mandateReference;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    public MissionRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public MissionStatus getStatus() { return status; }
    public void setStatus(MissionStatus status) { this.status = status; }

    public User getInitiator() { return initiator; }
    public void setInitiator(User initiator) { this.initiator = initiator; }

    public User getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(User assignedStaff) { this.assignedStaff = assignedStaff; }

    public String getMissionType() { return missionType; }
    public void setMissionType(String missionType) { this.missionType = missionType; }

    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public boolean isMandateApproved() { return mandateApproved; }
    public void setMandateApproved(boolean mandateApproved) { this.mandateApproved = mandateApproved; }

    public boolean isMandateSignedByGeneralManager() { return mandateSignedByGeneralManager; }
    public void setMandateSignedByGeneralManager(boolean mandateSignedByGeneralManager) { this.mandateSignedByGeneralManager = mandateSignedByGeneralManager; }

    public String getMandateReference() { return mandateReference; }
    public void setMandateReference(String mandateReference) { this.mandateReference = mandateReference; }

    public enum MissionStatus {
        INITIATED,
        GM_APPROVED,
        REJECTED,
        FORM_COMPLETED,
        ISSUED
    }
}