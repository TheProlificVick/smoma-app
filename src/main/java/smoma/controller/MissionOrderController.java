package smoma.controller;

import smoma.controller.model.MissionOrder;
import smoma.repository.MissionOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "*") 
public class MissionOrderController {

    @Autowired
    private MissionOrderRepository MissionOrderRepository;

    
    @GetMapping
    public List<MissionOrder> getAllMissions() {
        return MissionOrderRepository.findAll();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<MissionOrder> getMissionById(@PathVariable Long id) {
        Optional<MissionOrder> mission = MissionOrderRepository.findById(id);
        return mission.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    @PostMapping
    public ResponseEntity<MissionOrder> createMission(@RequestBody MissionOrder mission) {
        if (mission.getStatus() == null || mission.getStatus().isBlank()) {
            mission.setStatus("PENDING");
        }
        MissionOrder saved = MissionOrderRepository.save(mission);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<MissionOrder> updateMission(@PathVariable Long id, @RequestBody MissionOrder updated) {
        return MissionOrderRepository.findById(id).map(existing -> {
            existing.setMissionCode(updated.getMissionCode());
            existing.setDestination(updated.getDestination());
            existing.setDuration(updated.getDuration());
            existing.setStaffMember(updated.getStaffMember());
            existing.setStatus(updated.getStatus());
            MissionOrder saved = MissionOrderRepository.save(existing);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        if (!MissionOrderRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        MissionOrderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}