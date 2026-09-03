package smoma.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.BaremeIndemnite;
import smoma.controller.model.OrdreDeMission.TypeMission;

public interface BaremeIndemniteRepository extends JpaRepository<BaremeIndemnite, Long> {

    List<BaremeIndemnite> findByGradeAndRangAndFonctionAndTypeMissionAndEstActifTrue(
        String grade, String rang, String fonction, TypeMission typeMission);

    List<BaremeIndemnite> findByTypeMissionAndEstActifTrue(TypeMission typeMission);

    List<BaremeIndemnite> findByEstActifTrue();
}
