package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionFormDetail;
import smoma.controller.model.MissionOrder;
import smoma.repository.MissionFormDetailRepository;
import smoma.repository.MissionOrderRepository;
import smoma.controller.model.Service.NotificationService;

@RestController
@RequestMapping("/api/mission-orders")
public class MissionOrderController {

    @Autowired
    private MissionOrderRepository missionOrderRepository;

    @Autowired
    private MissionFormDetailRepository missionFormDetailRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/{id}/complete-form")
    public ResponseEntity<?> completeMissionForm(
            @PathVariable Long id,
            @RequestBody MissionFormDetail formDetail) {

        // 1. Fetch the existing Mission Order / Request
        MissionOrder missionOrder = missionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission Order not found with id: " + id));

        // 2. Save Form Details directly to repository
        missionFormDetailRepository.save(formDetail);

        // 3. Build notification message using actual schema fields (target_cities)
        String destination = (formDetail.getTargetCities() != null && !formDetail.getTargetCities().isEmpty()) 
                ? formDetail.getTargetCities() 
                : "the designated location";

        String message = "You have been assigned a new mission to " + destination + ".";

        // 4. Send Notification
        notificationService.sendNotification(
                missionOrder.getId(), 
                "NEW_MISSION_ASSIGNMENT", 
                message
        );

        return ResponseEntity.ok().body("Mission form populated and staff notified successfully.");
    }
}