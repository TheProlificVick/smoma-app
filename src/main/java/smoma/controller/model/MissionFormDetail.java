package smoma.controller.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mission_form_details")
public class MissionFormDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "mission_request_id", nullable = false)
    private MissionRequest missionRequest;

    
    private String originCity;
    private String targetCities;
    private String transitRoutes;

    private String costCodes;
    private BigDecimal perDiemAmount;
    private BigDecimal totalAllowance;

    
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalComputedActiveDays;

    
    public MissionFormDetail() {
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

    public String getOriginCity() { 
        return originCity; 
    }
    
    public void setOriginCity(String originCity) { 
        this.originCity = originCity; 
    }

    public String getTargetCities() { 
        return targetCities; 
    }
    
    public void setTargetCities(String targetCities) { 
        this.targetCities = targetCities; 
    }

    public String getTransitRoutes() { 
        return transitRoutes; 
    }
    
    public void setTransitRoutes(String transitRoutes) { 
        this.transitRoutes = transitRoutes; 
    }

    public String getCostCodes() { 
        return costCodes; 
    }
    
    public void setCostCodes(String costCodes) { 
        this.costCodes = costCodes; 
    }

    public BigDecimal getPerDiemAmount() { 
        return perDiemAmount; 
    }
    
    public void setPerDiemAmount(BigDecimal perDiemAmount) { 
        this.perDiemAmount = perDiemAmount; 
    }

    public BigDecimal getTotalAllowance() { 
        return totalAllowance; 
    }
    
    public void setTotalAllowance(BigDecimal totalAllowance) { 
        this.totalAllowance = totalAllowance; 
    }

    public LocalDate getStartDate() { 
        return startDate; 
    }
    
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return endDate; 
    }
    
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    public Integer getTotalComputedActiveDays() { 
        return totalComputedActiveDays; 
    }
    
    public void setTotalComputedActiveDays(Integer totalComputedActiveDays) { 
        this.totalComputedActiveDays = totalComputedActiveDays; 
    }
}
