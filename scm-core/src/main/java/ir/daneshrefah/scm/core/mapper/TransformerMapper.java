package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.transformer.Transformer;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerRelationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TransformerMapper {
    TransformerMapper INSTANCE = Mappers.getMapper(TransformerMapper.class);

    Transformer toModel(TransformerEntity entity);
    TransformerRelation toRelationModel(TransformerRelationEntity entity);

    List<Transformer> entitiesToModels(Iterable<TransformerEntity> entities);
    List<TransformerRelation> entitiesToRelationModels(Iterable<TransformerRelationEntity> entities);
}
