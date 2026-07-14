package smoma.controller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mission_orders") 
public class MissionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id") 
    private Long id;

    @Column(name = "mission_code", unique = true, nullable = false)
    private String missionCode;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "staff_id", nullable = false)
    private Long staffId = 1L; 

    @Column(name = "staff_member")
    private String staffMember;

    @Column(name = "current_state", length = 50, nullable = false)
    private String currentState = "PENDING";

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays = 0;

    @Column(name = "duration", nullable = false)
    private String duration = "0";

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public String getStaffMember() { return staffMember; }
    public void setStaffMember(String staffMember) { this.staffMember = staffMember; }

    // Intercepts state changes to keep current_state and status identical
    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { 
        this.currentState = currentState; 
        this.status = currentState; 
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.currentState = status; 
    }

    // Intercepts duration changes to keep duration_days and duration identical
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { 
        this.durationDays = durationDays; 
        this.duration = (durationDays != null) ? String.valueOf(durationDays) : "0"; 
    }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { 
        this.duration = duration; 
        if (duration != null) {
            try {
                this.durationDays = Integer.parseInt(duration);
            } catch (NumberFormatException e) {
                this.durationDays = 0;
            }
        }
    }
}