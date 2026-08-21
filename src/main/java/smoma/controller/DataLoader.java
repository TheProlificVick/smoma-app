package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import smoma.controller.model.Service.Role;
import smoma.controller.model.User;
import smoma.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin", "admin123", "System Administrator", "admin@art.cm", "IT", Role.ROLE_ADMIN));
            userRepository.save(new User("gm_user", "password", "General Manager", "gm@art.cm", "Executive", Role.ROLE_GENERAL_MANAGER));
            userRepository.save(new User("hr_officer", "password", "HR Officer", "hr@art.cm", "Human Resources", Role.ROLE_HR_OFFICER));
            userRepository.save(new User("dept_rep", "password", "Dept Representative", "dept@art.cm", "Telecommunications", Role.ROLE_DEPARTMENT_REPRESENTATIVE));
            userRepository.save(new User("staff_member", "password", "Staff Member", "staff@art.cm", "Network Engineering", Role.ROLE_STAFF_MEMBER));
        }
    }
}