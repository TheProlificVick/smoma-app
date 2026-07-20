package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.MissionWorkflowService;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "*")
public class MissionWorkflowController {

    @Autowired
    private MissionWorkflowService workflowService;

    @PostMapping("/transition")
    public ResponseEntity<?> transitionMission(@RequestBody Map<String, Object> payload) {
        Long missionId = Long.valueOf(payload.get("missionId").toString());
        String targetStateStr = payload.get("state").toString();

        // Route the transition requests to your specific service use-cases
        if ("APPROVED".equalsIgnoreCase(targetStateStr) || "GM_APPROVED".equalsIgnoreCase(targetStateStr)) {
            // Invokes UC-02 with approval and a system actor stamp
            workflowService.reviewByGM(missionId, true, "system.validator@art.cm");
        } 
        else if ("REJECTED".equalsIgnoreCase(targetStateStr)) {
            // Invokes UC-02 with rejection
            workflowService.reviewByGM(missionId, false, "system.validator@art.cm");
        } 
        else if ("ISSUED_ACTIVE".equalsIgnoreCase(targetStateStr) || "ISSUED".equalsIgnoreCase(targetStateStr)) {
            // Invokes complete issuance logic
            workflowService.issueMissionOrder(missionId);
        } 
        else {
            return ResponseEntity.badRequest().body("Unsupported state transition: " + targetStateStr);
        }

        return ResponseEntity.ok().build();
    }
}