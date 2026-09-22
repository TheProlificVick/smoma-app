package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Service.Role;
import smoma.controller.model.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByMatricule(String matricule);
    boolean existsByRole(Role role);
    List<User> findByDepartment_Id(Long departmentId);

    /**
     * Optional-returning: throws NonUniqueResultException if more than one row matches. Unlike
     * {@code username} (unique at the DB level), {@code matricule} carries no such constraint —
     * AD auto-provisioning can write a colliding one — so every call site resolving a caller's
     * identity at login/authorization time must use {@link #findAllByIdentity} instead, which
     * tolerates duplicates the same way {@code PersonnelRepository.findAllByMatricule} does.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:ident) OR LOWER(u.email) = LOWER(:ident) OR (u.matricule IS NOT NULL AND LOWER(u.matricule) = LOWER(:ident))")
    Optional<User> findByIdentity(@Param("ident") String ident);

    /** Duplicate-tolerant lookup — see {@link #findByIdentity}. */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:ident) OR LOWER(u.email) = LOWER(:ident) OR (u.matricule IS NOT NULL AND LOWER(u.matricule) = LOWER(:ident))")
    List<User> findAllByIdentity(@Param("ident") String ident);
}