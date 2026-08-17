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
            m1.setMissionCode("");
            m1.setStaffId(1L); 
            m1.setStaffMember("");
            m1.setDestination("");
            m1.setDuration("");   
            m1.setStatus(""); 
            missionOrderRepository.save(m1);

            MissionOrder m2 = new MissionOrder();
            m2.setMissionCode("");
            m2.setStaffId(2L);
            m2.setStaffMember("");
            m2.setDestination("");
            m2.setDuration("");  
            m2.setStatus("");  
            missionOrderRepository.save(m2);

            MissionOrder m3 = new MissionOrder();
            m3.setMissionCode("");
            m3.setStaffId(1L);
            m3.setStaffMember("");
            m3.setDestination("");
            m3.setDuration("");   
            m3.setStatus(""); 
            missionOrderRepository.save(m3);

            System.out.println("✅ Institutional records successfully persisted to MySQL database!");
        }
    }
}