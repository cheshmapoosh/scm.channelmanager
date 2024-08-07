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

@Mapper
public interface ParameterMapper {
    ParameterMapper INSTANCE = Mappers.getMapper(ParameterMapper.class);

    @Mapping(source = "datasource", target = "datasource", qualifiedByName = "toDatasourceModel")
    Parameter toModel(ParameterEntity entity);

    @Mapping(source = "datasource", target = "datasource", qualifiedByName = "toDatasourceEntity")
    ParameterEntity toEntity(Parameter entity);

    @Named("toDatasourceModel")
    default ParameterDatasource toDatasourceModel(ParameterDatasourceEntity entity) {
        return ParameterDatasourceMapper.INSTANCE.toModel(entity);
    }

    @Named("toDatasourceEntity")
    default ParameterDatasourceEntity toDatasourceEntity(ParameterDatasource model) {
        return ParameterDatasourceMapper.INSTANCE.toEntity(model);
    }

    default List<Parameter> toModelList(List<ParameterEntity> entities, ParameterActionType actionType) {
        return entities
                .stream()
                .map(this::toModel)
                .filter(parameter -> parameter.getActionType().equals(actionType))
                .toList();
    }

    default List<ParameterEntity> toEntityList(List<Parameter> models) {
        return models
                .stream()
                .map(this::toEntity)
                .toList();
    }
}
