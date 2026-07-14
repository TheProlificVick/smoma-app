package smoma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import smoma.controller.model.MissionOrder;
import smoma.repository.MissionOrderRepository;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private MissionOrderRepository missionOrderRepository;

    @Override
    public void run(String... args) throws Exception {
        if (missionOrderRepository.count() == 0) {
            System.out.println("⏳ MySQL table 'mission_orders' is empty. Initializing seed queries...");

            MissionOrder m1 = new MissionOrder();
            m1.setMissionCode("MSN-8091");
            m1.setStaffId(1L); 
            m1.setStaffMember("Alvick Ambas");
            m1.setDestination("Kribi (KRI)");
            m1.setDuration("5");   
            m1.setStatus("APPROVED"); 
            missionOrderRepository.save(m1);

            MissionOrder m2 = new MissionOrder();
            m2.setMissionCode("MSN-8092");
            m2.setStaffId(2L);
            m2.setStaffMember("Jean-Paul Belinga");
            m2.setDestination("Douala (DLA)");
            m2.setDuration("12");  
            m2.setStatus("PENDING");  
            missionOrderRepository.save(m2);

            MissionOrder m3 = new MissionOrder();
            m3.setMissionCode("MSN-8093");
            m3.setStaffId(1L);
            m3.setStaffMember("Alvick Ambas");
            m3.setDestination("Garoua (GOU)");
            m3.setDuration("3");   
            m3.setStatus("REJECTED"); 
            missionOrderRepository.save(m3);

            System.out.println("✅ Institutional records successfully persisted to MySQL database!");
        }
    }
}