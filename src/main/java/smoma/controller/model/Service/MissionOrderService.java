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

    @Transactional
    public MissionRequest initiateRequest(Long initiatorId, Long targetStaffId, String title, String justification, String destination) {
        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found: " + initiatorId));
        User targetStaff = userRepository.findById(targetStaffId)
                .orElseThrow(() -> new IllegalArgumentException("Target staff not found: " + targetStaffId));

        MissionRequest request = new MissionRequest();
        request.setTitle(title);
        request.setJustification(justification);
        request.setDestination(destination);
        request.setStatus(MissionRequest.MissionStatus.INITIATED);
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

        request.setStatus(approve ? MissionRequest.MissionStatus.GM_APPROVED : MissionRequest.MissionStatus.REJECTED);
        MissionRequest updated = requestRepository.save(request);

        logAudit("GM_REVIEW", gmUsername, "Request ID " + requestId + " marked as " + request.getStatus());
        return updated;
    }

    @Transactional
    public MissionOrder completeHRForm(HRFormDTO dto, String hrUsername) {
        MissionRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + dto.getRequestId()));

        if (request.getStatus() != MissionRequest.MissionStatus.GM_APPROVED) {
            throw new IllegalStateException("Only GM Approved requests can be processed by HR.");
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
        requestRepository.save(request);

        MissionOrder savedOrder = orderRepository.save(order);
        logAudit("HR_FORM_COMPLETE", hrUsername, "Issued Order ID: " + savedOrder.getId());
        return savedOrder;
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