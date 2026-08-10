package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LdapAuthenticationService {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    /**
     * Authenticates a user against mock LDAP credentials and synchronizes 
     * user attributes into the MySQL database upon successful login.
     */
    public Optional<StaffMember> authenticateAndSyncUser(String identifier, String rawPassword) {
        if (identifier == null || rawPassword == null || identifier.isBlank()) {
            return Optional.empty();
        }

        // 1. Check password (case-insensitive & whitespace trimmed)
        boolean isAuthenticated = mockLdapBind(identifier, rawPassword);

        if (!isAuthenticated) {
            return Optional.empty();
        }

        // 2. Normalize identifier & extract clean sAMAccountName
        String cleanIdentifier = identifier.trim().toLowerCase();
        String cleanSamName = extractSamAccountName(cleanIdentifier);
        String fullEmail = cleanIdentifier.contains("@") ? cleanIdentifier : cleanSamName + "@art.cm";

        // 3. Fetch existing staff member or instantiate new JIT record
        StaffMember staff = staffMemberRepository.findBySamAccountName(cleanSamName)
                .orElseGet(() -> staffMemberRepository.findByEmail(fullEmail)
                .orElseGet(() -> {
                    StaffMember newStaff = new StaffMember();
                    newStaff.setSamAccountName(cleanSamName);
                    newStaff.setEmail(fullEmail);
                    
                    // Assign default display names from username (e.g. alvick.ambas -> Alvick Ambas)
                    String formattedName = capitalizeWords(cleanSamName.replace(".", " "));
                    newStaff.setFirstName(formattedName);
                    newStaff.setLastName("ART");
                    
                    // Assign default role if needed by schema
                    try {
                        newStaff.setRoleScope("STAFF");
                    } catch (Exception ignored) {}

                    return newStaff;
                }));

        // 4. Update JIT fields & ensure non-null mandatory fields
        if (staff.getEmail() == null) staff.setEmail(fullEmail);
        if (staff.getFirstName() == null) staff.setFirstName(capitalizeWords(cleanSamName.replace(".", " ")));
        if (staff.getLastName() == null) staff.setLastName("ART");

        staff.setIsAdManaged(true);
        staff.setLastAdSyncAt(LocalDateTime.now());
        
        try {
            return Optional.of(staffMemberRepository.save(staff));
        } catch (Exception e) {
            System.err.println("Error saving synced LDAP staff to database: " + e.getMessage());
            // Fallback: return unpersisted staff object so login succeeds regardless of DB write lock
            return Optional.of(staff);
        }
    }

    private boolean mockLdapBind(String identifier, String rawPassword) {
        if (rawPassword == null) return false;
        String trimmedPass = rawPassword.trim();
        // Accepts Password123!, password123, or admin passwords
        return "Password123!".equalsIgnoreCase(trimmedPass) || "password123".equalsIgnoreCase(trimmedPass);
    }

    private String extractSamAccountName(String identifier) {
        if (identifier.contains("@")) {
            return identifier.substring(0, identifier.indexOf("@"));
        }
        return identifier;
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}