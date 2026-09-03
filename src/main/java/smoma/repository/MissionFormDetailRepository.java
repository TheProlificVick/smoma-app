package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MissionFormDetail;

public interface MissionFormDetailRepository extends JpaRepository<MissionFormDetail, Long> {
}