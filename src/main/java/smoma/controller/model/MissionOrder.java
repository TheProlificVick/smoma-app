package smoma.controller.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mission_order")
public class MissionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mission_code", nullable = false)
    private String missionCode;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "staff_member")
    private String staffMember;

    @Column(nullable = false)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMissionCode() { return missionCode; }
    public void setMissionCode(String missionCode) { this.missionCode = missionCode; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getStaffMember() { return staffMember; }
    public void setStaffMember(String staffMember) { this.staffMember = staffMember; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}