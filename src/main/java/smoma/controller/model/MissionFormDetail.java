package smoma.controller.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mission_form_details")
public class MissionFormDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "mission_request_id")
    private MissionRequest missionRequest;

    private String itinerary;
    private Integer durationDays;
    private BigDecimal allocatedBudget;
    private String transportMode;

    public MissionFormDetail() {
    }

    public MissionFormDetail(Long id, MissionRequest missionRequest, String itinerary, Integer durationDays, BigDecimal allocatedBudget, String transportMode) {
        this.id = id;
        this.missionRequest = missionRequest;
        this.itinerary = itinerary;
        this.durationDays = durationDays;
        this.allocatedBudget = allocatedBudget;
        this.transportMode = transportMode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MissionRequest getMissionRequest() {
        return missionRequest;
    }

    public void setMissionRequest(MissionRequest missionRequest) {
        this.missionRequest = missionRequest;
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public void setAllocatedBudget(BigDecimal allocatedBudget) {
        this.allocatedBudget = allocatedBudget;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }
}