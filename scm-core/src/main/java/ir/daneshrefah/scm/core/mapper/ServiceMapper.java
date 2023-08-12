package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.limitation.ServiceLimitationEntity;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.plugin.api.model.limitation.ServiceLimitation;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
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

    JavaService toModel(JavaServiceEntity entity);

    List<JavaService> javaEntitiesToModels(Iterable<JavaServiceEntity> entities);

    JavaServiceEntity toEntity(JavaService model);

    @Mapping(source = "externalServiceProviderEntity", target = "serviceProvider")
    ExternalService toModel(ExternalServiceEntity entity);
    List<ExternalService> externalEntitiesToModels(Iterable<ExternalServiceEntity> entities);

    ExternalServiceEntity toEntity(ExternalService model);

    CompositionService toModel(CompositionServiceEntity entity);

    CompositionServiceEntity toEntity(CompositionService model);

    ParentService toModel(ParentServiceEntity entity);

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

    @Mapping(source = "service", target = "service", qualifiedByName = "toService")
    @Mapping(source = "terminalServiceAccess", target = "terminalServiceAccess")
    @Mapping(source = "terminalServiceAccess.terminalEntity", target = "terminalServiceAccess.terminal")
    @Mapping(source = "terminalServiceAccess.serviceEntity", target = "terminalServiceAccess.service", qualifiedByName = "toService")
    @Mapping(source = "terminalServiceChannelAccess", target = "terminalServiceChannelAccess")
    @Mapping(source = "terminalServiceChannelAccess.terminalServiceAccessEntity", target = "terminalServiceChannelAccess.terminalServiceAccess")
    @Mapping(source = "terminalServiceChannelAccess.terminalServiceAccessEntity.terminalEntity", target = "terminalServiceChannelAccess.terminalServiceAccess.terminal")
    @Mapping(source = "terminalServiceChannelAccess.terminalServiceAccessEntity.serviceEntity", target = "terminalServiceChannelAccess.terminalServiceAccess.service", qualifiedByName = "toService")
    ServiceLimitation toModel(ServiceLimitationEntity entity);
    List<ServiceLimitation> limitationEntitiesToModels(Iterable<ServiceLimitationEntity> entities);

}
