package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionFormDetail;
import smoma.controller.model.MissionOrder;
import smoma.controller.model.Service.AppPermission;
import smoma.controller.model.Service.NotificationService;
import smoma.controller.model.Service.RoleManagementService;
import smoma.repository.MissionFormDetailRepository;
import smoma.repository.MissionOrderRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/mission-orders")
public class MissionOrderController {

    @Autowired
    private MissionOrderRepository missionOrderRepository;

    @Autowired
    private MissionFormDetailRepository missionFormDetailRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RoleManagementService roleManagementService;

    @PostMapping("/{id}/complete-form")
    public ResponseEntity<?> completeMissionForm(
            @PathVariable Long id,
            @RequestBody MissionFormDetail formDetail,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {

        if (userEmail == null || userEmail.isBlank()
                || !roleManagementService.isAuthorized(userEmail, AppPermission.APPROVE_MISSION)) {
            return ResponseEntity.status(403).body(Map.of(
                    "message", "You are not authorized to complete mission forms."
            ));
        }

        
        MissionOrder missionOrder = missionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission Order not found with id: " + id));

       
        missionFormDetailRepository.save(formDetail);

        String destination = (formDetail.getTargetCities() != null && !formDetail.getTargetCities().isEmpty())
                ? formDetail.getTargetCities()
                : "the designated location";

        String message = "You have been assigned a new mission to " + destination + ".";

       
        notificationService.sendNotification(
                missionOrder.getId(),
                "NEW_MISSION_ASSIGNMENT",
                message
        );

        return ResponseEntity.ok().body("Mission form populated and staff notified successfully.");
    }
}