package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.entity.transformer.TransformerRelationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransformerRelationRepository extends CrudRepository<TransformerRelationEntity, String> {

    Iterable<TransformerRelationEntity> findAllBySourceIdAndRelationType(String sourceId, TransformerRelationType relationType);

    Iterable<TransformerRelationEntity> findAllBySourceId(String sourceId);

}
