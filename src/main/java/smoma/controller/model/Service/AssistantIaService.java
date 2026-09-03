package smoma.controller.model.Service;

import org.springframework.stereotype.Service;
import smoma.controller.model.*;
import smoma.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssistantIaService {

    private final MandatDeMissionRepository mandatRepository;
    private final OrdreDeMissionRepository ordreRepository;
    private final PersonnelRepository personnelRepository;

    public AssistantIaService(MandatDeMissionRepository mandatRepository,
                              OrdreDeMissionRepository ordreRepository,
                              PersonnelRepository personnelRepository) {
        this.mandatRepository = mandatRepository;
        this.ordreRepository = ordreRepository;
        this.personnelRepository = personnelRepository;
    }

    public Map<String, Object> processQuery(String userPrompt) {
        if (userPrompt == null) userPrompt = "";
        String promptLower = userPrompt.toLowerCase();

        Map<String, Object> result = new HashMap<>();
        result.put("query", userPrompt);
        result.put("timestamp", new Date());

        if (promptLower.contains("mandat") || promptLower.contains("dg")) {
            List<MandatDeMission> mandats = mandatRepository.findAll();
            result.put("type", "MANDATS");
            result.put("message", "Assistant IA : " + mandats.size() + " mandat(s) de mission trouve(s) dans le système.");
            result.put("data", mandats);
        } else if (promptLower.contains("agent") || promptLower.contains("personnel") || promptLower.contains("effectif")) {
            List<Personnel> staff = personnelRepository.findAll();
            result.put("type", "PERSONNEL");
            result.put("message", "Assistant IA : " + staff.size() + " agent(s) repertorie(s) à l'ART.");
            result.put("data", staff);
        } else if (promptLower.contains("frais") || promptLower.contains("sans frais")) {
            List<OrdreDeMission> sansFrais = ordreRepository.findAll().stream()
                    .filter(OrdreDeMission::isSansFrais)
                    .collect(Collectors.toList());
            result.put("type", "SANS_FRAIS");
            result.put("message", "Assistant IA : " + sansFrais.size() + " ordre(s) de mission avec mention SANS FRAIS enregistre(s).");
            result.put("data", sansFrais);
        } else {
            List<OrdreDeMission> ordres = ordreRepository.findAll();
            result.put("type", "ORDRES_MISSION");
            result.put("message", "Assistant IA : Analyse effectuee. " + ordres.size() + " ordre(s) de mission traite(s) dans la base.");
            result.put("data", ordres);
        }

        return result;
    }
}
