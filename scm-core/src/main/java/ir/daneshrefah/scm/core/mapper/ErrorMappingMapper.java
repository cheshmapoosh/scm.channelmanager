package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.common.ErrorMappingEntity;
import ir.daneshrefah.scm.plugin.api.model.error.ErrorMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ErrorMappingMapper {
    ErrorMappingMapper INSTANCE = Mappers.getMapper(ErrorMappingMapper.class);

    @Mapping(source = "externalServiceProviderEntity", target = "externalServiceProvider")
    ErrorMapping toModel(ErrorMappingEntity entity);

    List<ErrorMapping> entitiesToModels(Iterable<ErrorMappingEntity> entities);
}
