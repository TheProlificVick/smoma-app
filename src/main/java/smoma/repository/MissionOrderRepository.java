package smoma.repository;

import smoma.model.MissionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionOrderRepository extends JpaRepository<MissionOrder, Long> {
    // Inherits findAll(), save(), deleteById(), etc. automatically!
}
