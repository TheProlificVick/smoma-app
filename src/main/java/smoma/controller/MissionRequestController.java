package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.Service.MissionOrderService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mission-requests")
public class MissionRequestController {

    @Autowired
    private MissionOrderService missionOrderService;

    @PostMapping("/create")
    public ResponseEntity<?> createRequest(@RequestBody Map<String, Object> payload, Principal principal) {
        try {
            Long targetStaffId = Long.parseLong(payload.get("targetStaffId").toString());
            String title = (String) payload.get("title");
            String objective = (String) payload.get("objective");
            String destination = (String) payload.get("destination");

            // Initiator ID passed from session or requested payload fallback
            Long initiatorId = payload.containsKey("initiatorId") 
                    ? Long.parseLong(payload.get("initiatorId").toString()) 
                    : 1L;

            MissionRequest request = missionOrderService.initiateRequest(
                    initiatorId, targetStaffId, title, objective, destination
            );
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<MissionRequest>> getMyRequests(Principal principal) {
        // Returns all mission requests for listing and status tracking
        return ResponseEntity.ok(missionOrderService.getAllRequests());
    }
}