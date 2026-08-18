package smoma.controller.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @OneToOne
    @JoinColumn(name = "mission_request_id")
    private MissionRequest missionRequest;

    @Enumerated(EnumType.STRING)
    private MissionOrderStatus status;

    private Boolean arrivalStamped = false;
    private Boolean completionStamped = false;

    private LocalDateTime issuedAt;
}