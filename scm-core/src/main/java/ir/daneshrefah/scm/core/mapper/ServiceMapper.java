package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.JavaServiceEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
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
    @Mapping(source = "externalServiceProviderEntity", target = "serviceProvider")
    ExternalService toModel(ExternalServiceEntity entity);
    List<ExternalService> externalEntitiesToModels(Iterable<ExternalServiceEntity> entities);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    ExternalServiceEntity toEntity(ExternalService model);

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
        } else if (serviceEntity instanceof ExternalServiceEntity) {
            return toModel((ExternalServiceEntity) serviceEntity);
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
        } else if (service instanceof ExternalService) {
            return toEntity((ExternalService) service);
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
