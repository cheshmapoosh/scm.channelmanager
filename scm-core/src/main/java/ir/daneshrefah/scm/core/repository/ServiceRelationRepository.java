package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRelationRepository extends CrudRepository<ServiceRelationEntity, String> {

    Iterable<ServiceRelationEntity> findAllBySourceServiceId(String sourceServiceId);

}
