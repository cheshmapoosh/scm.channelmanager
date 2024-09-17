package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceConditionEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseConditionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface ResponseConditionMapper {
    ResponseConditionMapper INSTANCE = Mappers.getMapper(ResponseConditionMapper.class);

    @Mapping(source = "conditions", target = "conditions", qualifiedByName = "toConditionsModel")
    @Mapping(source = "responseParameters", target = "responseParameters", qualifiedByName = "toParametersModel")
    ResponseCondition toModel(ResponseConditionEntity entity);

    @Mapping(source = "conditions", target = "conditions", qualifiedByName = "toConditionsEntity")
    @Mapping(source = "responseParameters", target = "responseParameters", qualifiedByName = "toParametersEntity")
    ResponseConditionEntity toEntity(ResponseCondition model);

    @Named("toConditionsModel")
    default List<ParameterDatasourceCondition> toConditionsModel(List<ParameterDatasourceConditionEntity> entities) {
        if (Objects.nonNull(entities)) {
            return entities.stream().map(ParameterDatasourceConditionMapper.INSTANCE::toModel).toList();
        }
        return null;
    }

    @Named("toConditionsEntity")
    default List<ParameterDatasourceConditionEntity> toConditionsEntity(List<ParameterDatasourceCondition> models) {
        if (Objects.nonNull(models)) {
            return models.stream().map(ParameterDatasourceConditionMapper.INSTANCE::toEntity).toList();
        }
        return null;
    }

    @Named("toParametersModel")
    default List<Parameter> toParametersModel(List<ParameterEntity> entities) {
        if (Objects.nonNull(entities)) {
            return entities.stream().map(ParameterMapper.INSTANCE::toModel).toList();
        }
        return null;
    }

    @Named("toParametersEntity")
    default List<ParameterEntity> toParametersEntity(List<Parameter> models) {
        if ((Objects.nonNull(models))) {
            return models.stream().map(ParameterMapper.INSTANCE::toEntity).toList();
        }
        return null;
    }

}
