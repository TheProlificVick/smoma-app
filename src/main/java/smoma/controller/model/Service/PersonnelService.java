package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.*;
import smoma.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final MandatDeMissionRepository mandatRepository;

    public PersonnelService(PersonnelRepository personnelRepository,
                            OrdreDeMissionRepository ordreRepository,
                            MandatDeMissionRepository mandatRepository) {
        this.personnelRepository = personnelRepository;
        this.ordreRepository = ordreRepository;
        this.mandatRepository = mandatRepository;
    }

    public List<Personnel> searchStaff(String query, String structure, String grade, String statut) {
        List<Personnel> all = personnelRepository.findAll();
        return all.stream()
                .filter(p -> query == null || query.isBlank() || 
                        (p.getNom() != null && p.getNom().toLowerCase().contains(query.toLowerCase())) ||
                        (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(query.toLowerCase())) ||
                        (p.getMatricule() != null && p.getMatricule().toLowerCase().contains(query.toLowerCase())))
                .filter(p -> structure == null || structure.isBlank() || 
                        (p.getDepartement() != null && p.getDepartement().equalsIgnoreCase(structure)))
                .filter(p -> grade == null || grade.isBlank() || 
                        (p.getGrade() != null && p.getGrade().name().equalsIgnoreCase(grade)))
                .filter(p -> statut == null || statut.isBlank() || 
                        (p.getStatut() != null && p.getStatut().name().equalsIgnoreCase(statut)))
                .toList();
    }

    public Map<String, Object> getCompteIndividuel(Long personnelId, LocalDate startDate, LocalDate endDate) {
        Personnel agent = personnelRepository.findById(personnelId)
                .orElseThrow(() -> new IllegalArgumentException("Agent introuvable: " + personnelId));

        if (startDate == null) startDate = LocalDate.now().withDayOfYear(1);
        if (endDate == null) endDate = LocalDate.now();

        final LocalDate start = startDate;
        final LocalDate end = endDate;

        List<OrdreDeMission> agentOrders = ordreRepository.findAll().stream()
                .filter(o -> o.getPersonnel() != null && o.getPersonnel().getId().equals(personnelId))
                .filter(o -> o.getDateDebut() != null && !o.getDateDebut().isBefore(start) && !o.getDateDebut().isAfter(end))
                .toList();

        long totalDays = 0;
        BigDecimal totalIndemnites = BigDecimal.ZERO;
        BigDecimal totalAvances = BigDecimal.ZERO;
        BigDecimal totalSoldes = BigDecimal.ZERO;

        for (OrdreDeMission o : agentOrders) {
            if (o.getDateDebut() != null && o.getDateFin() != null) {
                totalDays += (ChronoUnit.DAYS.between(o.getDateDebut(), o.getDateFin()) + 1);
            }
            if (o.getMontantIndemnite() != null) totalIndemnites = totalIndemnites.add(o.getMontantIndemnite());
            if (o.getMontantAvance() != null) totalAvances = totalAvances.add(o.getMontantAvance());
            if (o.getMontantSolde() != null) totalSoldes = totalSoldes.add(o.getMontantSolde());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("personnel", agent);
        response.put("periodeDebut", start);
        response.put("periodeFin", end);
        response.put("totalMissions", agentOrders.size());
        response.put("totalJoursMission", totalDays);
        response.put("totalIndemnites", totalIndemnites);
        response.put("totalAvancesPerçues", totalAvances);
        response.put("totalSoldesRestants", totalSoldes);
        response.put("ordresDeMission", agentOrders);

        return response;
    }

    public List<Personnel> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    public Personnel getById(Long id) {
        return personnelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent introuvable: " + id));
    }

    public Personnel save(Personnel p) {
        return personnelRepository.save(p);
    }
}
