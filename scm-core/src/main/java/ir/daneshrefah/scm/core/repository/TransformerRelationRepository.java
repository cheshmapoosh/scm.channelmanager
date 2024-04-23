package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.entity.transformer.TransformerRelationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransformerRelationRepository extends CrudRepository<TransformerRelationEntity, String> {

    List<TransformerRelationEntity> findAllBySourceIdAndRelationType(String sourceId, TransformerRelationType relationType);

    List<TransformerRelationEntity> findAllBySourceId(String sourceId);

}
