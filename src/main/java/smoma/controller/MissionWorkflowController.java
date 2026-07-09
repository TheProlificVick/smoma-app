package smoma.controller;



import smoma.controller.model.MissionFormDetail;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.Service.MissionWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions")
public class MissionWorkflowController {

    private final MissionWorkflowService workflowService;

    public MissionWorkflowController(MissionWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/initiate") // UC-01 [cite: 41]
    public ResponseEntity<MissionRequest> initiateMission(@RequestBody MissionRequest request) {
        return ResponseEntity.ok(workflowService.initiateRequest(request));
    }

    @PutMapping("/{id}/review") // UC-02 [cite: 43]
    public ResponseEntity<MissionRequest> reviewMission(
            @PathVariable Long id,
            @RequestParam boolean isApproved,
            @RequestParam String signatureStamp) {
        return ResponseEntity.ok(workflowService.reviewByGM(id, isApproved, signatureStamp));
    }

    @PostMapping("/{id}/hr-details") // UC-03 [cite: 45]
    public ResponseEntity<MissionFormDetail> allocateFormLogistics(
            @PathVariable Long id,
            @RequestBody MissionFormDetail formDetail) {
        return ResponseEntity.ok(workflowService.populateHRDetails(id, formDetail));
    }

    @PutMapping("/{id}/issue")
    public ResponseEntity<MissionRequest> completeIssuance(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.issueMissionOrder(id));
    }
}
