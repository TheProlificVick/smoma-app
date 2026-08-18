package smoma.controller.model.Service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import smoma.controller.model.*;
import smoma.repository.StaffMemberRepository;
import smoma.controller.model.Service.MissionWorkflowService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MissionWorkflowIntegrationTest {

    @Autowired
    private MissionWorkflowService workflowService;

    @Autowired
    private StaffMemberRepository staffRepository;

    @Test
    void testFullMissionWorkflowInitiation() {
        StaffMember initiator = staffRepository.save(StaffMember.builder()
                .username("test_initiator")
                .password("pass")
                .fullName("Test Initiator")
                .email("test1@art.cm")
                .role(RoleScope.DEPARTMENT_REP)
                .build());

        StaffMember staff = staffRepository.save(StaffMember.builder()
                .username("test_staff")
                .password("pass")
                .fullName("Test Staff")
                .email("test2@art.cm")
                .role(RoleScope.STAFF_MEMBER)
                .build());

        MissionRequest request = workflowService.initiateRequest(
                initiator.getId(),
                staff.getId(),
                "Field Mission Inspection",
                "Audit regional telecom infrastructure",
                "Annual regulatory compliance requirement"
        );

        assertNotNull(request.getId());
        assertEquals(MissionState.INITIATED, request.getState());
        assertEquals("Field Mission Inspection", request.getTitle());
    }
}