package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ScmService;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING,
        uses = {ServiceProviderMapper.class})
public abstract class ScmServiceMapper {

    @Autowired
    private ServiceProviderMapper serviceProviderMapper;


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(target = "javaImplementationClassName", ignore = true)
    @Mapping(target = "noneEditableProperties", ignore = true)
    @Mapping(target = "implemented", ignore = true)
    public abstract JavaService toModel(JavaServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "serviceProvider", target = "serviceProvider")
    public abstract CustomExternalService toModel(CustomExternalServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(source = "serviceProvider", target = "serviceProvider")
    public abstract RestExternalService toModel(RestExternalServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    @Mapping(target = "relations", ignore = true)
    public abstract CompositionService toModel(CompositionServiceEntity entity);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toService")
    public abstract ParentService toModel(ParentServiceEntity entity);

    @Mapping(source = "sourceService", target = "sourceService", qualifiedByName = "toService")
    @Mapping(source = "targetService", target = "targetService", qualifiedByName = "toService")
    @Mapping(source = "targetServiceCommit", target = "targetServiceCommit", qualifiedByName = "toService")
    @Mapping(source = "targetServiceReverse", target = "targetServiceReverse", qualifiedByName = "toService")
    @Mapping(target = "targetServiceRequestTransformers", ignore = true)
    @Mapping(target = "targetServiceResponseTransformers", ignore = true)
    @Mapping(target = "targetServiceCommitRequestTransformers", ignore = true)
    @Mapping(target = "targetServiceCommitResponseTransformers", ignore = true)
    @Mapping(target = "targetServiceReverseRequestTransformers", ignore = true)
    @Mapping(target = "targetServiceReverseResponseTransformers", ignore = true)
    public abstract ServiceRelation toModel(ServiceRelationEntity entity);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    public abstract JavaServiceEntity toEntity(JavaService model);


//    List<AbstractExternalService> externalEntitiesToModels(Iterable<AbstractExternalServiceEntity> entities);

    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    public abstract CustomExternalServiceEntity toEntity(CustomExternalService model);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    @Mapping(source = "serviceProvider", target = "serviceProvider", qualifiedByName = "toServiceProviderEntity")
    public abstract RestExternalServiceEntity toEntity(RestExternalService model);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    public abstract CompositionServiceEntity toEntity(CompositionService model);


    @Mapping(source = "parent", target = "parent", qualifiedByName = "toServiceEntity")
    public abstract ParentServiceEntity toEntity(ParentService model);


    public abstract List<ServiceRelation> relationEntitiesToModels(Iterable<ServiceRelationEntity> entities);


    @Named("toService")
    public ScmService toService(ScmServiceEntity serviceEntity) {
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
        }
        return null;
    }

    @Named("toServiceRelationModel")
    public ServiceRelation toServiceRelationModel(ServiceRelationEntity entity) {
        return toModel(entity);
    }


    @Named("toServiceEntity")
    public ScmServiceEntity toServiceEntity(ScmService service) {
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
    public List<ScmService> toServices(Iterable<ScmServiceEntity> serviceEntities) {
        if (null == serviceEntities)
            return null;
        List<ScmService> result = new ArrayList<>();
        for (ScmServiceEntity serviceEntity : serviceEntities) {
            ScmService service = toService(serviceEntity);
            result.add(service);
        }
        return result;
    }


    @Named("toServiceProvider")
    @SuppressWarnings("unchecked")
    public <E extends AbstractExternalServiceProviderEntity, M extends AbstractAuditableExternalServiceProvider> M toServiceProvider(E entity) {
        // Delegate the mapping to the method in ServiceMapper
        return (M) serviceProviderMapper.toServiceProvider(entity);
    }


    @Named("toServiceProviderEntity")
    @SuppressWarnings("unchecked")
    public <E extends AbstractExternalServiceProviderEntity, M extends AbstractAuditableExternalServiceProvider> E toServiceProviderEntity(M model) {
        // Delegate the mapping to the method in ServiceMapper
        return (E) serviceProviderMapper.toServiceProviderEntity(model);
    }


}
