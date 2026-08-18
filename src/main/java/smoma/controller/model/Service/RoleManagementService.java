package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smoma.controller.model.Service.RoleScope;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

@Service
public class RoleManagementService {

    @Autowired
    private StaffMemberRepository staffRepository;

    public StaffMember updateUserRole(Long staffId, RoleScope newRole) {
        StaffMember staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff member not found with ID: " + staffId));
        staff.setRole(newRole);
        return staffRepository.save(staff);
    }
}