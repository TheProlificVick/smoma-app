package smoma.dto;

import java.time.LocalDate;

public class MissionRequestDTO {
    private String title;
    private String justification;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long assignedStaffId;

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

    public Long getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(Long assignedStaffId) { this.assignedStaffId = assignedStaffId; }
}