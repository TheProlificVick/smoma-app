package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MissionOrder;

public interface MissionOrderRepository extends JpaRepository<MissionOrder, Long> {
}