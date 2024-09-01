package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseConditionEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.external.CustomExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ProxyService;
import ir.daneshrefah.scm.plugin.api.model.service.external.rest.RestExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    JavaService toModel(JavaServiceEntity entity);

    List<JavaService> javaEntitiesToModels(Iterable<JavaServiceEntity> entities);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    JavaServiceEntity toEntity(JavaService model);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "parameters", target = "requestHeaders",qualifiedByName = "toRequestHeadersModel")
    @Mapping(source = "parameters", target = "requestBody",qualifiedByName = "toRequestBodyModel")
    @Mapping(source = "parameters", target = "responseHeaders",qualifiedByName = "toResponseHeadersModel")
    @Mapping(source = "responseConditions",target = "responseConditions" ,qualifiedByName = "toResponseConditionModel")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProvider")
    CustomExternalService toModel(CustomExternalServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "parameters", target = "requestHeaders",qualifiedByName = "toRequestHeadersModel")
    @Mapping(source = "parameters", target = "requestBody",qualifiedByName = "toRequestBodyModel")
    @Mapping(source = "parameters", target = "responseHeaders",qualifiedByName = "toResponseHeadersModel")
    @Mapping(source = "responseConditions",target = "responseConditions" ,qualifiedByName = "toResponseConditionModel")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProvider")
    @Mapping(source = "parameters",target = "requestPathVariables" , qualifiedByName = "toPathVariablesModel")
    @Mapping(source = "parameters",target = "requestQueryStringVariables" , qualifiedByName = "toQueryStringVariablesModel")
    RestExternalService toModel(RestExternalServiceEntity entity);

