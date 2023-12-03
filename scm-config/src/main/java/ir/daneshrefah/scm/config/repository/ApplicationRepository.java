package ir.daneshrefah.scm.config.repository;

import ir.daneshrefah.scm.config.model.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity,String> {

    ApplicationEntity findByCode(String code);
}
