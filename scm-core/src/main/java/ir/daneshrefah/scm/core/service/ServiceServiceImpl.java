package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoDataChangedException;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.service.ServiceFindRequest;
import ir.daneshrefah.scm.common.service.ServiceInfoRequest;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.entity.service.ExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.service.JavaServiceEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntityFactory;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.mapper.ServiceProviderMapper;
import ir.daneshrefah.scm.core.repository.ServiceProviderRepository;
import ir.daneshrefah.scm.core.repository.ServiceRelationRepository;
import ir.daneshrefah.scm.core.repository.ServiceRepository;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ServiceServiceImpl implements ServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);

    private final ServiceRepository serviceRepository;
    private final ServiceRelationRepository serviceRelationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private List<ir.daneshrefah.scm.common.model.service.Service> services;
    private List<ExternalServiceProvider> serviceProviders;

    @Override
    public List<ExternalServiceProvider> findServiceProviderList() {
        if (null == serviceProviders) {
            serviceProviders = ServiceProviderMapper.INSTANCE.toModels(serviceProviderRepository.findAll());
        }
        return serviceProviders;
    }

    @Override
    public ExternalServiceProvider findServiceProviderById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return findServiceProviderList().stream().filter(serviceProvider -> id.equals(serviceProvider.getId())).findFirst().orElse(null);
    }

    @Override
    public ExternalServiceProvider findServiceProviderByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findServiceProviderList().stream().filter(serviceProvider -> code.equalsIgnoreCase(serviceProvider.getCode())).findFirst().orElse(null);
    }

    @Override
    public ExternalServiceProvider findServiceProviderByIdOrCode(String value) {
        ExternalServiceProvider provider = findServiceProviderById(value);
        if (null != provider) {
            return provider;
        }
        return findServiceProviderByCode(value);
    }

    @Override
    public List<ir.daneshrefah.scm.common.model.service.Service> findServiceList() {
        if (null == services) {
            services = ServiceMapper.INSTANCE.toServices(serviceRepository.findAll());
        }
        return services;
    }

    @Override
    public PagedResponseData<ir.daneshrefah.scm.common.model.service.Service> findServiceList(ServiceFindRequest request) {
        List<ir.daneshrefah.scm.common.model.service.Service> serviceList = findServiceList().stream()
                .filter(service -> null == request || null == request.getCode() || request.getCode().equals(service.getCode()))
                .filter(service -> null == request || null == request.getIsSystemic() || request.getIsSystemic().equals(service.getIsSystemic()))
                .filter(service -> null == request || null == request.getType() || request.getType().equals(service.getType()))
                .filter(service -> null == request || null == request.getStatus() || request.getStatus().equals(service.getStatus()))
//                .filter(service -> null == request || null == request.getParentId() || request.getStatus().equals(service.getStatus()))
                .filter(service -> null == request || null == request.getImplementationType() || request.getImplementationType().equals(service.getImplementationType()))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, serviceList);
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service findServiceByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findServiceList().stream().filter(service -> code.equals(service.getCode())).findFirst().orElse(null);
//        Optional<ServiceEntity> entity = serviceRepository.findByCode(code);
//        if (entity.isEmpty())
//            return null;
//        return ServiceMapper.INSTANCE.toService(entity.get());
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service findServiceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return findServiceList().stream().filter(service -> id.equals(service.getId())).findFirst().orElse(null);
    }

    public ParentService findParentServiceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        ir.daneshrefah.scm.common.model.service.Service result = findServiceList().stream().filter(service -> id.equals(service.getId())).findFirst().orElse(null);
        if (null != result && result instanceof ParentService) {
            return (ParentService) result;
        }
        return null;
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service createService(ServiceInfoRequest service) {
        if (StringUtils.isEmpty(service.getCode())) {
            throw new MissingRequiredInputException("service code");
        }
        if (StringUtils.isEmpty(service.getTitle())) {
            throw new MissingRequiredInputException("service title");
        }
        if (null == service.getType()) {
            throw new MissingRequiredInputException("service type");
        }
        if (ServiceImplementationType.JAVA.equals(service.getImplementationType()) &&
                StringUtils.isEmpty(service.getJavaImplementationClassName())) {
            throw new MissingRequiredInputException("javaImplementationClassName");
        }
        if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType()) &&
                (StringUtils.isEmpty(service.getServiceProviderId()))) {
            throw new MissingRequiredInputException("serviceProvider");
        }
        if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType()) &&
                !checkServiceProviderExistById(service.getServiceProviderId())) {
            throw new InvalidInputException("serviceProvider");
        }

        ServiceEntity entity = ServiceEntityFactory.createServiceEntity(service);

        entity.setParent(serviceRepository.findById(service.getParentId()).get());
        if (entity instanceof ExternalServiceEntity) {
            ((ExternalServiceEntity) entity).setServiceProvider(serviceProviderRepository.findById(service.getServiceProviderId()).get());
        }

        ir.daneshrefah.scm.common.model.service.Service result = ServiceMapper.INSTANCE.toService(serviceRepository.save(entity));
        emptyServiceListCache();
        return result;
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service updateService(String serviceId, ir.daneshrefah.scm.common.model.service.Service service) {
        Optional<ServiceEntity> entity = serviceRepository.findById(serviceId);
        if (entity.isEmpty()) {
            throw new InvalidInputException("serviceId");
        }
        boolean isModified = false;
        ServiceEntity serviceEntity = entity.get();
        if (!serviceEntity.getImplementationType().equals(service.getImplementationType())) {
            throw new InvalidInputException("implementationType");
        }
        if (StringUtils.isNotEmpty(service.getTitle()) && !service.getTitle().equals(serviceEntity.getTitle())) {
            serviceEntity.setTitle(service.getTitle());
            isModified = true;
        }
        if (null != service.getAlias() && !service.getAlias().equals(serviceEntity.getAlias())) {
            serviceEntity.setAlias(service.getAlias());
            isModified = true;
        }
        if (null != service.getMetadata() && !service.getMetadata().equals(serviceEntity.getMetadata())) {
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
        ParentService parentService = null;
        if (null != service.getParent() && StringUtils.isNotEmpty(service.getParent().getId())) {
            parentService = findParentServiceById(service.getParent().getId());
        }
        if (null == parentService && null != service.getParent() && StringUtils.isNotEmpty(service.getParent().getId())) {
            throw new InvalidInputException("parentId");
        }
        if (null != parentService &&
                null != serviceEntity.getParent() && !StringUtils.equals(parentService.getId(), serviceEntity.getParent().getId())) {
            serviceEntity.setParent(ServiceEntityFactory.createEmptyServiceEntity(service.getParent().getId(), ServiceImplementationType.PARENT));
            isModified = true;
        }
        if (null != parentService && null == serviceEntity.getParent()) {
            serviceEntity.setParent(ServiceEntityFactory.createEmptyServiceEntity(service.getParent().getId(), ServiceImplementationType.PARENT));
            isModified = true;
        }
        if (StringUtils.isNotEmpty(service.getAssetProperty()) && !service.getAssetProperty().equals(serviceEntity.getAssetProperty())) {
            serviceEntity.setAssetProperty(service.getAssetProperty());
            isModified = true;
        }
        if (serviceEntity instanceof JavaServiceEntity && StringUtils.isNotEmpty(((JavaService) service).getJavaImplementationClassName()) &&
                !((JavaService) service).getJavaImplementationClassName().equals(((JavaServiceEntity) serviceEntity).getJavaImplementationClassName())) {
            ((JavaServiceEntity) serviceEntity).setJavaImplementationClassName(((JavaService) service).getJavaImplementationClassName());
            isModified = true;
        }
        if (!isModified) {
            throw new NoDataChangedException("service");
        }
//        private ExternalServiceProvider serviceProvider;

        ir.daneshrefah.scm.common.model.service.Service result = ServiceMapper.INSTANCE.toService(serviceRepository.save(serviceEntity));
        emptyServiceListCache();
        return result;
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
        return findServiceList().stream().filter(service -> !ServiceImplementationType.PARENT.equals(service.getImplementationType()))
                .collect(Collectors.toList());
//        Iterable<ServiceEntity> serviceEntities = serviceRepository.findCallableServiceList();
//        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
//        return services;
    }

    public List<ir.daneshrefah.scm.common.model.service.Service> findParentServiceList() {
        return findServiceList().stream().filter(service -> ServiceImplementationType.PARENT.equals(service.getImplementationType()))
                .collect(Collectors.toList());
//        Iterable<ServiceEntity> serviceEntities = serviceRepository.findServiceListByImplementationType(ServiceImplementationType.PARENT);
//        List<ir.daneshrefah.scm.common.model.service.Service> services = ServiceMapper.INSTANCE.toServices(serviceEntities);
//        return services;
    }

    public List<ServiceRelation> findServiceRelationListBySourceServiceId(String sourceServiceId) {
        Iterable<ServiceRelationEntity> relationEntities = serviceRelationRepository.findAllBySourceServiceId(sourceServiceId);
        List<ServiceRelation> relations = ServiceMapper.INSTANCE.relationEntitiesToModels(relationEntities);
        return relations;
    }

    private void emptyServiceListCache() {
        this.services = null;
    }

}
