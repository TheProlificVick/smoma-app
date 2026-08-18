package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.*;
import smoma.controller.model.Service.MissionWorkflowService;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class MissionWorkflowController {

    @Autowired
    private MissionWorkflowService workflowService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestBody Map<String, Object> payload) {
        Long initiatorId = Long.valueOf(payload.get("initiatorId").toString());
        Long staffId = Long.valueOf(payload.get("assignedStaffId").toString());
        String title = payload.get("title").toString();
        String purpose = payload.get("purpose").toString();
        String justification = payload.get("justification").toString();

        MissionRequest request = workflowService.initiateRequest(initiatorId, staffId, title, purpose, justification);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/gm-review")
    public ResponseEntity<?> gmReview(@RequestBody Map<String, Object> payload) {
        Long requestId = Long.valueOf(payload.get("requestId").toString());
        boolean approve = Boolean.parseBoolean(payload.get("approve").toString());
        String comment = payload.getOrDefault("comment", "").toString();
        String username = payload.get("username").toString();

        MissionRequest request = workflowService.reviewByGM(requestId, approve, comment, username);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/hr-complete")
    public ResponseEntity<?> hrComplete(@RequestBody Map<String, Object> payload, @RequestParam String hrUsername) {
        Long requestId = Long.valueOf(payload.get("requestId").toString());
        
        MissionFormDetail details = MissionFormDetail.builder()
                .origin(payload.get("origin").toString())
                .destination(payload.get("destination").toString())
                .transitRoutes(payload.get("transitRoutes").toString())
                .allocatedBudget(Double.valueOf(payload.get("allocatedBudget").toString()))
                .perDiem(Double.valueOf(payload.get("perDiem").toString()))
                .modeOfTransport(payload.get("modeOfTransport").toString())
                .startDate(java.time.LocalDate.parse(payload.get("startDate").toString()))
                .endDate(java.time.LocalDate.parse(payload.get("endDate").toString()))
                .build();

        MissionFormDetail saved = workflowService.completeHrForm(requestId, details, hrUsername);
        return ResponseEntity.ok(saved);
    }
}