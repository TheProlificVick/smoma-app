package smoma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Service.AssistantIaService;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant-ia")
public class AssistantIaController {

    private final AssistantIaService assistantIaService;

    public AssistantIaController(AssistantIaService assistantIaService) {
        this.assistantIaService = assistantIaService;
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> processQuery(@RequestBody Map<String, String> payload) {
        String prompt = payload != null ? payload.get("prompt") : "";
        return ResponseEntity.ok(assistantIaService.processQuery(prompt));
    }
}
