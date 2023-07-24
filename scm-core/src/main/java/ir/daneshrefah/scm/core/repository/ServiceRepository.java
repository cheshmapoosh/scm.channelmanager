package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends CrudRepository<ServiceEntity, String> {

}
