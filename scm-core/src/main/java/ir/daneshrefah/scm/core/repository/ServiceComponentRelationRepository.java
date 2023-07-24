package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.ServiceComponentRelationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceComponentRelationRepository extends CrudRepository<ServiceComponentRelationEntity, String> {

    List<ServiceComponentRelationEntity> findServiceRelationEntityByServiceEntityId(String serviceEntityId);

}
