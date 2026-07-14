package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionOrder;
import smoma.repository.MissionOrderRepository;
import smoma.controller.model.Service.AppPermission;
import smoma.controller.model.Service.RoleManagementService;
import smoma.controller.model.Service.MissionWorkflowService;

import java.util.List;

@RestController
@RequestMapping("/api/mission-orders")
public class MissionOrderController {

    @Autowired
    private MissionOrderRepository orderRepository;

    @Autowired
    private MissionWorkflowService workflowService;

    @Autowired
    private RoleManagementService roleService;

    @GetMapping
    public ResponseEntity<List<MissionOrder>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<String> transitionWorkflow(
            @PathVariable Long id, 
            @RequestParam String action,
            @RequestParam String actorEmail) { 
        
        // 1. Validate permissions against our AppPermission declarations
        AppPermission requiredPermission;
        if ("APPROVE".equalsIgnoreCase(action)) {
            requiredPermission = AppPermission.APPROVE_MISSION;
        } else if ("REJECT".equalsIgnoreCase(action)) {
            requiredPermission = AppPermission.CREATE_MISSION; // Fallback mapping
        } else {
            return ResponseEntity.badRequest().body("Unknown workflow action: " + action);
        }

        if (!roleService.isAuthorized(actorEmail, requiredPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: " + actorEmail + " does not have permission to " + action);
        }

        try {
            boolean isApproved = "APPROVE".equalsIgnoreCase(action);
            workflowService.reviewByGM(id, isApproved, actorEmail);
            return ResponseEntity.ok("Mission workflow successfully updated by " + actorEmail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing workflow: " + e.getMessage());
        }
    }
}