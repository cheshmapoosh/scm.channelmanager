package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.CustomExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalServiceProvider;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.CustomExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.RestExternalServiceProviderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
@Mapper
public interface ServiceProviderMapper {

    ServiceProviderMapper INSTANCE = Mappers.getMapper(ServiceProviderMapper.class);

    @Mapping(source = "responseConditions",target = "responseConditions",qualifiedByName = "toResponseConditionsModel")
    @Mapping(source = "parameters", target = "requestHeaders",qualifiedByName = "toRequestHeadersModel")
    @Mapping(source = "parameters", target = "requestBody",qualifiedByName = "toRequestBodyModel")
    @Mapping(source = "parameters", target = "responseHeaders",qualifiedByName = "toResponseHeadersModel")
    @Mapping(source = "parameters", target = "configParameter",qualifiedByName = "toConfigParameter")
    RestExternalServiceProvider toModel(RestExternalServiceProviderEntity entity);

    @Mapping(source = "responseConditions",target = "responseConditions",qualifiedByName = "toResponseConditionsModel")
    @Mapping(source = "parameters", target = "requestHeaders",qualifiedByName = "toRequestHeadersModel")
    @Mapping(source = "parameters", target = "requestBody",qualifiedByName = "toRequestBodyModel")
    @Mapping(source = "parameters", target = "responseHeaders",qualifiedByName = "toResponseHeadersModel")
    @Mapping(source = "parameters", target = "configParameter",qualifiedByName = "toConfigParameter")
    CustomExternalServiceProvider toModel(CustomExternalServiceProviderEntity entity);

    @Mapping(source = "responseConditions",target = "responseConditions",qualifiedByName = "toResponseConditionsEntity")
    @Mapping(target = "parameters" ,ignore = true)
    RestExternalServiceProviderEntity toEntityWithoutParameter(RestExternalServiceProvider model);

    @Mapping(source = "responseConditions",target = "responseConditions",qualifiedByName = "toResponseConditionsEntity")
    @Mapping(target = "parameters" ,ignore = true)
    CustomExternalServiceProviderEntity toEntityWithoutParameter(CustomExternalServiceProvider model);

    default RestExternalServiceProviderEntity toEntity(RestExternalServiceProvider model){
        RestExternalServiceProviderEntity entity = toEntityWithoutParameter(model);
        List<ParameterEntity> parameterEntities = new ArrayList<>();
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getRequestBody()));
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getRequestHeaders()));
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getResponseHeaders()));
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getConfigParameter()));
        entity.setParameters(parameterEntities);
        return entity;
    }

    default CustomExternalServiceProviderEntity toEntity(CustomExternalServiceProvider model){
        CustomExternalServiceProviderEntity entity = toEntityWithoutParameter(model);
        List<ParameterEntity> parameterEntities = new ArrayList<>();
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getRequestBody()));
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getRequestHeaders()));
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getResponseHeaders()));
        parameterEntities.addAll(ParameterMapper.INSTANCE.toEntityList(model.getConfigParameter()));
        entity.setParameters(parameterEntities);
        return entity;
    }

    default List<AbstractExternalServiceProvider> toModels(Iterable<AbstractExternalServiceProviderEntity> entities) {
        if (Objects.isNull(entities)) {
            return null;
        }
        List<AbstractExternalServiceProvider> result = new ArrayList<>();
        for (Iterator<AbstractExternalServiceProviderEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
            AbstractExternalServiceProviderEntity entity = iterator.next();
            result.add(toServiceProvider(entity));
        }
        return result;
    }

    @Named("toServiceProvider")
    default AbstractExternalServiceProvider toServiceProvider(AbstractExternalServiceProviderEntity entity) {
        if (entity instanceof RestExternalServiceProviderEntity restExternalServiceProviderEntity) {
            return toModel(restExternalServiceProviderEntity);
        } else if (entity instanceof CustomExternalServiceProviderEntity customExternalServiceProviderEntity) {
            return toModel(customExternalServiceProviderEntity);
        }
        return null;
    }

    @Named("toServiceProviderEntity")
    default AbstractExternalServiceProviderEntity toServiceProviderEntity(AbstractExternalServiceProvider model) {
        if (model instanceof RestExternalServiceProvider restExternalServiceProvider) {
            return toEntity(restExternalServiceProvider);
        } else if (model instanceof CustomExternalServiceProvider customExternalServiceProvider) {
            return toEntity(customExternalServiceProvider);
        }
        return null;
    }

    @Named("toResponseConditionsModel")
    default List<Response> toResponseConditionsModel(List<ResponseEntity> entities){
        if (Objects.nonNull(entities)) {
            return entities.stream().map(ResponseMapper.INSTANCE::toModel).toList();
        }
        return null;
    }

    @Named("toResponseConditionsEntity")
    default List<ResponseEntity> toResponseConditionsEntity(List<Response> models){
        if(Objects.nonNull(models)) {
            return models.stream().map(ResponseMapper.INSTANCE::toEntity).toList();
        }
        return null;
    }

    @Named("toRequestHeadersModel")
    default List<Parameter> toRequestHeadersModel(List<ParameterEntity> entities){
        if(Objects.nonNull(entities)) {
            return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_HEADER);
        }
        return null;
    }

    @Named("toRequestBodyModel")
    default List<Parameter> toRequestBodyModel(List<ParameterEntity> entities){
        if(Objects.nonNull(entities)) {
            return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_BODY);
        }
        return null;
    }

    @Named("toResponseHeadersModel")
    default List<Parameter> toResponseHeadersModel(List<ParameterEntity> entities){
        if(Objects.nonNull(entities)) {
            return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.RESPONSE_HEADER);
        }
        return null;
    }

    @Named("toConfigParameter")
    default List<Parameter> toConfigParameter(List<ParameterEntity> entities){
        if(Objects.nonNull(entities)) {
            return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.CONFIG);
        }
        return null;
    }

}
