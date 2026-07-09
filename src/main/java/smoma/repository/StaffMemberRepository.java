package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import smoma.controller.model.StaffMember;

import java.util.Optional;
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    Optional<StaffMember> findByEmail(String email);
}
