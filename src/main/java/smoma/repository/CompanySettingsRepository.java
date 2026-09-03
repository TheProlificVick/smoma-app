package smoma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smoma.controller.model.CompanySettings;

@Repository
public interface CompanySettingsRepository extends JpaRepository<CompanySettings, Long> {
}
