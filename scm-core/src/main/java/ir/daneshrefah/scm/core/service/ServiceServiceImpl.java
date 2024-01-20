package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.entity.service.JavaServiceEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.mapper.ServiceProviderMapper;
import ir.daneshrefah.scm.core.repository.ServiceProviderRepository;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

@RequiredArgsConstructor
@Service
public class ServiceServiceImpl implements ServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);

    private final ServiceRepository serviceRepository;
    private final ServiceRelationRepository serviceRelationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private List<ir.daneshrefah.scm.common.model.service.Service> services;
    private List<ExternalServiceProvider> serviceProviders;

    public List<ExternalServiceProvider> findServiceProviderList() {
        if (null == serviceProviders) {
            serviceProviders = ServiceProviderMapper.INSTANCE.toModels(serviceProviderRepository.findAll());
        }
        return serviceProviders;
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> findServiceList() {
        if (null == services) {
            services = ServiceMapper.INSTANCE.toServices(serviceRepository.findAll());
        }
        return services;
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service findServiceByCode(String code) {
        Optional<ServiceEntity> entity = serviceRepository.findByCode(code);
        if (entity.isEmpty())
            return null;
        return ServiceMapper.INSTANCE.toService(entity.get());
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service createService(ir.daneshrefah.scm.common.model.service.Service service) {
        if (StringUtils.isEmpty(service.getCode())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_CODE_IS_EMPTY, "service code is empty.");
        }
        if (StringUtils.isEmpty(service.getTitle())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_TITLE_IS_EMPTY, "service title is empty.");
        }
        if (null == service.getType()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_TYPE_IS_EMPTY, "service type is empty.");
        }
        if (ServiceImplementationType.JAVA.equals(service.getImplementationType()) &&
                StringUtils.isEmpty(((JavaService) service).getJavaImplementationClassName())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_JAVA_CLASS_IS_EMPTY, "service java class name is empty.");
        }
        if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType()) &&
                (null == ((ExternalService) service).getServiceProvider() || StringUtils.isEmpty(((ExternalService) service).getServiceProvider().getId()))) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_EMPTY, "service provider is empty.");
        }
        if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType()) &&
                !checkServiceProviderExistById(((ExternalService) service).getServiceProvider().getId())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_INVALID, "service provider is invalid.");
        }
        if (null == service.getVersion()) {
            service.setVersion(1);
        }
        service.setIsSystemic(false);
        if (null == service.getStatus()) {
            service.setStatus(ServiceStatus.ACTIVE);
        }
        if (null == service.getCheckAccessFirstAuthentication())
            service.setCheckAccessFirstAuthentication(false);
        if (null == service.getCheckAccessSecondAuthentication())
            service.setCheckAccessSecondAuthentication(false);
        if (null == service.getCheckAccessService())
            service.setCheckAccessService(false);
        if (null == service.getCheckAccessAsset())
            service.setCheckAccessAsset(false);
        ServiceEntity entity = ServiceMapper.INSTANCE.toServiceEntity(service);
        return ServiceMapper.INSTANCE.toService(serviceRepository.save(entity));
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service updateService(String serviceId, ir.daneshrefah.scm.common.model.service.Service service) {
        Optional<ServiceEntity> entity = serviceRepository.findById(serviceId);
        if (entity.isEmpty()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_OBJECT_NOT_FOUND, "service entity not found.");
        }
        boolean isModified = false;
        ServiceEntity serviceEntity = entity.get();
        if (!serviceEntity.getImplementationType().equals(service.getImplementationType())) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_IMPLEMENTATION_TYPE_IS_INVALID,
                    "service implementation type is invalid.");
        }
        if (StringUtils.isNotEmpty(service.getTitle()) && !service.getTitle().equals(serviceEntity.getTitle())) {
            serviceEntity.setTitle(service.getTitle());
            isModified = true;
        }
        if (StringUtils.isNotEmpty(service.getAlias()) && !service.getAlias().equals(serviceEntity.getAlias())) {
            serviceEntity.setAlias(service.getAlias());
            isModified = true;
        }
        if (StringUtils.isNotEmpty(service.getMetadata()) && !service.getMetadata().equals(serviceEntity.getMetadata())) {
            serviceEntity.setMetadata(service.getMetadata());
            isModified = true;
        }
        if (null != service.getType() && !service.getType().equals(serviceEntity.getType())) {
            serviceEntity.setType(service.getType());
            isModified = true;
        }
        if (null != service.getStatus() && !service.getStatus().equals(serviceEntity.getStatus())) {
            serviceEntity.setStatus(service.getStatus());
            isModified = true;
        }
        if (StringUtils.isNotEmpty(service.getRequestJsonSchema()) && !service.getRequestJsonSchema().equals(serviceEntity.getRequestJsonSchema())) {
            serviceEntity.setRequestJsonSchema(service.getResponseJsonSchema());
            isModified = true;
        }
        if (StringUtils.isNotEmpty(service.getResponseJsonSchema()) && !service.getResponseJsonSchema().equals(serviceEntity.getResponseJsonSchema())) {
            serviceEntity.setResponseJsonSchema(service.getResponseJsonSchema());
            isModified = true;
        }
        if (null != service.getCheckAccessFirstAuthentication()) {
            serviceEntity.setCheckAccessFirstAuthentication(service.getCheckAccessFirstAuthentication());
            isModified = true;
        }
        if (null != service.getCheckAccessSecondAuthentication()) {
            serviceEntity.setCheckAccessSecondAuthentication(service.getCheckAccessSecondAuthentication());
            isModified = true;
        }
        if (null != service.getCheckAccessService()) {
            serviceEntity.setCheckAccessService(service.getCheckAccessService());
            isModified = true;
        }
        if (null != service.getCheckAccessAsset()) {
            serviceEntity.setCheckAccessAsset(service.getCheckAccessAsset());
            isModified = true;
        }
