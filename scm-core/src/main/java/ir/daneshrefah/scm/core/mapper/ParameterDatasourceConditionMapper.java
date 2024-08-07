package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceConditionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ParameterDatasourceConditionMapper {
    ParameterDatasourceConditionMapper INSTANCE = Mappers.getMapper(ParameterDatasourceConditionMapper.class);
    ParameterDatasourceCondition toModel(ParameterDatasourceConditionEntity entity);
    ParameterDatasourceConditionEntity toEntity(ParameterDatasourceCondition model);
}
