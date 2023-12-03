package ir.daneshrefah.scm.config.repository;

import ir.daneshrefah.scm.config.model.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity,String> {

    ProfileEntity findByCode(String code);
}
