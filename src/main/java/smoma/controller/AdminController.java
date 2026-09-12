package smoma.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import smoma.controller.model.Department;
import smoma.controller.model.User;
import smoma.controller.model.Service.AccessPolicy;
import smoma.controller.model.Service.AdUserSyncService;
import smoma.controller.model.Service.LdapDirectoryService;
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

    private final AccessPolicy accessPolicy;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${spring.ldap.urls:ldap://192.168.0.101:389}")
    private String ldapServer;

    public AdminController(AdUserSyncService adUserSyncService,
                           LdapDirectoryService ldapDirectoryService,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           AccessPolicy accessPolicy) {
        this.adUserSyncService = adUserSyncService;
        this.ldapDirectoryService = ldapDirectoryService;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.accessPolicy = accessPolicy;
    }

    /** Every endpoint of this module is reserved to the application administrator. */
    private void assertAdmin(Principal principal, String requestEmail) {
        User actor = currentUser(principal, requestEmail);
        if (actor == null || !accessPolicy.isAdmin(actor)) {
            throw new SecurityException("Module réservé à l'administrateur de l'application. / Module reserved for the application administrator.");
        }
    }

    @PostMapping("/sync-ad")
    public ResponseEntity<?> syncActiveDirectory(Principal principal,
                                                 @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
        try {
            int count = adUserSyncService.syncUsersFromActiveDirectory();
            return ResponseEntity.ok(Map.of("message", "Active Directory sync finished", "syncedCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(Principal principal,
                                                  @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Returns the ENTIRE contents of the Active Directory as exposed by the configured
     * spring.ldap.urls. This includes users, groups, OUs, contacts, computers and domains.
     * Used by the Admin & AD Sync module to fortify role-based access.
     */
    @GetMapping("/ad-directory")
    public ResponseEntity<?> getFullActiveDirectory(Principal principal,
                                                    @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
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
    public ResponseEntity<?> getAdUsers(Principal principal,
                                        @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
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
    public ResponseEntity<?> getAdGroups(Principal principal,
                                         @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
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
    public ResponseEntity<?> getAdStatistics(Principal principal,
                                             @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
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

    private User currentUser(Principal principal, String requestEmail) {
        User u = null;
        if (principal != null) {
            u = userRepository.findByIdentity(principal.getName()).orElse(null);
        }
        if (u == null && requestEmail != null) {
            u = accessPolicy.resolve(requestEmail);
        }
        return u;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request,
                                      Principal principal,
                                      @RequestHeader(value = "X-User-Role", required = false) String requestRole,
                                      @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = currentUser(principal, requestEmail);
        if (!accessPolicy.canCreateOrgEntities(actor)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seuls l'administrateur système et le personnel DRH peuvent créer un compte utilisateur."));
        }

        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required."));
        }
        if (request.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe doit compter au moins 8 caractères."));
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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setMatricule(request.getMatricule());
        user.setStructure(department != null ? department.getName() : request.getStructure());
        user.setTitle(request.getTitle());
        user.setDepartment(department);
        user.setRole(request.getRole());
        user.setGenre(request.getGenre());
        user.setRang(request.getRang());
        user.setFonction(request.getFonction());
        user.setActive(true);

        try {
            User saved = userRepository.save(user);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not create user: " + ex.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments(Principal principal,
                                                          @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        assertAdmin(principal, requestEmail);
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody CreateDepartmentRequest request,
                                            Principal principal,
                                            @RequestHeader(value = "X-User-Role", required = false) String requestRole,
                                            @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = currentUser(principal, requestEmail);
        if (!accessPolicy.canCreateOrgEntities(actor)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seuls l'administrateur système et le personnel DRH peuvent créer une direction / structure."));
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

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        Principal principal,
                                        @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = currentUser(principal, requestEmail);
        if (!accessPolicy.canDeleteOrgEntities(actor)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seul l'administrateur système peut supprimer un compte utilisateur."));
        }
        User target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Utilisateur introuvable: " + id));
        }
        if (actor != null && target.getId().equals(actor.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vous ne pouvez pas supprimer votre propre compte."));
        }
        userRepository.delete(target);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id,
                                              Principal principal,
                                              @RequestHeader(value = "X-User-Email", required = false) String requestEmail) {
        User actor = currentUser(principal, requestEmail);
        if (!accessPolicy.canDeleteOrgEntities(actor)) {
            return ResponseEntity.status(403).body(Map.of("error",
                    "Seul l'administrateur système peut supprimer une direction / structure."));
        }
        Department target = departmentRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Direction introuvable: " + id));
        }
        // Detach any user still pointing at this department so the FK does not block the delete.
        userRepository.findAll().forEach(u -> {
            if (u.getDepartment() != null && u.getDepartment().getId().equals(id)) {
                u.setDepartment(null);
                userRepository.save(u);
            }
        });
        departmentRepository.delete(target);
        return ResponseEntity.ok(Map.of("status", "deleted", "id", id));
    }
}