package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientMatriculeIgnoreCaseOrderByDateCreationDesc(String recipientMatricule);

    List<Notification> findByRecipientUsernameIgnoreCaseOrderByDateCreationDesc(String recipientUsername);
}
