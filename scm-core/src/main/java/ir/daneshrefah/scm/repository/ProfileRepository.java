package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.ProfileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends CrudRepository<ProfileEntity, String> {

    ProfileEntity findByCode(String code);

}
