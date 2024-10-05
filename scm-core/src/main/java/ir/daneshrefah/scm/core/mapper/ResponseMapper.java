package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceConditionEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface ResponseMapper {
    ResponseMapper INSTANCE = Mappers.getMapper(ResponseMapper.class);

    @Mapping(source = "conditions", target = "conditions", qualifiedByName = "toConditionsModel")
    @Mapping(source = "responseParameters", target = "responseParameters", qualifiedByName = "toParametersModel")
    Response toModel(ResponseEntity entity);

    @Mapping(source = "conditions", target = "conditions", qualifiedByName = "toConditionsEntity")
    @Mapping(source = "responseParameters", target = "responseParameters", qualifiedByName = "toParametersEntity")
    ResponseEntity toEntity(Response model);

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
