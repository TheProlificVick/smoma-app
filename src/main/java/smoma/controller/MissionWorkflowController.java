package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.dto.MissionRequestDTO;
import smoma.controller.model.Service.MissionOrderService;

import java.security.Principal;

@RestController
@RequestMapping("/api/missions")
public class MissionWorkflowController {

    @Autowired
    private MissionOrderService missionOrderService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateRequest(@RequestBody MissionRequestDTO dto, Principal principal) {
        return ResponseEntity.ok(missionOrderService.initiateRequest(dto, principal.getName()));
    }
}