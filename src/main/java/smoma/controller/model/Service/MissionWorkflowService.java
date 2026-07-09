package smoma.controller.model.Service;

import smoma.controller.model.MissionFormDetail;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.MissionState;
import smoma.repository.MissionFormDetailRepository;
import smoma.repository.MissionRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionWorkflowService {

    private final MissionRequestRepository requestRepository;
    private final MissionFormDetailRepository formDetailRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public MissionWorkflowService(MissionRequestRepository requestRepository,
                                  MissionFormDetailRepository formDetailRepository,
                                  AuditLogService auditLogService,
                                  NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.formDetailRepository = formDetailRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public MissionRequest initiateRequest(MissionRequest request) {
        request.setState(MissionState.INITIATED);
        MissionRequest savedRequest = requestRepository.save(request);

        auditLogService.logTransaction(savedRequest.getId(), "DEPARTMENT_REP", "INITIATE", 
                "Mission project initialized for department: " + request.getOriginatingDepartment());
        
        return savedRequest;
    }

    @Transactional
    public MissionRequest reviewByGM(Long requestId, boolean isApproved, String signatureStamp) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Mission request record not found."));

        if (request.getState() != MissionState.INITIATED) {
            throw new IllegalStateException("Operational Pipeline Error: Target dossier is not in an INITIATED state.");
        }

        if (isApproved) {
            request.setState(MissionState.GM_APPROVED);
            request.setGmApprovalStamp(signatureStamp);
            auditLogService.logTransaction(requestId, "GENERAL_MANAGER", "APPROVE", "Executive review passed. Forwarded to HR.");
        } else {
            request.setState(MissionState.REJECTED);
            auditLogService.logTransaction(requestId, "GENERAL_MANAGER", "REJECT", "Executive review denied. Workflow terminated.");
        }

        MissionRequest updatedRequest = requestRepository.save(request);
        notificationService.notifyStaff(updatedRequest.getStaffMemberId(), updatedRequest.getId(), updatedRequest.getState());
        
        return updatedRequest;
    }

    @Transactional
    public MissionFormDetail populateHRDetails(Long requestId, MissionFormDetail incomingForm) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Mission request record not found."));

        // Strict Enforcement: Exclusivity validation for HR data entry 
        if (request.getState() != MissionState.GM_APPROVED) {
            throw new IllegalStateException("Exclusivity Violation: Cannot populate logistics unless request is formally GM Approved.");
        }

        incomingForm.setMissionRequest(request);
        MissionFormDetail savedForm = formDetailRepository.save(incomingForm);

        request.setState(MissionState.FORM_COMPLETED); // Transition to Form Completed state 
        requestRepository.save(request);

        auditLogService.logTransaction(requestId, "HR_OFFICER", "POPULATE_FORM", 
                String.format("HR logistics configured. Duration days: %d. PerDiem: %s", 
                        incomingForm.getTotalComputedActiveDays(), incomingForm.getPerDiemAmount().toString()));

        notificationService.notifyStaff(request.getStaffMemberId(), request.getId(), request.getState());
        
        return savedForm;
    }

    @Transactional
    public MissionRequest issueMissionOrder(Long requestId) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Mission request record not found."));

        if (request.getState() != MissionState.FORM_COMPLETED) {
            throw new IllegalStateException("Pipeline Error: Cannot compile final document without verified HR logistics.");
        }

        request.setState(MissionState.ISSUED_ACTIVE);
        MissionRequest finalizedRequest = requestRepository.save(request);

        auditLogService.logTransaction(requestId, "SYSTEM", "ISSUE_DOCUMENT", "Final read-only PDF document generated and exposed for downstream download.");
        notificationService.notifyStaff(finalizedRequest.getStaffMemberId(), finalizedRequest.getId(), finalizedRequest.getState());

        return finalizedRequest;
    }
} 