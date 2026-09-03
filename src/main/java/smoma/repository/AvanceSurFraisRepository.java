package smoma.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.AvanceSurFrais;
import smoma.controller.model.OrdreDeMission;

public interface AvanceSurFraisRepository extends JpaRepository<AvanceSurFrais, Long> {

    Optional<AvanceSurFrais> findByOrdreDeMission(OrdreDeMission ordreDeMission);

    List<AvanceSurFrais> findByOrdreDeMissionIn(List<OrdreDeMission> ordresDeMission);
}
