package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Personnel;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {
}
