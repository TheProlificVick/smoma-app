package smoma.controller.model.Service;

 
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import smoma.dto.LdapUserDTO;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
public class LdapSyncService {

    private final LdapTemplate ldapTemplate;

    public LdapSyncService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public List<LdapUserDTO> searchUsers(String searchTerm) {
        // LDAP Bitwise filter: (!(userAccountControl:1.2.840.113556.1.4.803:=2)) ensures account is active
        String filter = String.format(
            "(& (objectClass=user) (objectCategory=person) (!(userAccountControl:1.2.840.113556.1.4.803:=2)) (| (sAMAccountName=*%s*) (givenName=*%s*) (sn=*%s*) (department=*%s*)))",
            searchTerm, searchTerm, searchTerm, searchTerm
        );

        return ldapTemplate.search("", filter, new LdapUserRowMapper());
    }

    private static class LdapUserRowMapper implements AttributesMapper<LdapUserDTO> {
        @Override
        public LdapUserDTO mapFromAttributes(Attributes attrs) throws NamingException {
            LdapUserDTO user = new LdapUserDTO();
            user.setLogin(getAttrValue(attrs, "sAMAccountName"));
            user.setNom(getAttrValue(attrs, "givenName"));
            user.setPrenom(getAttrValue(attrs, "sn"));
            user.setEmail(getAttrValue(attrs, "mail"));
            user.setCodeFonction(getAttrValue(attrs, "title"));
            user.setNomStructure(getAttrValue(attrs, "department"));
            user.setMatricule(extractMatricule(attrs));
            return user;
        }

        private String extractMatricule(Attributes attrs) {
            String[] fallbackOrder = {"description", "info", "comment", "employeeID", "employeeNumber"};
            for (String attrName : fallbackOrder) {
                String val = getAttrValue(attrs, attrName);
                if (!val.isEmpty()) {
                    return val;
                }
            }
            return "";
        }

        private String getAttrValue(Attributes attrs, String attrName) {
            try {
                Attribute attr = attrs.get(attrName);
                if (attr != null && attr.get() != null) {
                    return attr.get().toString().trim();
                }
            } catch (NamingException ignored) {}
            return "";
        }
    }
}
