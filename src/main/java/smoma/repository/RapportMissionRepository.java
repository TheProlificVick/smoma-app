package smoma.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.RapportMission;

public interface RapportMissionRepository extends JpaRepository<RapportMission, Long> {

    Optional<RapportMission> findByOrdreDeMission(OrdreDeMission ordreDeMission);

    List<RapportMission> findByOrdreDeMissionIn(List<OrdreDeMission> ordresDeMission);
}
