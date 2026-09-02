package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smoma.dto.HRFormDTO;
import smoma.dto.MissionRequestDTO;
import smoma.controller.model.*;
import smoma.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
public class MissionOrderService {

    @Autowired
    private MissionRequestRepository requestRepository;

    @Autowired
    private MissionFormDetailRepository formDetailRepository;

    @Autowired
    private MissionOrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public boolean canCreateMissionRequest(Role role) {
        return role == Role.ROLE_DEPARTMENT_REPRESENTATIVE || role == Role.ROLE_GENERAL_MANAGER || role == Role.ROLE_ADMIN;
    }

    public boolean canCompleteHrForm(Role role) {
        return role == Role.ROLE_HR_OFFICER || role == Role.ROLE_ADMIN;
    }

    public boolean canReviewMission(Role role) {
        return role == Role.ROLE_GENERAL_MANAGER || role == Role.ROLE_ADMIN;
    }

    @Transactional
    public MissionRequest initiateRequest(Long initiatorId, Long targetStaffId, String title, String justification, String destination) {
        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found: " + initiatorId));
        User targetStaff = userRepository.findById(targetStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Target staff not found: " + targetStaffId));

        if (!canCreateMissionRequest(initiator.getRole())) {
            throw new IllegalStateException("Seuls le directeur de département, le général manager ou l'administrateur peuvent initier une demande de mission.");
        }

        MissionRequest request = new MissionRequest();
        request.setTitle(title);
        request.setJustification(justification);
        request.setDestination(destination);
        request.setStatus(MissionRequest.MissionStatus.INITIATED);
        request.setCurrentStage("INITIATED");
        request.setPaymentStatus("PENDING");
        request.setMandateApproved(false);
        request.setMandateSignedByGeneralManager(false);
        request.setInitiator(initiator);
        request.setAssignedStaff(targetStaff);

        MissionRequest saved = requestRepository.save(request);
        logAudit("INITIATE_REQUEST", initiator.getUsername(), "Created request ID: " + saved.getId());
        return saved;
    }

    @Transactional
    public MissionRequest initiateRequest(MissionRequestDTO dto, User initiator) {
        return initiateRequest(
                initiator.getId(),
                dto.getAssignedStaffId(),
                dto.getTitle(),
                dto.getJustification(),
                dto.getDestination()
        );
    }

    @Transactional
    public MissionRequest reviewByGM(Long requestId, boolean approve, String comment, String gmUsername) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        User reviewUser = userRepository.findByUsername(gmUsername).orElse(null);
        if (reviewUser != null && !canReviewMission(reviewUser.getRole())) {
            throw new IllegalStateException("Seul le général manager ou l'administrateur peut approuver ou rejeter une mission.");
        }

        if (approve) {
            request.setStatus(MissionRequest.MissionStatus.GM_APPROVED);
            request.setCurrentStage("GM_APPROVED");
            request.setMandateApproved(true);
            request.setMandateSignedByGeneralManager(true);
            request.setMandateReference("MANDAT-ART-" + request.getId());
        } else {
            request.setStatus(MissionRequest.MissionStatus.REJECTED);
            request.setCurrentStage("REJECTED");
            request.setMandateApproved(false);
            request.setMandateSignedByGeneralManager(false);
            request.setPaymentStatus("CANCELLED");
        }
        MissionRequest updated = requestRepository.save(request);

        logAudit("GM_REVIEW", gmUsername, "Request ID " + requestId + " marked as " + request.getStatus());
        return updated;
    }

    @Transactional
    public MissionOrder completeHRForm(HRFormDTO dto, String hrUsername) {
        MissionRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + dto.getRequestId()));

        User hrUser = userRepository.findByUsername(hrUsername).orElse(null);
        if (hrUser != null && !canCompleteHrForm(hrUser.getRole())) {
            throw new IllegalStateException("Seuls le service RH ou l'administrateur peuvent compléter le formulaire de mission.");
        }

        if (request.getStatus() != MissionRequest.MissionStatus.GM_APPROVED
                || !request.isMandateApproved()
                || !request.isMandateSignedByGeneralManager()) {
            throw new IllegalStateException("A mission cannot be issued without an approved and signed general manager mandate.");
        }

        MissionFormDetail formDetail = new MissionFormDetail();
        formDetail.setMissionRequest(request);
        formDetail.setItinerary(dto.getItinerary());
        formDetail.setDurationDays(dto.getDurationDays());
        formDetail.setAllocatedBudget(dto.getAllocatedBudget());
        formDetail.setTransportMode(dto.getTransportMode());
        formDetailRepository.save(formDetail);

        MissionOrder order = new MissionOrder();
        order.setMissionRequest(request);
        order.setFormDetail(formDetail);
        order.setOrderNumber("OM-ART-" + System.currentTimeMillis());
        order.setIssueDate(LocalDate.now());
        order.setStatus(MissionRequest.MissionStatus.ISSUED);

        request.setStatus(MissionRequest.MissionStatus.FORM_COMPLETED);
        request.setCurrentStage("HR_FORM_COMPLETED");
        request.setPaymentStatus("PENDING");
        requestRepository.save(request);

        MissionOrder savedOrder = orderRepository.save(order);
        logAudit("HR_FORM_COMPLETE", hrUsername, "Issued Order ID: " + savedOrder.getId());
        return savedOrder;
    }

    @Transactional
    public MissionRequest updateMissionPayment(Long requestId, String paymentStage, String paymentAmount,
                                              String paymentCurrency, String paymentReference,
                                              String reportStatus, String reportScanUrl) {
        MissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        request.setPaymentStage(paymentStage);
        request.setPaymentAmount(paymentAmount);
        request.setPaymentCurrency(paymentCurrency);
        request.setPaymentReference(paymentReference);
        request.setReportStatus(reportStatus);
        request.setReportScanUrl(reportScanUrl);

        if ("PAID".equalsIgnoreCase(paymentStage) || "SETTLED".equalsIgnoreCase(paymentStage)) {
            request.setPaymentStatus("PAID");
        } else if ("PENDING".equalsIgnoreCase(paymentStage)) {
            request.setPaymentStatus("PENDING");
        } else {
            request.setPaymentStatus("IN_REVIEW");
        }

        if (request.getStatus() == null || request.getStatus() == MissionRequest.MissionStatus.INITIATED) {
            request.setStatus(MissionRequest.MissionStatus.FORM_COMPLETED);
        }

        return requestRepository.save(request);
    }

    public List<MissionRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public MissionOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    private void logAudit(String action, String user, String details) {
        AuditLog log = new AuditLog(action, user, details);
        auditLogRepository.save(log);
    }

    public Object initiateRequest(MissionRequestDTO dto, String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initiateRequest'");
    }
}