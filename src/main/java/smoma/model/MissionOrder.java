package smoma.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long missionId;

    @Column(name = "mission_code", unique = true, nullable = false)
    private String missionCode;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "current_state", nullable = false)
    private String currentState;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffMember staffMember;
}