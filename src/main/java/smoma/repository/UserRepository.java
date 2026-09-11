package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByMatricule(String matricule);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:ident) OR LOWER(u.email) = LOWER(:ident) OR (u.matricule IS NOT NULL AND LOWER(u.matricule) = LOWER(:ident))")
    Optional<User> findByIdentity(@Param("ident") String ident);
}