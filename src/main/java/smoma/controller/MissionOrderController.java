package smoma.controller;

import smoma.model.MissionOrder;
import smoma.repository.MissionOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "*") // Allows your index.html file to safely talk to this backend
public class MissionOrderController {

    @Autowired
    private MissionOrderRepository missionOrderRepository;

    
    @GetMapping
    public List<MissionOrder> getAllMissions() {
        return missionOrderRepository.findAll();
    }
    
    @PostMapping
    public MissionOrder createMissionOrder(@RequestBody MissionOrder newMission) {
        return missionOrderRepository.save(newMission);
    }
}
