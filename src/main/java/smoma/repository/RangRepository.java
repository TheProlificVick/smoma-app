package smoma.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Rang;

public interface RangRepository extends JpaRepository<Rang, Long> {

    List<Rang> findByActifTrue();

    List<Rang> findByLibelleContainingIgnoreCase(String libelle);
}
