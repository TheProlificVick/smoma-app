package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smoma.controller.model.MissionRequest;

@Repository
public interface MissionRequestRepository extends JpaRepository<MissionRequest, Long> {
}