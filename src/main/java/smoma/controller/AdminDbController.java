package smoma.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smoma.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/admin")
public class AdminDbController {

    private final UserRepository userRepository;

    public AdminDbController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> recentUsers() {
        List<Map<String, Object>> out = new ArrayList<>();
        userRepository.findAll().stream().limit(50).forEach(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("email", u.getEmail());
            m.put("matricule", u.getMatricule());
            m.put("role", u.getRole());
            out.add(m);
        });
        return out;
    }
}
