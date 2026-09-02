package smoma.controller.model.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import smoma.dto.HRFormDTO;
import smoma.controller.model.MissionOrder;
import smoma.controller.model.MissionRequest;
import smoma.controller.model.Service.Role;
import smoma.controller.model.User;
import smoma.repository.MissionOrderRepository;
import smoma.repository.MissionRequestRepository;
import smoma.repository.UserRepository;
import smoma.controller.model.Service.MissionOrderService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class MissionWorkflowIntegrationTest {

    @Autowired 
    private MissionOrderService missionOrderService;

    @Autowired 
    private UserRepository userRepository;

    @Autowired 
    private MissionRequestRepository requestRepository;

    @Autowired
    private MissionOrderRepository orderRepository;

    private User initiator;
    private User targetStaff;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();

        initiator = userRepository.save(new User(
                "initiator_user",
                "password123",
                "Initiator Test",
                "initiator@art.cm",
                "IT",
                Role.ROLE_ADMIN
        ));

        targetStaff = userRepository.save(new User(
                "staff_user",
                "password123",
                "Staff Test",
                "staff@art.cm",
                "CSI",
                Role.ROLE_STAFF_MEMBER
        ));
    }

    @Test
    void testFullWorkflowCycle() {
        // 1. Initiate Request
        MissionRequest request = missionOrderService.initiateRequest(
                initiator.getId(), 
                targetStaff.getId(), 
                "Audit Mission", 
                "System Inspection", 
                "Douala"
        );

        assertNotNull(request.getId());
        assertEquals(MissionRequest.MissionStatus.INITIATED, request.getStatus());

        // 2. GM Review (Approve)
        MissionRequest approved = missionOrderService.reviewByGM(
                request.getId(), 
                true, 
                "Approved by GM", 
                "gm_user"
        );
        assertEquals(MissionRequest.MissionStatus.GM_APPROVED, approved.getStatus());

        // 3. HR Form Completion
        HRFormDTO hrDto = new HRFormDTO();
        hrDto.setRequestId(request.getId());
        hrDto.setItinerary("Yaoundé - Douala");
        hrDto.setDurationDays(4);
        hrDto.setTransportMode("Road");

        MissionOrder order = missionOrderService.completeHRForm(hrDto, "hr_officer");
        
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(MissionRequest.MissionStatus.ISSUED, order.getStatus());
        assertEquals(4, order.getFormDetail().getDurationDays());
    }

    @Test
    void testRequiredRoleRulesAreEnforced() {
        assertEquals(true, missionOrderService.canCreateMissionRequest(Role.ROLE_DEPARTMENT_REPRESENTATIVE));
        assertEquals(false, missionOrderService.canCreateMissionRequest(Role.ROLE_HR_OFFICER));
        assertEquals(true, missionOrderService.canCompleteHrForm(Role.ROLE_HR_OFFICER));
        assertEquals(false, missionOrderService.canCompleteHrForm(Role.ROLE_STAFF_MEMBER));
        assertEquals(true, missionOrderService.canReviewMission(Role.ROLE_GENERAL_MANAGER));
        assertEquals(false, missionOrderService.canReviewMission(Role.ROLE_STAFF_MEMBER));
    }
}