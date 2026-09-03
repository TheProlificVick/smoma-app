package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MissionRequest;

public interface MissionRequestRepository extends JpaRepository<MissionRequest, Long> {
}