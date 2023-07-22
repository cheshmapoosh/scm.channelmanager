package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.service.ServiceRelationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRelationRepository extends CrudRepository<ServiceRelationEntity, String> {

    List<ServiceRelationEntity> findServiceRelationEntityByServiceEntityId(String serviceEntityId);

}
