package smoma.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import smoma.controller.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
}
