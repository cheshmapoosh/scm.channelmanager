package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.external.CustomExternalService;
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
//    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProvider")
    CustomExternalService toModel(CustomExternalServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
//    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProvider")
    RestExternalService toModel(RestExternalServiceEntity entity);

//    List<AbstractExternalService> externalEntitiesToModels(Iterable<AbstractExternalServiceEntity> entities);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
//    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    CustomExternalServiceEntity toEntity(CustomExternalService model);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
//    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    RestExternalServiceEntity toEntity(RestExternalService model);

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
        if (serviceEntity instanceof JavaServiceEntity) {
            return toModel((JavaServiceEntity) serviceEntity);
        } else if (serviceEntity instanceof CustomExternalServiceEntity) {
            return toModel((CustomExternalServiceEntity) serviceEntity);
        } else if (serviceEntity instanceof RestExternalServiceEntity) {
            return toModel((RestExternalServiceEntity) serviceEntity);
        } else if (serviceEntity instanceof ParentServiceEntity) {
            return toModel((ParentServiceEntity) serviceEntity);
        } else if (serviceEntity instanceof CompositionServiceEntity) {
            return toModel((CompositionServiceEntity) serviceEntity);
        }
        return null;
    }

    @Named("toServiceEntity")
    default ServiceEntity toServiceEntity(Service service) {
        if (service instanceof JavaService) {
            return toEntity((JavaService) service);
        } else if (service instanceof CustomExternalService) {
            return toEntity((CustomExternalService) service);
        } else if (service instanceof RestExternalService) {
            return toEntity((RestExternalService) service);
        } else if (service instanceof ParentService) {
            return toEntity((ParentService) service);
        } else if (service instanceof CompositionService) {
            return toEntity((CompositionService) service);
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
    default AbstractExternalServiceProvider toServiceProvider(AbstractExternalServiceProviderEntity entity) {
        // Delegate the mapping to the method in ServiceMapper
        return ServiceProviderMapper.INSTANCE.toServiceProvider(entity);
    }

    @Named("toServiceProviderEntity")
    default AbstractExternalServiceProviderEntity toServiceProviderEntity(AbstractExternalServiceProvider model) {
        // Delegate the mapping to the method in ServiceMapper
        return ServiceProviderMapper.INSTANCE.toServiceProviderEntity(model);
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
