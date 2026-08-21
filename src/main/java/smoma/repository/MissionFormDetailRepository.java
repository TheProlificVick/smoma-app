package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smoma.controller.model.MissionFormDetail;

@Repository
public interface MissionFormDetailRepository extends JpaRepository<MissionFormDetail, Long> {
}