package smoma.repository;

import smoma.controller.model.MissionFormDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionFormDetailRepository extends JpaRepository<MissionFormDetail, Long> {
    
}
