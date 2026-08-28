package smoma.controller.model.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import smoma.controller.model.MandatDeMission;
import smoma.controller.model.OrdreDeMission;
import smoma.controller.model.Personnel;
import smoma.dto.StaffMissionDashboardDTO;
import smoma.repository.MissionRequestRepository;

@Service
public class DashboardService {

    private final MissionRequestRepository missionRequestRepo;

    public DashboardService(MissionRequestRepository missionRequestRepo) {
        this.missionRequestRepo = missionRequestRepo;
    }

    public StaffMissionDashboardDTO getStaffAnnualDashboard(Long staffId, Integer fiscalYear) {
        StaffMissionDashboardDTO dto = new StaffMissionDashboardDTO();
        dto.setAnneeFiscale(fiscalYear != null ? fiscalYear : LocalDate.now().getYear());
        dto.setTotalMandats(0);
        dto.setTotalOrdres(0);
        dto.setTotalJours(0L);
        dto.setMontantTotal(0.0);
        return dto;
    }

    public List<StaffMissionDashboardDTO> getAllStaffAnnualDashboard(Integer fiscalYear) {
        return new ArrayList<>();
    }
}
