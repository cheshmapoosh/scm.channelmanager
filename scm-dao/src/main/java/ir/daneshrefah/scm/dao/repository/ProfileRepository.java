package ir.daneshrefah.scm.dao.repository;

import ir.daneshrefah.scm.dao.entity.ProfileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends CrudRepository<ProfileEntity, String> {

    ProfileEntity findByName(String name);

}
