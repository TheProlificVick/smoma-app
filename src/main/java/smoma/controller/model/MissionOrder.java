package smoma.controller.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "mission_orders")
public class MissionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    private MissionRequest.MissionStatus status;

    @OneToOne
    @JoinColumn(name = "mission_request_id")
    private MissionRequest missionRequest;

    @OneToOne
    @JoinColumn(name = "form_detail_id")
    private MissionFormDetail formDetail;

    public MissionOrder() {}

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public MissionRequest.MissionStatus getStatus() { return status; }
    public void setStatus(MissionRequest.MissionStatus status) { this.status = status; }

    public MissionRequest getMissionRequest() { return missionRequest; }
    public void setMissionRequest(MissionRequest missionRequest) { this.missionRequest = missionRequest; }

    public MissionFormDetail getFormDetail() { return formDetail; }
    public void setFormDetail(MissionFormDetail formDetail) { this.formDetail = formDetail; }
}