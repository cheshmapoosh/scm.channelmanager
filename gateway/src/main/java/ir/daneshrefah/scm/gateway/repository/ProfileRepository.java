package ir.daneshrefah.scm.gateway.repository;

import ir.daneshrefah.scm.common.model.ProfileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends CrudRepository<ProfileEntity, String> {

    ProfileEntity findByName(String name);

}
