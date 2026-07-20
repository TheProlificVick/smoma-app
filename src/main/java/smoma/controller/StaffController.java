package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @GetMapping
    public ResponseEntity<List<StaffMember>> getAllStaff() {
        return ResponseEntity.ok(staffMemberRepository.findAll());
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<?> updateStaffRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newRole = payload.get("role");
        Optional<StaffMember> staffOpt = staffMemberRepository.findById(id);
        
        if (staffOpt.isPresent()) {
            StaffMember staff = staffOpt.get();
            // Assuming your StaffMember model has a standard setter for role or roleScope:
            staff.setRoleScope(newRole);
            staffMemberRepository.save(staff);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}