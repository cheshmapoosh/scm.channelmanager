package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasource;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface ParameterMapper {
    ParameterMapper INSTANCE = Mappers.getMapper(ParameterMapper.class);

    @Mapping(source = "datasource", target = "datasource", qualifiedByName = "toDatasourceModel")
    Parameter toModel(ParameterEntity entity);

    @Mapping(source = "datasource", target = "datasource", qualifiedByName = "toDatasourceEntity")
    @Mapping(target = "serviceProvider", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "responseCondition", ignore = true)
    @Mapping(target = "parent.serviceProvider", ignore = true)
    @Mapping(target = "parent.service", ignore = true)
    @Mapping(target = "parent.responseCondition", ignore = true)
    ParameterEntity toEntity(Parameter entity);

    @Named("toDatasourceModel")
    default ParameterDatasource toDatasourceModel(ParameterDatasourceEntity entity) {
        if (Objects.nonNull(entity)) {
            return ParameterDatasourceMapper.INSTANCE.toModel(entity);
        }
        return null;
    }

    @Named("toDatasourceEntity")
    default ParameterDatasourceEntity toDatasourceEntity(ParameterDatasource model) {
        if (Objects.nonNull(model)) {
            return ParameterDatasourceMapper.INSTANCE.toEntity(model);
        }
        return null;
    }

    default List<Parameter> toModelList(List<ParameterEntity> entities, ParameterActionType actionType) {
        if (Objects.nonNull(entities)) {
            return entities
                    .stream()
                    .map(this::toModel)
                    .filter(parameter -> parameter.getActionType().equals(actionType))
                    .toList();
        }
        return null;
    }

    default List<Parameter> toModelList(List<ParameterEntity> entities) {
        return entities
                .stream()
                .map(this::toModel)
                .toList();
    }

    default List<ParameterEntity> toEntityList(List<Parameter> models) {
        if (Objects.nonNull(models)) {
            return models
                    .stream()
                    .map(this::toEntity)
                    .toList();
        }
        return null;
    }
}
