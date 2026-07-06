package smoma.repository;

import smoma.model.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    // Custom query method we will use later for Active Directory synchronization checks
    Optional<StaffMember> findByEmail(String email);
}
