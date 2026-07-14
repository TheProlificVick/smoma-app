package smoma.repository;

import smoma.controller.model.MissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRequestRepository extends JpaRepository<MissionRequest, Long> {
}