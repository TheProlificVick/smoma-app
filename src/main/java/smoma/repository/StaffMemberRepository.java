package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.StaffMember;
import smoma.controller.model.Service.RoleScope;
import java.util.Optional;
import java.util.List;

public interface StaffMemberRepository extends JpaRepository<StaffMember, Long> {
    Optional<StaffMember> findByUsername(String username);
    List<StaffMember> findByDepartment(String department);
    List<StaffMember> findByRole(RoleScope role);
}