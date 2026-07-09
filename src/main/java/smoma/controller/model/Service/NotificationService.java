package smoma.controller.model.Service;

import smoma.controller.model.MissionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    
    public void notifyStaff(Long staffMemberId, Long requestId, MissionState state) {
        String baseMessage = String.format("Notification to Staff ID %d: Your Mission Order Request #%d status has shifted to: %s.", 
                                            staffMemberId, requestId, state.name());
        
        
        switch (state) {
            case GM_APPROVED -> baseMessage += " Your request is now migrating to HR for logistical formatting.";
            case ISSUED_ACTIVE -> baseMessage += " The formal PDF mandate has been generated. You are legally cleared to download and execute the mission.";
            case REJECTED -> baseMessage += " The request was officially denied during executive structural review.";
            default -> {}
        }
        
        
        logger.info("[SMOMA ALERT ENGINE] -> {}", baseMessage);
    }
}
