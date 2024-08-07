package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasource;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ParameterDatasourceMapper {
    ParameterDatasourceMapper INSTANCE = Mappers.getMapper(ParameterDatasourceMapper.class);

    ParameterDatasource toModel(ParameterDatasourceEntity entity);
    ParameterDatasourceEntity toEntity(ParameterDatasource model);
}
