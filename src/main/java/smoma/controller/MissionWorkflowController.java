package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionFormDetail;
import smoma.controller.model.StaffMember;
import smoma.controller.model.Service.MissionWorkflowService;

@RestController
@RequestMapping("/api/missions")
public class MissionWorkflowController {

    @Autowired
    private MissionWorkflowService workflowService;

    // Submit a mission form
    @PostMapping("/{id}/form")
    public ResponseEntity<?> submitMissionForm(
            @PathVariable Long id, 
            @RequestBody MissionFormDetail details,
            @RequestAttribute("currentUser") StaffMember currentUser) { // Retrieved from Interceptor/Auth Session
        try {
            MissionFormDetail saved = workflowService.saveFormDetails(id, details, currentUser);
            return ResponseEntity.ok(saved);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // Approve a mission
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveMission(
            @PathVariable Long id,
            @RequestAttribute("currentUser") StaffMember currentUser) {
        try {
            workflowService.approveMission(id, currentUser);
            return ResponseEntity.ok("Mission approved successfully.");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}