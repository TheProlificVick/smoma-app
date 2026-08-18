package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MissionFormDetail;
import java.util.Optional;

public interface MissionFormDetailRepository extends JpaRepository<MissionFormDetail, Long> {
    Optional<MissionFormDetail> findByMissionRequestId(Long requestId);
}