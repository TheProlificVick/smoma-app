package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.*;

import java.time.LocalDateTime;

@Service
@Transactional
public class MissionWorkflowService {

    @Autowired
    private MissionRequestRepository requestRepository;

    @Autowired
    private MissionFormDetailRepository formRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // UC-01: Initiate Request
    public MissionRequest initiateRequest(MissionRequest request) {
        request.setState(MissionState.INITIATED);
        MissionRequest savedRequest = requestRepository.save(request);
        writeAuditLog(savedRequest.getId(), "INITIATE", "Mission initiated.");
        return savedRequest;
    }

    // UC-02: GM Executive Review
    public MissionRequest reviewByGM(Long requestId, boolean approved, String actorEmail) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        
        if (approved) {
            request.setState(MissionState.GM_APPROVED);
            request.setGmApprovalStamp(actorEmail);
            writeAuditLog(requestId, "APPROVE", "Approved by GM: " + actorEmail);
        } else {
            request.setState(MissionState.REJECTED);
            writeAuditLog(requestId, "REJECT", "Rejected by GM: " + actorEmail);
        }
        return requestRepository.save(request);
    }

    // UC-03: HR Form Completion
    public MissionFormDetail populateHRDetails(Long requestId, MissionFormDetail formDetails) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        formDetails.setMissionRequest(request);
        MissionFormDetail savedDetails = formRepository.save(formDetails);

        request.setState(MissionState.FORM_COMPLETED);
        requestRepository.save(request);

        writeAuditLog(requestId, "POPULATE_FORM", "Logistics form completed by HR.");
        return savedDetails;
    }

    // Complete Issuance
    public MissionRequest issueMissionOrder(Long requestId) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        request.setState(MissionState.ISSUED_ACTIVE);
        MissionRequest savedRequest = requestRepository.save(request);

        writeAuditLog(requestId, "ISSUE_DOCUMENT", "Mission order issued and active.");
        return savedRequest;
    }

    // Process and link a form detail with simple authorization checks
    public MissionFormDetail saveFormDetails(Long requestId, MissionFormDetail formDetails, StaffMember actor) {
        // Enforce matching enum check
        if (!"STAFF".equalsIgnoreCase(actor.getRoleScope()) && !"ADMIN".equalsIgnoreCase(actor.getRoleScope())) {
            throw new SecurityException("Unauthorized: Only staff members can fill out mission forms.");
        }
        return populateHRDetails(requestId, formDetails);
    }

    // Role-based action to transition workflow states
    public void approveMission(Long requestId, StaffMember actor) {
        if (!"SUPERVISOR".equalsIgnoreCase(actor.getRoleScope()) && !"ADMIN".equalsIgnoreCase(actor.getRoleScope())) {
            throw new SecurityException("Unauthorized: Only Supervisors or Admins can approve missions.");
        }
        reviewByGM(requestId, true, actor.getEmail());
    }

    private void writeAuditLog(Long requestId, String action, String description) {
        AuditLog log = new AuditLog();
        log.setMissionRequestId(requestId);
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}