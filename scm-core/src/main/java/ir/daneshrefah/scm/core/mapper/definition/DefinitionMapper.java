package ir.daneshrefah.scm.core.mapper.definition;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.core.entity.definition.DefinitionEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DefinitionMapper {
    DefinitionEntity toEntity(Definition definition);

    Definition toDto(DefinitionEntity definitionEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DefinitionEntity partialUpdate(Definition definition, @MappingTarget DefinitionEntity definitionEntity);
}