package ir.daneshrefah.scm.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RecordVersionException;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceCompositionType;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.service.*;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.mapper.AssetProviderMapper;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.mapper.ServiceProviderMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.utils.string.StringUtils.compareObject;

@RequiredArgsConstructor
@Service
public class ServiceServiceImpl implements ServiceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);

    private final ServiceRepository serviceRepository;
    private final ServiceRelationRepository serviceRelationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final AssetProviderRepository assetProviderRepository;
    private final TerminalRepository terminalRepository;
    private final TerminalService terminalService;
    private final TransformerRelationRepository transformerRelationRepository;
    private List<ir.daneshrefah.scm.common.model.service.Service> services;
    private List<ExternalServiceProvider> serviceProviders;
    private List<AssetProvider> assetProviders;

    private static ObjectMapper getObjectMapper() {
        return ApplicationConfig.getObjectMapperInstance();
    }

    @Override
    public List<AssetProvider> findAssetProviderList() {
        if (null == assetProviders || assetProviders.isEmpty()) {
            synchronized (this) {
                assetProviders = AssetProviderMapper.INSTANCE.toModels(assetProviderRepository.findAll());
            }
        }
        return assetProviders;
    }

    @Override
    public AssetProvider findAssetProviderById(Integer id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return findAssetProviderList().stream().filter(assetProvider -> id.equals(assetProvider.getId())).findFirst().orElse(null);
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service findAssetProviderProviderServiceByAssetProviderId(Integer id) {
        AssetProvider assetProvider = findAssetProviderById(id);
        if (Objects.isNull(assetProvider) || StringUtils.isEmpty(assetProvider.getProviderServiceId())) {
            return null;
        }
        return findServiceById(assetProvider.getProviderServiceId());
    }

    @Override
    public List<ExternalServiceProvider> findServiceProviderList() {
        if (null == serviceProviders || serviceProviders.isEmpty()) {
            synchronized (this) {
                serviceProviders = ServiceProviderMapper.INSTANCE.toModels(serviceProviderRepository.findAll());
            }
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
        if (null == services || services.isEmpty()) {
            synchronized (this) {
                services = ServiceMapper.INSTANCE.toServices(serviceRepository.findAll());
            }
        }
        return services;
    }

    @Override
    public PagedResponseData<ir.daneshrefah.scm.common.model.service.Service> findServiceList(ServiceFindRequest request) {
        List<ir.daneshrefah.scm.common.model.service.Service> serviceList = findServiceList().stream()
                .filter(service -> null == request || null == request.getCode() || service.getCode().trim().toUpperCase().contains(request.getCode().trim().toUpperCase()))
                .filter(service -> null == request || null == request.getIsSystemic() || request.getIsSystemic().equals(service.getIsSystemic()))
                .filter(service -> null == request || null == request.getType() || request.getType().equals(service.getType()))
                .filter(service -> null == request || null == request.getStatus() || request.getStatus().equals(service.getStatus()))
                .filter(service -> null == request || null == request.getTitle() || service.getTitle().trim().toLowerCase().contains(request.getTitle().trim().toLowerCase()))
                .filter(service -> {
                    if (Objects.nonNull(request) && Objects.nonNull(request.getParentId())) {
                        if (Objects.nonNull(service.getParent()) && Objects.nonNull(service.getParent().getId())) {
                            return request.getParentId().trim().equals(service.getParent().getId().trim());
                        }
                        return false;
                    }
                    return true;
                })
                .filter(service -> null == request || null == request.getImplementationType() || request.getImplementationType().equals(service.getImplementationType()))
                .filter(service->serviceAccessFindFilter(request,service))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, serviceList);


    }

    private boolean serviceAccessFindFilter(ServiceFindRequest request, ir.daneshrefah.scm.common.model.service.Service service) {
        if (request instanceof ServiceAccessFindRequest serviceAccessFindRequest){
            Boolean hasTerminalAccess = serviceAccessFindRequest.getHasTerminalAccess();
            if (Objects.isNull(hasTerminalAccess)){
                return true;
            }else {
                Boolean access = hasTerminalAccess(serviceAccessFindRequest, service);
                return hasTerminalAccess == access;
            }
        }
        return true;
    }


    @Override
    public ir.daneshrefah.scm.common.model.service.Service findServiceByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findServiceList()
                .stream()
                .filter(service -> code.equals(service.getCode()))
                .findFirst().orElse(null);
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service findServiceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return serviceRepository.findById(id).map(ServiceMapper.INSTANCE::toService).orElse(null);
    }

    public ParentService findParentServiceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        ir.daneshrefah.scm.common.model.service.Service result = findServiceList().stream().filter(service -> id.equals(service.getId())).findFirst().orElse(null);
        if (result instanceof ParentService) {
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
        if (ServiceImplementationType.COMPOSITION.equals(service.getImplementationType()) &&
                Objects.isNull(service.getCompositionType())) {
            throw new MissingRequiredInputException("compositionType");
        }

        ServiceEntity entity = ServiceEntityFactory.createServiceEntity(service);
        setServiceParent(entity, service);
        checkServiceProvider(entity, service);

        ir.daneshrefah.scm.common.model.service.Service result = ServiceMapper.INSTANCE.toService(serviceRepository.save(entity));
        emptyServiceListCache();
        return result;
    }


    private void checkServiceProvider(ServiceEntity entity, ServiceInfoRequest service) {
        if (entity instanceof ExternalServiceEntity) {
            ((ExternalServiceEntity) entity)
                    .setServiceProvider(serviceProviderRepository
                            .findById(service.getServiceProviderId()).orElseThrow(() -> new NoMatchRecordFoundException("serviceProvider")
                            ));
        }
    }

    private void setServiceParent(ServiceEntity entity, ServiceInfoRequest service) {
        if (service.getImplementationType().equals(ServiceImplementationType.PARENT)) {
            entity.setParent(null);
        } else {
            String parentId = service.getParentId();
            if (Objects.nonNull(parentId)) {
                entity.setParent(serviceRepository.findById(service.getParentId())
                        .orElseThrow(() -> new NoMatchRecordFoundException("parent")));
            }
        }
    }

    @Override
    public ir.daneshrefah.scm.common.model.service.Service updateService(ServiceInfoEditRequest request) {
        validateServiceInfoEditRequest(request);
        serviceRepository
                .findByCode(request.getCode())
                .stream().filter(service -> service.getId().equals(request.getId()))
                .findFirst()
                .ifPresentOrElse(service -> {
                    if (service.getLastEditDate().equals(request.getLastEditDate())) {
                        applyChangesDynamically(service, request);
                        try {
                            serviceRepository.save(service);
                        } catch (ObjectOptimisticLockingFailureException e) {
                            throw new RecordVersionException("service");
                        }
                        emptyServiceListCache();
                    } else {
                        throw new RecordVersionException("service");
                    }
                }, () -> {
                    throw new NoMatchRecordFoundException("service");
                });
        //reload cache and find service on that.
        return findServiceList()
                .stream()
                .filter(service -> service.getId().equals(request.getId()))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException("service"));
    }

    private void applyChangesDynamically(ServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        applyEditServiceTypeProperties(serviceEntity, request);
        applyEditStringBasedProperties(serviceEntity, request);
        applyEditBooleanBasedProperties(serviceEntity, request);
        applyEditJsonBasedProperties(serviceEntity, request);
        if (Objects.nonNull(request.getVersion()) && !request.getVersion().equals(serviceEntity.getVersion())) {
            serviceEntity.setVersion(request.getVersion());
        }
        if (Objects.nonNull(request.getStatus()) && !request.getStatus().equals(serviceEntity.getStatus())) {
            serviceEntity.setStatus(request.getStatus());
        }
        if (Objects.nonNull(request.getType()) && !request.getType().equals(serviceEntity.getType())) {
            serviceEntity.setType(request.getType());
        }
        serviceEntity.setVersion(serviceEntity.getVersion() + 1);
    }

    private void applyEditJsonBasedProperties(ServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        if (Objects.nonNull(request.getMetadata())) {
            serviceEntity.setMetadata(getCheckedJsonString(request.getMetadata(), "metadata"));
        }
        if (Objects.nonNull(request.getRequestJsonSchema())) {
            serviceEntity.setMetadata(getCheckedJsonString(request.getRequestJsonSchema(), "requestJsonSchema"));
        }
        if (Objects.nonNull(request.getResponseJsonSchema())) {
            serviceEntity.setMetadata(getCheckedJsonString(request.getResponseJsonSchema(), "responseJsonSchema"));
        }
    }

    private JsonNode getCheckedJsonString(String metadata, String property) {
        try {
            return getObjectMapper().readTree(metadata);
        } catch (Exception e) {
            throw new InvalidInputException(property);
        }
    }

    private void applyEditBooleanBasedProperties(ServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        serviceEntity.setCheckAccessService(compareObject(request.getCheckAccessAsset(), serviceEntity.getCheckAccessAsset(), Boolean.class));
        serviceEntity.setCheckAccessService(compareObject(request.getCheckAccessService(), serviceEntity.getCheckAccessService(), Boolean.class));
        serviceEntity.setCheckAccessFirstAuthentication(compareObject(request.getCheckAccessFirstAuthentication(), serviceEntity.getCheckAccessFirstAuthentication(), Boolean.class));
        serviceEntity.setCheckAccessSecondAuthentication(compareObject(request.getCheckAccessSecondAuthentication(), serviceEntity.getCheckAccessSecondAuthentication(), Boolean.class));
    }

    private void applyEditStringBasedProperties(ServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        serviceEntity.setAssetProperty(compareObject(request.getAssetProperty(), serviceEntity.getAssetProperty(), String.class));
        serviceEntity.setAmountProperty(compareObject(request.getAmountProperty(), serviceEntity.getAmountProperty(), String.class));
        serviceEntity.setCustomerProperty(compareObject(request.getCustomerProperty(), serviceEntity.getCustomerProperty(), String.class));
        serviceEntity.setTitle(StringUtils.isNotEmpty(request.getTitle()) ? request.getTitle() : serviceEntity.getTitle());
        serviceEntity.setAlias(StringUtils.isNotEmpty(request.getAlias()) ? request.getAlias() : serviceEntity.getAlias());
        //check service code
        String code = request.getCode();
        if (StringUtils.isNotEmpty(code) && !serviceEntity.getCode().equals(code)) {
            serviceRepository.findByCode(code).map(ServiceEntity::getCode).ifPresent(s -> {
                throw new InvalidInputException("code");
            });
            serviceEntity.setCode(code);
        }
    }

    private void applyEditServiceTypeProperties(ServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        if (serviceEntity instanceof JavaServiceEntity javaServiceEntity) {
            String value = request.getJavaImplementationClassName();
            javaServiceEntity.setJavaImplementationClassName(StringUtils.isEmpty(value) ? javaServiceEntity.getJavaImplementationClassName() : value);
        } else if (serviceEntity instanceof ExternalServiceEntity externalServiceEntity) {
            String reqProviderId = request.getServiceProviderId();
            String serviceProviderId = externalServiceEntity.getServiceProvider().getId();
            if (StringUtils.isNotEmpty(reqProviderId) && !reqProviderId.equals(serviceProviderId)) {
                ExternalServiceProviderEntity foundProvider = serviceProviderRepository.findById(reqProviderId)
                        .orElseThrow(() -> new InvalidInputException("serviceProviderId"));
                externalServiceEntity.setServiceProvider(foundProvider);
            }
        } else if (serviceEntity instanceof CompositionServiceEntity compositionService) {
            ServiceCompositionType reqCompositionType = request.getCompositionType();
            ServiceCompositionType compositionType = compositionService.getCompositionType();
            if (Objects.nonNull(reqCompositionType) && !reqCompositionType.equals(compositionType)) {
                compositionService.setCompositionType(reqCompositionType);
            }
        }
        //check parent changes on all service type
        else if (serviceEntity instanceof ParentServiceEntity) {
            request.setParentId("");
        } else {
            String reqParentId = request.getParentId();
            if (StringUtils.isNotEmpty(reqParentId) && !reqParentId.equals(serviceEntity.getParent().getId())) {
                ServiceEntity foundParent = serviceRepository.findById(reqParentId)
                        .orElseThrow(() -> new InvalidInputException("parentId"));
                serviceEntity.setParent(foundParent);
            }
        }

    }

    private void validateServiceInfoEditRequest(ServiceInfoEditRequest request) {
        String id = request.getId();
        LocalDateTime lastEditDate = request.getLastEditDate();
        if (StringUtils.isEmpty(id)) {
            throw new MissingRequiredInputException("id");
        }
        if (Objects.isNull(lastEditDate)) {
            throw new MissingRequiredInputException("lastEditDate");
        }
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
        return ServiceMapper.INSTANCE.relationEntitiesToModels(relationEntities);
    }

    private void emptyServiceListCache() {
        if (Objects.nonNull(this.services)) {
            this.services.clear();
        }
    }

    @Override
    public void deleteService(ServiceDeleteRequest request) {
        validateServiceDeleteRequest(request);
        serviceRepository.findById(request.getId())
                .stream()
                .filter(service -> service.getId().equals(request.getId()))
                .findFirst()
                .ifPresentOrElse(found -> {
                    if (found.getLastEditDate().equals(request.getLastEditDate())) {
                        //if record version passed.
                        try {
                            serviceRepository.delete(found);
                            transformerRelationRepository.deleteAll(transformerRelationRepository.findAllBySourceId(found.getId()));
                        } catch (ObjectOptimisticLockingFailureException e) {
                            throw new RecordVersionException("service");
                        }
                        emptyServiceListCache();
                    } else {
                        throw new RecordVersionException("service");
                    }
                }, () -> {
                    throw new NoMatchRecordFoundException("service");
                });
    }


    private void validateServiceDeleteRequest(ServiceDeleteRequest request) {
        String id = request.getId();
        LocalDateTime lastEditDate = request.getLastEditDate();
        if (Objects.isNull(id) || id.isBlank()) {
            throw new InvalidInputException("id");
        }
        if (Objects.isNull(lastEditDate)) {
            throw new InvalidInputException("lastEditDate");
        }
    }

    @Override
    public PagedResponseData<TerminalServiceAccessAssignmentResponse> findAllServiceAccessOnTerminal(ServiceAccessFindRequest request) {
        ValidationUtils.checkBlankString(request.getTerminalId(), () -> new MissingRequiredInputException("terminalId"));
        terminalRepository.findById(request.getTerminalId()).orElseThrow(() -> new InvalidInputException("terminalId"));
        PagedResponseData<ir.daneshrefah.scm.common.model.service.Service> serviceList = findServiceList(request);
        List<TerminalServiceAccessAssignmentResponse> result = serviceList.getData()
                .stream()
                .map(service -> {
                    TerminalServiceAccessAssignmentResponse accessAssignmentResponse = new TerminalServiceAccessAssignmentResponse();
                    accessAssignmentResponse.setService(service);
                    Boolean access = hasTerminalAccess(request,service);
                    accessAssignmentResponse.setHasTerminalAccess(access);
                    return accessAssignmentResponse;
                })
                .collect(Collectors.toList());
        return new PagedResponseData<>(serviceList.getPageNo(),serviceList.getPageSize(),serviceList.getTotalCount().longValue(), result);
    }

    private Boolean hasTerminalAccess(ServiceAccessFindRequest request, ir.daneshrefah.scm.common.model.service.Service service) {
       return terminalService
                .findAllTerminalServiceAccesses()
                .stream()
                .filter(serviceAccess -> request.getTerminalId().equals(serviceAccess.getTerminal().getId()))
                .filter(serviceAccess -> service.getId().equals(serviceAccess.getService().getId()))
                .map(serviceAccess -> true)
                .findFirst()
                .orElse(false);
    }


}
