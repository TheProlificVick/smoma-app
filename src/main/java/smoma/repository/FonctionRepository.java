package smoma.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Fonction;

public interface FonctionRepository extends JpaRepository<Fonction, Long> {

    List<Fonction> findByActifTrue();

    List<Fonction> findByLibelleContainingIgnoreCase(String libelle);
}
