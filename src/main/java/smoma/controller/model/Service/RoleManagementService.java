package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

import java.util.Optional;

@Service
public class RoleManagementService {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    /**
     * Verifies if a staff member identified by email is authorized for a permission.
     */
    public boolean isAuthorized(String email, AppPermission permission) {
        Optional<StaffMember> staffOpt = staffMemberRepository.findByEmail(email);
        if (staffOpt.isEmpty()) {
            return false;
        }

        StaffMember staff = staffOpt.get();
        String roleStr = staff.getRoleScope();

        try {
            RoleScope roleScope = RoleScope.valueOf(roleStr.toUpperCase());
            return roleScope.getPermissions().contains(permission);
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }
}