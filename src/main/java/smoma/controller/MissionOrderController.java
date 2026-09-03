package smoma.controller;

 
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import smoma.dto.HRFormDTO;
import smoma.dto.MissionRequestDTO;
import smoma.controller.model.MissionOrder;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.User;
import smoma.repository.UserRepository;
import smoma.controller.model.Service.MissionOrderService;
import smoma.controller.model.Service.PdfGeneratorService;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
public class MissionOrderController {

    private final MissionOrderService missionOrderService;

    private final PdfGeneratorService pdfGeneratorService;

    private final UserRepository userRepository;

    public MissionOrderController(MissionOrderService missionOrderService,
                                  PdfGeneratorService pdfGeneratorService,
                                  UserRepository userRepository) {
        this.missionOrderService = missionOrderService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.userRepository = userRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<MissionRequest> createRequest(@RequestBody MissionRequestDTO dto, Principal principal) {
        String username = principal != null ? principal.getName() : "dept_rep";
        User initiator = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return ResponseEntity.ok(missionOrderService.initiateRequest(dto, initiator));
    }

    @PutMapping("/request/{id}/gm-review")
    public ResponseEntity<MissionRequest> gmReview(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestParam(required = false, defaultValue = "") String comment,
            Principal principal) {
        String username = principal != null ? principal.getName() : "general_manager";
        return ResponseEntity.ok(missionOrderService.reviewByGM(id, approve, comment, username));
    }

    @PostMapping("/hr/complete-form")
    public ResponseEntity<MissionOrder> completeHRForm(@RequestBody HRFormDTO dto, Principal principal) {
        String username = principal != null ? principal.getName() : "hr_officer";
        return ResponseEntity.ok(missionOrderService.completeHRForm(dto, username));
    }

    @GetMapping
    public ResponseEntity<List<MissionRequest>> getAllRequests() {
        return ResponseEntity.ok(missionOrderService.getAllRequests());
    }

    @PutMapping("/request/{id}/payment")
    public ResponseEntity<?> updateMissionPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            Principal principal) {
        try {
            String paymentStage = String.valueOf(payload.getOrDefault("paymentStage", "PENDING"));
            String paymentAmount = String.valueOf(payload.getOrDefault("paymentAmount", "0"));
            String paymentCurrency = String.valueOf(payload.getOrDefault("paymentCurrency", "XAF"));
            String paymentReference = String.valueOf(payload.getOrDefault("paymentReference", ""));
            String reportStatus = String.valueOf(payload.getOrDefault("reportStatus", "NOT_SUBMITTED"));
            String reportScanUrl = payload.get("reportScanUrl") != null ? payload.get("reportScanUrl").toString() : "";

            MissionRequest updated = missionOrderService.updateMissionPayment(
                    id,
                    paymentStage,
                    paymentAmount,
                    paymentCurrency,
                    paymentReference,
                    reportStatus,
                    reportScanUrl
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/order/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Long id) {
        MissionOrder order = missionOrderService.getOrderById(id);
        ByteArrayInputStream pdfStream = pdfGeneratorService.generateMissionOrderPdf(order);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=" + order.getOrderNumber() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}