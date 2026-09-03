package smoma.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/admin")
public class AdminDbJdbcController {

    private final JdbcTemplate jdbc;

    public AdminDbJdbcController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/users-jdbc")
    public List<Map<String, Object>> recentUsersJdbc() {
        String q = "SELECT id, username, email, matricule, role FROM users ORDER BY id DESC LIMIT 50";
        return jdbc.queryForList(q);
    }
}
