package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Deprecated
public interface ServiceRelationRepository extends CrudRepository<ServiceRelationEntity, String> {

    List<ServiceRelationEntity> findAllBySourceServiceId(String sourceServiceId);
}
