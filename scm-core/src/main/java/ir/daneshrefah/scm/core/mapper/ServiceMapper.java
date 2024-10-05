package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
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
import java.util.Objects;

@Mapper
public interface ServiceMapper {
    ServiceMapper INSTANCE = Mappers.getMapper(ServiceMapper.class);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersModel")
    JavaService toModel(JavaServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "responseList", target = "responseList", qualifiedByName = "toResponseConditionModel")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProvider")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersModel")
    CustomExternalService toModel(CustomExternalServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "responseList", target = "responseList", qualifiedByName = "toResponseConditionModel")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProvider")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersModel")
    RestExternalService toModel(RestExternalServiceEntity entity);

    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersModel")
    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(target = "targetService", ignore = true)
    ProxyService toModel(ProxyServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersModel")
    CompositionService toModel(CompositionServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersModel")
    ParentService toModel(ParentServiceEntity entity);

    @Mapping(source = "sourceService", target = "sourceService", qualifiedByName = "toService")
    @Mapping(source = "targetService", target = "targetService", qualifiedByName = "toService")
    @Mapping(source = "targetServiceCommit", target = "targetServiceCommit", qualifiedByName = "toService")
    @Mapping(source = "targetServiceReverse", target = "targetServiceReverse", qualifiedByName = "toService")
    ServiceRelation toModel(ServiceRelationEntity entity);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersEntities")
    JavaServiceEntity toEntity(JavaService model);


//    List<AbstractExternalService> externalEntitiesToModels(Iterable<AbstractExternalServiceEntity> entities);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersEntities")
    @Mapping(source = "responseList", target = "responseList", qualifiedByName = "toResponseConditionEntity")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    CustomExternalServiceEntity toEntity(CustomExternalService model);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersEntities")
    ProxyServiceEntity toEntity(ProxyService model);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersEntities")
    @Mapping(source = "responseList", target = "responseList", qualifiedByName = "toResponseConditionEntity")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    RestExternalServiceEntity toEntity(RestExternalService model);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersEntities")
    CompositionServiceEntity toEntity(CompositionService model);



    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "parameters", target = "parameters", qualifiedByName = "toParametersEntities")
    ParentServiceEntity toEntity(ParentService model);



    List<ServiceRelation> relationEntitiesToModels(Iterable<ServiceRelationEntity> entities);


    @Named("toParametersModel")
    default List<Parameter> toParametersModel(List<ParameterEntity> entities) {
        if (Objects.nonNull(entities)) {
            return ParameterMapper
                    .INSTANCE
                    .toModelList(entities);
        }
        return null;
    }

    @Named("toParametersEntities")
    default List<ParameterEntity> toParametersEntities(List<Parameter> models) {
        if (Objects.nonNull(models)) {
            return ParameterMapper
                    .INSTANCE
                    .toEntityList(models);
        }
        return null;
    }

    @Named("toResponseConditionEntity")
    default List<ResponseEntity> toResponseConditionEntity(List<Response> conditions) {
        return conditions.stream().map(ResponseMapper.INSTANCE::toEntity).toList();
    }

    @Named("toResponseConditionModel")
    default List<Response> toResponseConditionModel(List<ResponseEntity> conditions) {
        return conditions.stream().map(ResponseMapper.INSTANCE::toModel).toList();
    }


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
        } else if (serviceEntity instanceof ProxyServiceEntity proxyExternalServiceEntity) {
            return toModel(proxyExternalServiceEntity);
        }
        return null;
    }

    @Named("toServiceRelationModel")
    default ServiceRelation toServiceRelationModel(ServiceRelationEntity entity) {
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
        } else if ((service instanceof ProxyService proxyService)) {
            return toEntity(proxyService);
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



    @Named("toServiceProvider")
    @SuppressWarnings("unchecked")
    default <E extends AbstractExternalServiceProviderEntity, M extends AbstractExternalServiceProvider> M toServiceProvider(E entity) {
        // Delegate the mapping to the method in ServiceMapper
        return (M) ServiceProviderMapper.INSTANCE.toServiceProvider(entity);
    }


    @Named("toServiceProviderEntity")
    @SuppressWarnings("unchecked")
    default <E extends AbstractExternalServiceProviderEntity, M extends AbstractExternalServiceProvider> E toServiceProviderEntity(M model) {
        // Delegate the mapping to the method in ServiceMapper
        return (E) ServiceProviderMapper.INSTANCE.toServiceProviderEntity(model);
    }

    @Named("toRequestHeadersModel")
    default List<Parameter> toRequestHeadersModel(List<ParameterEntity> entities) {
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_HEADER);
    }

    @Named("toRequestBodyModel")
    default List<Parameter> toRequestBodyModel(List<ParameterEntity> entities) {
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_BODY);
    }

    @Named("toQueryStringVariablesModel")
    default List<Parameter> toQueryStringVariablesModel(List<ParameterEntity> entities) {
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_QUERY_STRING);
    }

    @Named("toPathVariablesModel")
    default List<Parameter> toPathVariablesModel(List<ParameterEntity> entities) {
        return ParameterMapper.INSTANCE.toModelList(entities, ParameterActionType.REQUEST_PATH_VARIABLE);
    }

    @Named("toResponseHeadersModel")
    default List<Parameter> toResponseHeadersModel(List<ParameterEntity> entities) {
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
