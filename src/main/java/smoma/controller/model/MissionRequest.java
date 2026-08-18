package smoma.controller.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private StaffMember initiator;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private StaffMember assignedStaff;

    private String title;

    @Column(length = 2000)
    private String purpose;

    @Column(length = 2000)
    private String justification;

    @Enumerated(EnumType.STRING)
    private MissionState state;

    private LocalDateTime createdAt;
    private LocalDateTime gmDecisionAt;
    private String gmComment;
}