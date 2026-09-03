package smoma.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.MotifReglementaire;

public interface MotifReglementaireRepository extends JpaRepository<MotifReglementaire, Long> {

    Optional<MotifReglementaire> findByCode(String code);

    List<MotifReglementaire> findByActifTrue();

    List<MotifReglementaire> findByLibelleContainingIgnoreCase(String libelle);
}
