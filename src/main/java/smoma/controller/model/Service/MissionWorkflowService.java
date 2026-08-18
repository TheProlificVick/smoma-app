package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MissionWorkflowService {

    @Autowired private MissionRequestRepository requestRepository;
    @Autowired private MissionFormDetailRepository formDetailRepository;
    @Autowired private MissionOrderRepository orderRepository;
    @Autowired private StaffMemberRepository staffRepository;
    @Autowired private AuditLogService auditLogService;

    @Transactional
    public MissionRequest initiateRequest(Long initiatorId, Long staffId, String title, String purpose, String justification) {
        StaffMember initiator = staffRepository.findById(initiatorId).orElseThrow();
        StaffMember staff = staffRepository.findById(staffId).orElseThrow();

        MissionRequest request = MissionRequest.builder()
                .initiator(initiator)
                .assignedStaff(staff)
                .title(title)
                .purpose(purpose)
                .justification(justification)
                .state(MissionState.INITIATED)
                .createdAt(LocalDateTime.now())
                .build();

        MissionRequest saved = requestRepository.save(request);
        auditLogService.logAction(initiator.getUsername(), "INITIATE_REQUEST", "MissionRequest", saved.getId(), "Initiated mission request: " + title);
        return saved;
    }

    @Transactional
    public MissionRequest reviewByGM(Long requestId, boolean approve, String comment, String gmUsername) {
        MissionRequest request = requestRepository.findById(requestId).orElseThrow();
        request.setGmDecisionAt(LocalDateTime.now());
        request.setGmComment(comment);
        request.setState(approve ? MissionState.GM_APPROVED : MissionState.REJECTED);

        MissionRequest saved = requestRepository.save(request);
        auditLogService.logAction(gmUsername, approve ? "GM_APPROVE" : "GM_REJECT", "MissionRequest", saved.getId(), "GM decision executed.");
        return saved;
    }

    @Transactional
    public MissionFormDetail completeHrForm(Long requestId, MissionFormDetail details, String hrUsername) {
        MissionRequest request = requestRepository.findById(requestId).orElseThrow();
        if (request.getState() != MissionState.GM_APPROVED) {
            throw new IllegalStateException("Only GM Approved requests can be populated by HR.");
        }

        long days = ChronoUnit.DAYS.between(details.getStartDate(), details.getEndDate()) + 1;
        details.setTotalDays((int) days);
        details.setMissionRequest(request);
        details.setPdfDocumentPath("/documents/MO_" + requestId + ".pdf");

        MissionFormDetail savedDetail = formDetailRepository.save(details);

        request.setState(MissionState.FORM_COMPLETED);
        requestRepository.save(request);

        // Auto-Generate Mission Order Document
        MissionOrder order = MissionOrder.builder()
                .orderNumber("ART-MO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .missionRequest(request)
                .status(MissionOrderStatus.PENDING)
                .issuedAt(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        request.setState(MissionState.ISSUED_ACTIVE);
        requestRepository.save(request);

        auditLogService.logAction(hrUsername, "HR_FORM_FILL", "MissionFormDetail", savedDetail.getId(), "HR completed parameters for Mission #" + requestId);
        return savedDetail;
    }
}