package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.error.ErrorMappingEntity;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE,
        componentModel = SPRING
        , uses = {PersonMapper.class})
public interface ErrorMappingMapper {

    @Mapping(target = "bundleKey",ignore = true)
    ErrorMapping toModel(ErrorMappingEntity entity);
    ErrorMappingEntity toEntity(ErrorMapping errorMapping);


}
