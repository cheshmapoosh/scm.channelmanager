package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentProviderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceComponentProviderRepository extends CrudRepository<ServiceComponentProviderEntity, String> {

}
