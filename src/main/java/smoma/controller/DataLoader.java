package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import smoma.controller.model.Service.RoleScope;
import smoma.controller.model.StaffMember;
import smoma.repository.StaffMemberRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private StaffMemberRepository staffRepository;

    @Override
    public void run(String... args) throws Exception {
        if (staffRepository.count() == 0) {
            staffRepository.save(new StaffMember(null, "admin", "admin123", "System Administrator", "admin@art.cm", "CSI", "Engineer", RoleScope.ADMIN, true));
            staffRepository.save(new StaffMember(null, "gm_user", "gm123", "Prof. Philemon ZOO ZAME", "gm@art.cm", "Direction Générale", "General Manager", RoleScope.GENERAL_MANAGER, true));
            staffRepository.save(new StaffMember(null, "hr_officer", "hr123", "HR Officer", "hr@art.cm", "Human Resources", "HR Specialist", RoleScope.HR_OFFICER, true));
            staffRepository.save(new StaffMember(null, "omenga_alvick", "staff123", "OMENGA AMBAS Alvick", "a.omenga@art.cm", "CSI", "Software Engineer Intern", RoleScope.STAFF_MEMBER, true));
        }
    }
}