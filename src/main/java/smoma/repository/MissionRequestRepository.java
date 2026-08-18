package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.MissionState;
import java.util.List;

public interface MissionRequestRepository extends JpaRepository<MissionRequest, Long> {
    List<MissionRequest> findByState(MissionState state);
    List<MissionRequest> findByInitiatorId(Long initiatorId);
    List<MissionRequest> findByAssignedStaffId(Long assignedStaffId);
}