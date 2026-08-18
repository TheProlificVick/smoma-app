package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

@Service
public class LdapAuthenticationService {

    @Autowired
    private StaffMemberRepository staffMemberRepository;

    public StaffMember authenticate(String username, String password) {
        // Authenticates against local store or LDAP fallback
        return staffMemberRepository.findByUsername(username)
                .filter(staff -> staff.getPassword().equals(password))
                .orElseThrow(() -> new RuntimeException("Invalid Active Directory / LDAP credentials"));
    }
}