package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.limitation.ServiceLimitationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceLimitationRepository extends CrudRepository<ServiceLimitationEntity, String> {

}
