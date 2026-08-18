package smoma.controller.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "mission_form_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionFormDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "mission_request_id")
    private MissionRequest missionRequest;

    private String origin;
    private String destination;
    private String transitRoutes;
    private Double allocatedBudget;
    private Double perDiem;
    private String modeOfTransport;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;

    private String pdfDocumentPath;
}