package smoma.controller.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "mission_form_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionFormDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String department;

    @Column(columnDefinition = "TEXT")
    private String equipmentRequired;

    @Column(columnDefinition = "TEXT")
    private String safetyNotes;

    @Column(columnDefinition = "TEXT")
    private String missionObjectives;

    // Logistical details requested by the integration lifecycle test:
    private String originCity;
    private String targetCities;
    private String transitRoutes;
    private String costCodes;
    private BigDecimal perDiemAmount;
    private BigDecimal totalAllowance;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalComputedActiveDays;

    @OneToOne
    @JoinColumn(name = "mission_request_id", referencedColumnName = "id")
    private MissionRequest missionRequest;
}