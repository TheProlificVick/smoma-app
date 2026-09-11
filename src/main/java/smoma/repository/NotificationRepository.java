package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientMatriculeOrderByDateCreationDesc(String recipientMatricule);

    List<Notification> findByRecipientUsernameOrderByDateCreationDesc(String recipientUsername);

    long countByRecipientMatriculeAndLuFalse(String recipientMatricule);

    long countByRecipientUsernameAndLuFalse(String recipientUsername);
}