//    List<AbstractExternalService> externalEntitiesToModels(Iterable<AbstractExternalServiceEntity> entities);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(target = "parameters", ignore = true)
    @Mapping(source = "responseConditions",target = "responseConditions" ,qualifiedByName = "toResponseConditionEntity")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    CustomExternalServiceEntity toEntityWithoutParameters(CustomExternalService model);

    default CustomExternalServiceEntity toEntity(CustomExternalService model){
        List<Parameter> allParameters = new ArrayList<>();
        allParameters.addAll(model.getRequestHeaders());
        allParameters.addAll(model.getRequestBody());
        allParameters.addAll(model.getResponseHeaders());
        List<ParameterEntity> parameterEntities = ParameterMapper.INSTANCE.toEntityList(allParameters);
        CustomExternalServiceEntity entity = this.toEntityWithoutParameters(model);
        entity.setParameters(parameterEntities);
        return entity;
    }


    @Mapping(source = "parameters",target = "parameters" , qualifiedByName = "toParameterModel")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(target = "targetService",ignore = true)
    ProxyService toModel(ProxyServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(target = "parameters", ignore = true)
    @Mapping(source = "responseConditions",target = "responseConditions" ,qualifiedByName = "toResponseConditionEntity")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    RestExternalServiceEntity toEntityWithoutParameters(RestExternalService model);

    @Named("toResponseConditionEntity")
    default List<ResponseConditionEntity> toResponseConditionEntity(List<ResponseCondition> conditions){
        return conditions.stream().map(ResponseConditionMapper.INSTANCE::toEntity).toList();
    }

    @Named("toResponseConditionModel")
    default List<ResponseCondition> toResponseConditionModel(List<ResponseConditionEntity> conditions){
        return conditions.stream().map(ResponseConditionMapper.INSTANCE::toModel).toList();
    }

    @Named("toParameterModel")
    default List<Parameter> toParameterModel(List<ParameterEntity> entities){
        return entities
                .stream()
                .map(ParameterMapper.INSTANCE::toModel)
                .toList();
    }

    default RestExternalServiceEntity toEntity(RestExternalService model){
        List<Parameter> allParameters = new ArrayList<>();
        allParameters.addAll(model.getRequestHeaders());
        allParameters.addAll(model.getRequestBody());
        allParameters.addAll(model.getResponseHeaders());
        allParameters.addAll(model.getRequestPathVariables());
        allParameters.addAll(model.getRequestQueryStringVariables());
        List<ParameterEntity> parameterEntities = ParameterMapper.INSTANCE.toEntityList(allParameters);
        RestExternalServiceEntity entity = this.toEntityWithoutParameters(model);
        entity.setParameters(parameterEntities);
        return entity;
    }

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    CompositionService toModel(CompositionServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    CompositionServiceEntity toEntity(CompositionService model);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    ParentService toModel(ParentServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    ParentServiceEntity toEntity(ParentService model);

    @Named("toService")
    default Service toService(ServiceEntity serviceEntity) {
        if (serviceEntity instanceof JavaServiceEntity javaServiceEntity) {
            return toModel(javaServiceEntity);
        } else if (serviceEntity instanceof CustomExternalServiceEntity customExternalServiceEntity) {
            return toModel(customExternalServiceEntity);
        } else if (serviceEntity instanceof RestExternalServiceEntity restExternalServiceEntity) {
            return toModel(restExternalServiceEntity);
        } else if (serviceEntity instanceof ParentServiceEntity parentServiceEntity) {
            return toModel(parentServiceEntity);
        } else if (serviceEntity instanceof CompositionServiceEntity compositionServiceEntity) {
            return toModel(compositionServiceEntity);
        }else if (serviceEntity instanceof ProxyServiceEntity proxyExternalServiceEntity){
            return toModel(proxyExternalServiceEntity);
        }
        return null;
    }

    @Named("toServiceRelationModel")
    default ServiceRelation toServiceRelationModel(ServiceRelationEntity entity){
        return toModel(entity);
    }


    @Named("toServiceEntity")
    default ServiceEntity toServiceEntity(Service service) {
        if (service instanceof JavaService javaService) {
            return toEntity(javaService);
        } else if (service instanceof CustomExternalService customExternalService) {
            return toEntity(customExternalService);
        } else if (service instanceof RestExternalService restExternalService) {
            return toEntity(restExternalService);
        } else if (service instanceof ParentService parentService) {
            return toEntity(parentService);
        } else if (service instanceof CompositionService compositionService) {
            return toEntity(compositionService);
        }
        return null;
    }

    @Named("toServices")
    default List<Service> toServices(Iterable<ServiceEntity> serviceEntities) {
        if (null == serviceEntities)
            return null;
        List<Service> result = new ArrayList<>();
        for (Iterator<ServiceEntity> iterator = serviceEntities.iterator(); iterator.hasNext(); ) {
            ServiceEntity serviceEntity = iterator.next();
            Service service = toService(serviceEntity);
            result.add(service);
        }
        return result;
    }

    @Mapping(source = "sourceService", target = "sourceService", qualifiedByName = "toService")
    @Mapping(source = "targetService", target = "targetService", qualifiedByName = "toService")
    @Mapping(source = "targetServiceCommit", target = "targetServiceCommit", qualifiedByName = "toService")
    @Mapping(source = "targetServiceReverse", target = "targetServiceReverse", qualifiedByName = "toService")
    ServiceRelation toModel(ServiceRelationEntity entity);

    List<ServiceRelation> relationEntitiesToModels(Iterable<ServiceRelationEntity> entities);

    @Named("toServiceProvider")
    @SuppressWarnings("unchecked")
    default <E extends AbstractExternalServiceProviderEntity,M extends AbstractExternalServiceProvider> M toServiceProvider(E entity) {
        // Delegate the mapping to the method in ServiceMapper
        return (M) ServiceProviderMapper.INSTANCE.toServiceProvider(entity);
    }


    @Named("toServiceProviderEntity")
    @SuppressWarnings("unchecked")
    default <E extends AbstractExternalServiceProviderEntity,M extends AbstractExternalServiceProvider> E toServiceProviderEntity(M model) {
        // Delegate the mapping to the method in ServiceMapper
        return (E) ServiceProviderMapper.INSTANCE.toServiceProviderEntity(model);
    }

    @Named("toRequestHeadersModel")
    default List<Parameter> toRequestHeadersModel(List<ParameterEntity> entities){
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_HEADER);
    }

    @Named("toRequestBodyModel")
    default List<Parameter> toRequestBodyModel(List<ParameterEntity> entities){
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_BODY);
    }

    @Named("toQueryStringVariablesModel")
    default List<Parameter> toQueryStringVariablesModel(List<ParameterEntity> entities){
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_QUERY_STRING);
    }

    @Named("toPathVariablesModel")
    default List<Parameter> toPathVariablesModel(List<ParameterEntity> entities){
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_PATH_VARIABLE);
    }

    @Named("toResponseHeadersModel")
    default List<Parameter> toResponseHeadersModel(List<ParameterEntity> entities){
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.RESPONSE_HEADER);
    }


//    @Mapping(source = "service", target = "service", qualifiedByName = "toService")
//    @Mapping(source = "terminalServiceAccess", target = "terminalServiceAccess")
//    @Mapping(source = "terminalServiceAccess.terminalEntity", target = "terminalServiceAccess.terminal")
//    @Mapping(source = "terminalServiceAccess.serviceEntity", target = "terminalServiceAccess.service", qualifiedByName = "toService")
//    @Mapping(source = "terminalServiceChannelAccess", target = "terminalServiceChannelAccess")
//    @Mapping(source = "terminalServiceChannelAccess.terminalServiceAccessEntity", target = "terminalServiceChannelAccess.terminalServiceAccess")
//    @Mapping(source = "terminalServiceChannelAccess.terminalServiceAccessEntity.terminalEntity", target = "terminalServiceChannelAccess.terminalServiceAccess.terminal")
//    @Mapping(source = "terminalServiceChannelAccess.terminalServiceAccessEntity.serviceEntity", target = "terminalServiceChannelAccess.terminalServiceAccess.service", qualifiedByName = "toService")
//    ServiceLimitation toModel(ServiceLimitationEntity entity);
//    List<ServiceLimitation> limitationEntitiesToModels(Iterable<ServiceLimitationEntity> entities);

}
