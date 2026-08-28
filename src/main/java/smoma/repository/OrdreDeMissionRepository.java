package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.OrdreDeMission;

public interface OrdreDeMissionRepository extends JpaRepository<OrdreDeMission, Long> {
}
