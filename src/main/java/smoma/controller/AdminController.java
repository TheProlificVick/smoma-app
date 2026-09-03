package smoma.controller;

 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Department;
import smoma.controller.model.User;
import smoma.controller.model.Service.AdUserSyncService;
import smoma.controller.model.Service.LdapDirectoryService;
import smoma.controller.model.Service.Role;
import smoma.dto.AdDirectoryEntryDTO;
import smoma.dto.CreateDepartmentRequest;
import smoma.dto.CreateUserRequest;
import smoma.repository.DepartmentRepository;
import smoma.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdUserSyncService adUserSyncService;

    private final LdapDirectoryService ldapDirectoryService;

    private final UserRepository userRepository;

    private final DepartmentRepository departmentRepository;

    @Value("${spring.ldap.urls:ldap://192.168.0.101:389}")
    private String ldapServer;

    public AdminController(AdUserSyncService adUserSyncService,
                           LdapDirectoryService ldapDirectoryService,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository) {
        this.adUserSyncService = adUserSyncService;
        this.ldapDirectoryService = ldapDirectoryService;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostMapping("/sync-ad")
    public ResponseEntity<?> syncActiveDirectory() {
        try {
            int count = adUserSyncService.syncUsersFromActiveDirectory();
            return ResponseEntity.ok(Map.of("message", "Active Directory sync finished", "syncedCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Returns the ENTIRE contents of the Active Directory as exposed by the configured
     * spring.ldap.urls. This includes users, groups, OUs, contacts, computers and domains.
     * Used by the Admin & AD Sync module to fortify role-based access.
     */
    @GetMapping("/ad-directory")
    public ResponseEntity<?> getFullActiveDirectory() {
        try {
            List<AdDirectoryEntryDTO> entries = ldapDirectoryService.getAllDirectoryEntries();
            return ResponseEntity.ok(Map.of(
                "totalEntries", entries.size(),
                "entries", entries,
                "ldapServer", ldapServer
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unable to read Active Directory: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/ad-directory/users - Only the users from AD with role assignments.
     */
    @GetMapping("/ad-directory/users")
    public ResponseEntity<?> getAdUsers() {
        try {
            List<Map<String, String>> users = ldapDirectoryService.searchUsers(null);
            return ResponseEntity.ok(Map.of(
                    "totalUsers", users.size(),
                    "users", users
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unable to read AD users: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/ad-directory/groups - Returns only security groups & distribution groups.
     */
    @GetMapping("/ad-directory/groups")
    public ResponseEntity<?> getAdGroups() {
        try {
            List<AdDirectoryEntryDTO> all = ldapDirectoryService.getAllDirectoryEntries();
            List<AdDirectoryEntryDTO> groups = all.stream()
                    .filter(e -> "GROUP".equalsIgnoreCase(e.getEntryType()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of("totalGroups", groups.size(), "groups", groups));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unable to read AD groups: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/ad-directory/statistics - Summary stats of the whole directory.
     */
    @GetMapping("/ad-directory/statistics")
    public ResponseEntity<?> getAdStatistics() {
        try {
            List<AdDirectoryEntryDTO> all = ldapDirectoryService.getAllDirectoryEntries();
                    Map<String, Long> byType = all.stream()
                        .filter(Objects::nonNull)
                        .map(e -> e.getEntryType())
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                    Map<String, Long> byDepartment = all.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.getDepartment() != null && !e.getDepartment().isBlank())
                        .map(e -> e.getDepartment())
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                    long activeUsers = all.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> e.isAccountEnabled())
                        .filter(e -> "USER".equalsIgnoreCase(e.getEntryType()))
                        .count();

                    long disabledUsers = all.stream()
                        .filter(Objects::nonNull)
                        .filter(e -> !e.isAccountEnabled())
                        .filter(e -> "USER".equalsIgnoreCase(e.getEntryType()))
                        .count();

            return ResponseEntity.ok(Map.of(
                    "totalEntries", all.size(),
                    "entriesByType", byType,
                    "entriesByDepartment", byDepartment,
                    "activeUsers", activeUsers,
                    "disabledUsers", disabledUsers
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unable to compute AD statistics: " + e.getMessage()));
        }
    }

    private User requireAdmin(Principal principal, String requestRole, String requestEmail) {
        if (principal != null) {
            User current = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new SecurityException("Authenticated administrator not found."));
            if (current.getRole() == Role.ROLE_ADMIN) {
                return current;
            }
        }

        if (requestRole != null && requestRole.equalsIgnoreCase(Role.ROLE_ADMIN.name()) && requestEmail != null) {
            User current = userRepository.findByUsername(requestEmail)
                    .orElse(null);
            if (current != null && current.getRole() == Role.ROLE_ADMIN) {
                return current;
            }
        }

        throw new SecurityException("Only administrators can create users and departments.");
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request,
                                      Principal principal,
                                      @RequestHeader(value = "X-User-Role", required = false) String requestRole,
                                      @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        try {
            requireAdmin(principal, requestRole, requestEmail);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }

        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required."));
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists: " + request.getUsername()));
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + request.getDepartmentId()));
        } else if (request.getStructure() != null && !request.getStructure().isBlank()) {
            department = departmentRepository.findByName(request.getStructure())
                    .orElse(null);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setMatricule(request.getMatricule());
        user.setStructure(request.getStructure());
        user.setTitle(request.getTitle());
        user.setDepartment(department);
        user.setRole(request.getRole());
        user.setActive(true);

        try {
            User saved = userRepository.save(user);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not create user: " + ex.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody CreateDepartmentRequest request,
                                            Principal principal,
                                            @RequestHeader(value = "X-User-Role", required = false) String requestRole,
                                            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        try {
            requireAdmin(principal, requestRole, requestEmail);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }

        if (request == null || request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department name is required."));
        }

        Department existing = departmentRepository.findByName(request.getName()).orElse(null);
        if (existing != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department already exists: " + request.getName()));
        }

        Department department = new Department();
        department.setName(request.getName());
        department.setAcronym(request.getAcronym());
        department.setHeadName(request.getHeadName());

        return ResponseEntity.ok(departmentRepository.save(department));
    }
}