

package smoma.repository;

import smoma.controller.model.MissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MissionRequestRepository extends JpaRepository<MissionRequest, Long> {
    
   
    List<MissionRequest> findByStaffMemberId(Long staffMemberId);
    
    
    List<MissionRequest> findByState(smoma.controller.model.MissionState state);
}
