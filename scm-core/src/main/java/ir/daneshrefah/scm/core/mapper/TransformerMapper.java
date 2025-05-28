package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.transformer.Transformer;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerRelationEntity;
import org.mapstruct.Mapper;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface TransformerMapper {

    Transformer toModel(TransformerEntity entity);

    TransformerRelation toRelationModel(TransformerRelationEntity entity);

    List<Transformer> entitiesToModels(Iterable<TransformerEntity> entities);

    List<TransformerRelation> entitiesToRelationModels(Iterable<TransformerRelationEntity> entities);
}
