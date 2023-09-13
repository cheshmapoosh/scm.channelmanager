package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRelationRepository extends CrudRepository<ServiceRelationEntity, String> {

    Iterable<ServiceRelationEntity> findAllBySourceServiceIdAndRelationType(String sourceServiceId, ServiceRelationType relationType);

}
