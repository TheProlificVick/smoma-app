package smoma.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smoma.controller.model.EtapeMission;

public interface EtapeMissionRepository extends JpaRepository<EtapeMission, Long> {

    List<EtapeMission> findByMandatDeMissionId(Long mandatId);

    /** Steps whose end date has passed and for which the completion notification is not yet sent. */
    List<EtapeMission> findByDateFinLessThanEqualAndNotificationFinEnvoyeeFalse(LocalDate date);

    @Query("SELECT e FROM EtapeMission e WHERE " +
           "(LOWER(e.lieu) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.commentaire) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<EtapeMission> searchByKeyword(@Param("keyword") String keyword);
}
