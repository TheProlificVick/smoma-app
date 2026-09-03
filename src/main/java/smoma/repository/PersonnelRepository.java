package smoma.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import smoma.controller.model.Personnel;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    Optional<Personnel> findByMatricule(String matricule);

    List<Personnel> findByNomContainingIgnoreCase(String nom);

    List<Personnel> findByDepartement(String departement);

    List<Personnel> findByGrade(Personnel.Grade grade);

    List<Personnel> findByFonction(String fonction);

    List<Personnel> findByStatut(Personnel.Statut statut);

    @Query("SELECT p FROM Personnel p WHERE " +
           "(:matricule IS NULL OR LOWER(p.matricule) LIKE LOWER(CONCAT('%', :matricule, '%'))) AND " +
           "(:nom IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
           "(:departement IS NULL OR LOWER(p.departement) LIKE LOWER(CONCAT('%', :departement, '%'))) AND " +
           "(:fonction IS NULL OR LOWER(p.fonction) LIKE LOWER(CONCAT('%', :fonction, '%'))) AND " +
           "(:grade IS NULL OR p.grade = :grade) AND " +
           "(:statut IS NULL OR p.statut = :statut)")
    List<Personnel> search(
            @Param("matricule") String matricule,
            @Param("nom") String nom,
            @Param("departement") String departement,
            @Param("fonction") String fonction,
            @Param("grade") Personnel.Grade grade,
            @Param("statut") Personnel.Statut statut);

    default List<Personnel> findActivePersonnel() {
        return findByStatut(Personnel.Statut.ACTIF);
    }
}
