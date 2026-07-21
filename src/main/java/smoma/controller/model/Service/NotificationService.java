package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    public void sendNotification(Long staffId, String type, String message) {
        // Business logic to persist notification log and send real-time alert/email
        System.out.println("Notification sent to Staff ID " + staffId + " [" + type + "]: " + message);
    }
}