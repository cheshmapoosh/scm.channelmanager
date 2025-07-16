package ir.daneshrefah.scm.core.mapper.definition;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.core.entity.definition.DefinitionEntity;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface DefinitionMapper {
    DefinitionEntity toEntity(Definition definition);

    Definition toModel(DefinitionEntity definitionEntity);
}