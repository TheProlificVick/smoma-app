package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MissionOrder;
import java.util.Optional;

public interface MissionOrderRepository extends JpaRepository<MissionOrder, Long> {
    Optional<MissionOrder> findByMissionRequestId(Long requestId);
    Optional<MissionOrder> findByOrderNumber(String orderNumber);
}