//        private Integer version;
//        private ir.daneshrefah.scm.common.model.service.Service parent;
//        private ServiceImplementationType implementationType;
        if (StringUtils.isNotEmpty(service.getAmountProperty()) && !service.getAmountProperty().equals(serviceEntity.getAmountProperty())) {
            serviceEntity.setAmountProperty(service.getAmountProperty());
            isModified = true;
        }
        if (StringUtils.isNotEmpty(service.getAssetProperty()) && !service.getAssetProperty().equals(serviceEntity.getAssetProperty())) {
            serviceEntity.setAssetProperty(service.getAssetProperty());
            isModified = true;
        }
        if (serviceEntity instanceof JavaServiceEntity && StringUtils.isNotEmpty(((JavaService) service).getJavaImplementationClassName()) &&
                !((JavaService) service).getJavaImplementationClassName().equals(((JavaServiceEntity) serviceEntity).getJavaImplementationClassName())) {
            ((JavaServiceEntity) serviceEntity).setAssetProperty(((JavaService) service).getJavaImplementationClassName());
            isModified = true;
        }
        if (!isModified) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_NO_CHANGE, "object has no change.");
        }
//        private ExternalServiceProvider serviceProvider;

        return ServiceMapper.INSTANCE.toService(serviceRepository.save(serviceEntity));
    }

    @Override
    public boolean checkServiceExistById(String serviceId) {
        if (StringUtils.isEmpty(serviceId)) {
            return false;
        }
        return findServiceList().stream().anyMatch(service -> serviceId.equals(service.getId()));
    }

    @Override
    public boolean checkServiceProviderExistById(String serviceProviderId) {
        if (StringUtils.isEmpty(serviceProviderId)) {
            return false;
        }
        return findServiceProviderList().stream().anyMatch(serviceProvider -> serviceProviderId.equals(serviceProvider.getId()));
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> findCallableServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findCallableServiceList();
        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
        return services;
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> findParentServiceList() {
        Iterable<ServiceEntity> serviceEntities = serviceRepository.findServiceListByImplementationType(ServiceImplementationType.PARENT);
        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
        return services;
    }

    public List<ServiceRelation> findServiceRelationListBySourceServiceId(String sourceServiceId, ServiceRelationType relationType) {
        Iterable<ServiceRelationEntity> relationEntities = serviceRelationRepository.findAllBySourceServiceIdAndRelationType(sourceServiceId, relationType);
        List<ServiceRelation> relations = ServiceMapper.INSTANCE.relationEntitiesToModels(relationEntities);
        return relations;
    }

}
