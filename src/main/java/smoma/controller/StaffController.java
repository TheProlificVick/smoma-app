package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.RoleScope;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;
import smoma.controller.model.Service.RoleManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffMemberRepository staffRepository;

    @Autowired
    private RoleManagementService roleManagementService;

    @GetMapping
    public ResponseEntity<List<StaffMember>> getAllStaff() {
        return ResponseEntity.ok(staffRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffMember> getStaffById(@PathVariable Long id) {
        return staffRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<StaffMember> updateRole(@PathVariable Long id, @RequestParam RoleScope role) {
        StaffMember updated = roleManagementService.updateUserRole(id, role);
        return ResponseEntity.ok(updated);
    }
}