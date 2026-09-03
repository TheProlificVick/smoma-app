package smoma.controller;

 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionOrder;
import smoma.controller.model.Service.MissionOrderService;
import smoma.dto.HRFormDTO;

import java.util.Map;

@RestController
@RequestMapping("/api/hr")
public class HrController {

    private final MissionOrderService missionOrderService;

    public HrController(MissionOrderService missionOrderService) {
        this.missionOrderService = missionOrderService;
    }

    @PostMapping("/complete-order")
    public ResponseEntity<?> completeHRForm(@RequestBody HRFormDTO hrFormDTO, @RequestParam(defaultValue = "hr_officer") String hrUsername) {
        try {
            MissionOrder order = missionOrderService.completeHRForm(hrFormDTO, hrUsername);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
