package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.error.ErrorMappingEntity;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ErrorMappingMapper {
    ErrorMappingMapper INSTANCE = Mappers.getMapper(ErrorMappingMapper.class);

    @Mapping(target = "bundleKey",ignore = true)
    ErrorMapping toModel(ErrorMappingEntity entity);
    ErrorMappingEntity toEntity(ErrorMapping errorMapping);


}
