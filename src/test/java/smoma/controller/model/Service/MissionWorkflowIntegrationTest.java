package smoma.controller.model.Service;

import smoma.controller.model.*;
import smoma.repository.AuditLogRepository;
import smoma.repository.MissionFormDetailRepository;
import smoma.repository.MissionOrderRepository;
import smoma.repository.MissionRequestRepository;
import smoma.repository.StaffMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MissionWorkflowIntegrationTest {

    @Autowired
    private MissionWorkflowService workflowService;

    @Autowired
    private MissionRequestRepository requestRepository;

    @Autowired
    private MissionFormDetailRepository formDetailRepository;

    @Autowired
    private MissionOrderRepository orderRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    @BeforeEach
    void cleanUpDatabase() {
        auditLogRepository.deleteAll();
        orderRepository.deleteAll();
        formDetailRepository.deleteAll();
        requestRepository.deleteAll();
        staffMemberRepository.deleteAll();
    }

    @Test
    void testEndToEndMissionOrderLifecycleWithAuditing() {
        // 1. Arrange: Create a Staff Member with all required database fields
        StaffMember staff = new StaffMember();
        staff.setFirstName("Alvick");
        staff.setLastName("Ambas");
        staff.setDepartment("CSI Division");
        staff.setMatricule("ART-2026-042"); 
        staff.setEmail("alvick.ambas@art.cm"); 
        staff.setRoleScope("STAFF"); 
        StaffMember savedStaff = staffMemberRepository.save(staff);

        // 2. Arrange a Mission Request
        MissionRequest request = new MissionRequest();
        request.setOriginatingDepartment("CSI Division");
        request.setIssueDescription("Regular Telecom Infrastructure Audit.");
        request.setTextJustification("Compliance monitoring directive.");
        request.setStaffMemberId(savedStaff.getId()); 

        // 3. Act: Initiate (UC-01)
        MissionRequest initiatedRequest = workflowService.initiateRequest(request);
        assertNotNull(initiatedRequest.getId());
        assertEquals(MissionState.INITIATED, initiatedRequest.getState());

        // 4. Act: GM Executive Review (UC-02)
        MissionRequest approvedRequest = workflowService.reviewByGM(initiatedRequest.getId(), true, "GM-STAMP-TEST");
        assertEquals(MissionState.GM_APPROVED, approvedRequest.getState());
        assertEquals("GM-STAMP-TEST", approvedRequest.getGmApprovalStamp());

        // 5. Act: HR Form Completion (UC-03)
        MissionFormDetail logisticsForm = new MissionFormDetail();
        logisticsForm.setDepartment("CSI Division");
        logisticsForm.setEquipmentRequired("Testing Probes");
        logisticsForm.setSafetyNotes("Standard field risks apply.");
        logisticsForm.setMissionObjectives("Complete base audit.");
        logisticsForm.setOriginCity("Yaoundé");
        logisticsForm.setTargetCities("Douala");
        logisticsForm.setTransitRoutes("Road Transport");
        logisticsForm.setCostCodes("ART-2026-FIN");
        logisticsForm.setPerDiemAmount(new BigDecimal("35000.00"));
        logisticsForm.setTotalAllowance(new BigDecimal("140000.00"));
        logisticsForm.setStartDate(LocalDate.now());
        logisticsForm.setEndDate(LocalDate.now().plusDays(4));
        logisticsForm.setTotalComputedActiveDays(4);

        MissionFormDetail savedLogistics = workflowService.populateHRDetails(approvedRequest.getId(), logisticsForm);
        assertNotNull(savedLogistics.getId());

        // Re-fetch request to verify intermediate transition state
        MissionRequest formCompletedRequest = requestRepository.findById(initiatedRequest.getId()).orElseThrow();
        assertEquals(MissionState.FORM_COMPLETED, formCompletedRequest.getState());

        // 6. Act: Complete Issuance
        MissionRequest finalizedRequest = workflowService.issueMissionOrder(formCompletedRequest.getId());
        assertEquals(MissionState.ISSUED_ACTIVE, finalizedRequest.getState());

        // 7. Assert: Immutable Audit Log Ledger Verification
        List<AuditLog> structuralLogs = auditLogRepository.findByMissionRequestIdOrderByTimestampAsc(finalizedRequest.getId());
        assertFalse(structuralLogs.isEmpty(), "Audit engine failed to record operations.");
        assertEquals(4, structuralLogs.size(), "The ledger must record exactly 4 distinct phase state transitions.");
        
        assertEquals("INITIATE", structuralLogs.get(0).getAction());
        assertEquals("APPROVE", structuralLogs.get(1).getAction());
        assertEquals("POPULATE_FORM", structuralLogs.get(2).getAction());
        assertEquals("ISSUE_DOCUMENT", structuralLogs.get(3).getAction());
    }
}