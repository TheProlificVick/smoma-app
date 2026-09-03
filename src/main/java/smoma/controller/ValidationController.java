package smoma.controller;

 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.Service.MissionOrderService;

import java.util.Map;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {

    private final MissionOrderService missionOrderService;

    public ValidationController(MissionOrderService missionOrderService) {
        this.missionOrderService = missionOrderService;
    }

    @PostMapping("/gm-review")
    public ResponseEntity<?> gmReview(@RequestBody Map<String, Object> payload) {
        try {
            Long requestId = Long.parseLong(payload.get("requestId").toString());
            boolean approved = Boolean.parseBoolean(payload.get("approved").toString());
            String comments = (String) payload.get("comments");
            String reviewerUsername = (String) payload.getOrDefault("reviewerUsername", "gm_user");

            MissionRequest updatedRequest = missionOrderService.reviewByGM(requestId, approved, comments, reviewerUsername);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
