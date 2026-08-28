package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MandatDeMission;

public interface MandatDeMissionRepository extends JpaRepository<MandatDeMission, Long> {
}
