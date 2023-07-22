package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.component.ServiceComponentProviderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceComponentProviderRepository extends CrudRepository<ServiceComponentProviderEntity, String> {

}
