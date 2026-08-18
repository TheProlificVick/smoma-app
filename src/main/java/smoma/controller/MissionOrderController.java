package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.MissionOrder;
import smoma.repository.MissionOrderRepository;

@RestController
@RequestMapping("/api/mission-orders")
public class MissionOrderController {

    @Autowired
    private MissionOrderRepository orderRepository;

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getOrderByRequestId(@PathVariable Long requestId) {
        return orderRepository.findByMissionRequestId(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}