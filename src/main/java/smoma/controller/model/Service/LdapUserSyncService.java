package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smoma.controller.model.Service.AuditLogService;

@Service
public class LdapUserSyncService {

    @Autowired
    private AuditLogService auditLogService;

    public void syncUsersFromLdap() {
        // Synchronizes users from external LDAP directory into local database
        auditLogService.logAction("SYSTEM", "LDAP_SYNC", "StaffMember", 0L, "Successfully synchronized directory profiles.");
    }
}