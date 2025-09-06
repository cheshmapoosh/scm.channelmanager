package ir.daneshrefah.scm.core.services.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.asset.AssetProviderEntity;
import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
import ir.daneshrefah.scm.common.dto.*;
import ir.daneshrefah.scm.common.dto.provider.*;
import ir.daneshrefah.scm.common.dto.rest.ExternalProviderRequest;
import ir.daneshrefah.scm.common.dto.rest.ExternalProviderResponse;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.composition.CompositionServiceEditRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.java.JavaServiceEditRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.parent.ParentServiceEditRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.*;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.config.ApplicationConfig;
import ir.daneshrefah.scm.core.entity.service.*;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.ServiceRelationEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.core.mapper.AssetProviderMapper;
import ir.daneshrefah.scm.core.mapper.ScmServiceMapper;
import ir.daneshrefah.scm.core.mapper.ServiceProviderMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.plugin.api.model.service.composition.CompositionService;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractAuditableExternalService;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.model.service.parent.ParentService;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ChainValidation;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.utils.string.StringUtils.compareObject;

@RequiredArgsConstructor
@Service
@Slf4j
@Deprecated
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceRelationRepository serviceRelationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final TerminalRepository terminalRepository;
    private final TerminalService terminalService;
    private final TransformerRelationRepository transformerRelationRepository;
    private final TerminalServiceAccessRepository terminalServiceAccessRepository;
    private final AssetProviderService assetProviderService;
    private final AssetProviderMapper assetProviderMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final ScmServiceMapper scmServiceMapper;
    private List<ScmService> services;
    private List<AbstractAuditableExternalServiceProvider> serviceProviders;

    private static ObjectMapper getObjectMapper() {
        return ApplicationConfig.getObjectMapperInstance();
    }


    public void cacheEvict() {
        synchronized (this) {
            if (Objects.nonNull(serviceProviders)) {
                serviceProviders.clear();
            }
            if (Objects.nonNull(services)) {
                services.clear();
            }
        }
    }


    @Override
    public List<ScmService> findServiceList() {
        if (null == services || services.isEmpty()) {
            synchronized (this) {
                services = scmServiceMapper.toServices(serviceRepository.findAll());
            }
        }
        return services;
    }

    @Override
    public List<ExternalProviderResponse> getServiceProviderNameList(ExternalProviderRequest request) {
        return findServiceProviderList()
                .stream()
                .filter(serviceProvider -> "ALL".equalsIgnoreCase(request.getProtocol()) || ServiceProviderProtocol.findByName(request.getProtocol()).equals(serviceProvider.getProtocol()))
                .map(provider -> new ExternalProviderResponse()
                        .setCode(provider.getCode())
                        .setId(provider.getId())
                        .setTitle(provider.getTitle())).toList();
    }

    @Override
    public PagedResponseData<ScmService> findServiceList(ServiceFindRequest request) {
        List<ScmService> serviceList = findServiceList().stream()
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
                .filter(service -> serviceAccessFindFilter(request, service))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, serviceList);
    }

    private boolean serviceAccessFindFilter(ServiceFindRequest request, ScmService service) {
        if (request instanceof ServiceAccessFindRequest serviceAccessFindRequest) {
            Boolean hasTerminalAccess = serviceAccessFindRequest.getHasTerminalAccess();
            if (Objects.isNull(hasTerminalAccess)) {
                return true;
            } else {
                Boolean access = hasTerminalAccess(serviceAccessFindRequest, service);
                return hasTerminalAccess == access;
            }
        }
        return true;
    }


    @Override
    public ScmService findServiceByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findServiceList()
                .stream()
                .filter(service -> code.equals(service.getCode()))
                .findFirst().orElse(null);
    }

    @Override
    public ScmService findServiceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return serviceRepository.findById(id).map(scmServiceMapper::toService).orElse(null);
    }

    @Override
    @Transactional
    public ScmService createService(ServiceInfoRequest service) {
        if (StringUtils.isEmpty(service.getCode())) {
            throw new MissingRequiredInputException("service code");
        }
        if (StringUtils.isEmpty(service.getTitle())) {
            throw new MissingRequiredInputException("service title");
        }
        if (null == service.getType()) {
            throw new MissingRequiredInputException("service type");
        }
        if (ServiceImplementationType.CUSTOM_EXTERNAL.equals(service.getImplementationType()) &&
            (StringUtils.isEmpty(service.getServiceProviderId()))) {
            throw new MissingRequiredInputException("serviceProvider");
        }
        if (ServiceImplementationType.CUSTOM_EXTERNAL.equals(service.getImplementationType()) &&
            !checkServiceProviderExistById(service.getServiceProviderId())) {
            throw new InvalidInputException("serviceProvider");
        }
        if (ServiceImplementationType.COMPOSITION.equals(service.getImplementationType()) &&
            Objects.isNull(service.getCompositionType())) {
            throw new MissingRequiredInputException("compositionType");
        }

        if (ServiceImplementationType.REST_EXTERNAL.equals(service.getImplementationType())) {
            validateRestExternalRequest(service);
        }

        if (ServiceImplementationType.PROXY.equals(service.getImplementationType())) {
            validateProxyService(service);
        }

        ScmServiceEntity entity = ServiceEntityFactory.createServiceEntity(service);
        setServiceParent(entity, service);
        checkServiceProvider(entity, service);

        ScmServiceEntity savedEntity = serviceRepository.save(entity);
        ScmService result = scmServiceMapper.toService(savedEntity);
        if (ServiceImplementationType.PROXY.equals(service.getImplementationType())) {
            ServiceRelationEntity serviceRelation = new ServiceRelationEntity();
            serviceRelation.setSourceService(savedEntity);
            ScmServiceEntity targetService = serviceRepository.findById(service.getProxyTargetServiceId()).orElseThrow(() -> new InvalidInputException("proxyTargetServiceId"));
            serviceRelation.setTargetService(targetService);
            serviceRelationRepository.save(serviceRelation);
        }
        cacheEvict();
        return result;
    }

    private void validateProxyService(ServiceInfoRequest service) {
        String proxyTargetServiceId = service.getProxyTargetServiceId();
        serviceRepository.findById(proxyTargetServiceId).orElseThrow(() -> new NoMatchRecordFoundException("proxyTargetServiceId"));
    }

    private void validateRestExternalRequest(ServiceInfoRequest service) {
        HttpMethod httpMethod = service.getHttpMethod();
        ExternalServiceBodyType requestBodyType = service.getRequestBodyType();
        String serviceProviderId = service.getServiceProviderId();
        String path = service.getPath();
        HttpContentType requestContentType = service.getRequestContentType();
        ValidationUtils.checkNull(httpMethod, () -> new MissingRequiredInputException("httpMethod"));
        ValidationUtils.checkNull(requestBodyType, () -> new MissingRequiredInputException("requestBodyType"));
        ValidationUtils.checkNull(requestContentType, () -> new MissingRequiredInputException("requestContentType"));
        ValidationUtils.checkBlankString(serviceProviderId, () -> new MissingRequiredInputException("serviceProviderId"));
        ValidationUtils.checkBlankString(path, () -> new MissingRequiredInputException("path"));
    }

    @SuppressWarnings("unchecked")
    private void checkServiceProvider(ScmServiceEntity entity, ServiceInfoRequest service) {
        if (entity instanceof AbstractExternalServiceEntity externalServiceEntity) {
            AbstractExternalServiceProviderEntity provider = serviceProviderRepository
                    .findById(service.getServiceProviderId())
                    .orElseThrow(() -> new InvalidInputException("serviceProvider"));
            externalServiceEntity.setServiceProvider(provider);
        }
    }

    private void setServiceParent(ScmServiceEntity entity, ServiceInfoRequest service) {
        if (service.getImplementationType().equals(ServiceImplementationType.PARENT)) {
            entity.setParent(null);
        } else {
            String parentId = service.getParentId();
            if (Objects.nonNull(parentId) && !parentId.isBlank()) {
                entity.setParent(serviceRepository.findById(service.getParentId())
                        .orElseThrow(() -> new NoMatchRecordFoundException("parent")));
            }
        }
    }

    @Override
    public ScmService updateService(ServiceInfoEditRequest request) {
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
                        cacheEvict();
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

    private void applyChangesDynamically(ScmServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        applyEditServiceTypeProperties(serviceEntity, request);
        applyEditStringBasedProperties(serviceEntity, request);
        applyEditBooleanBasedProperties(serviceEntity, request);
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

    private JsonNode getCheckedJsonString(String metadata, String property) {
        try {
            return getObjectMapper().readTree(metadata);
        } catch (Exception e) {
            throw new InvalidInputException(property);
        }
    }

    private void applyEditBooleanBasedProperties(ScmServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        serviceEntity.setCheckAccessService(compareObject(request.getCheckAccessAsset(), serviceEntity.getCheckAccessAsset(), Boolean.class));
        serviceEntity.setCheckAccessService(compareObject(request.getCheckAccessService(), serviceEntity.getCheckAccessService(), Boolean.class));
        serviceEntity.setCheckAccessFirstAuthentication(compareObject(request.getCheckAccessFirstAuthentication(), serviceEntity.getCheckAccessFirstAuthentication(), Boolean.class));
        serviceEntity.setCheckAccessSecondAuthentication(compareObject(request.getCheckAccessSecondAuthentication(), serviceEntity.getCheckAccessSecondAuthentication(), Boolean.class));
    }

    private void applyEditStringBasedProperties(ScmServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        serviceEntity.setAssetProperty(compareObject(request.getAssetProperty(), serviceEntity.getAssetProperty(), String.class));
        serviceEntity.setAmountProperty(compareObject(request.getAmountProperty(), serviceEntity.getAmountProperty(), String.class));
        serviceEntity.setTitle(StringUtils.isNotEmpty(request.getTitle()) ? request.getTitle() : serviceEntity.getTitle());
        serviceEntity.setAlias(StringUtils.isNotEmpty(request.getAlias()) ? request.getAlias() : serviceEntity.getAlias());
        //check service code
        String code = request.getCode();
        if (StringUtils.isNotEmpty(code) && !serviceEntity.getCode().equals(code)) {
            serviceRepository.findByCode(code).map(ScmServiceEntity::getCode).ifPresent(s -> {
                throw new InvalidInputException("code");
            });
            serviceEntity.setCode(code);
        }
    }

    private void applyEditServiceTypeProperties(ScmServiceEntity serviceEntity, ServiceInfoEditRequest request) {
        if (serviceEntity instanceof JavaServiceEntity javaServiceEntity) {
            String value = request.getJavaImplementationClassName();
        } else if (serviceEntity instanceof AbstractExternalServiceEntity externalServiceEntity) {
            String reqProviderId = request.getServiceProviderId();
            String serviceProviderId = externalServiceEntity.getServiceProvider().getId();
            if (externalServiceEntity instanceof RestExternalServiceEntity restExternalService) {
                DynamicUpdateUtils.applyChangesIfNotNull(request.getPath(), restExternalService::setPath);
                DynamicUpdateUtils.applyChangesIfNotNull(request.getHttpMethod(), restExternalService::setHttpMethod);
                DynamicUpdateUtils.applyChangesIfNotNull(request.getRequestContentType(), restExternalService::setRequestContentType);
                DynamicUpdateUtils.applyChangesIfNotNull(request.getRequestBodyType(), restExternalService::setRequestBodyType);
            }
            if (StringUtils.isNotEmpty(reqProviderId) && !reqProviderId.equals(serviceProviderId)) {
                AbstractExternalServiceProviderEntity foundProvider = serviceProviderRepository.findById(reqProviderId)
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
                ScmServiceEntity foundParent = serviceRepository.findById(reqParentId)
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

    /**
     * @apiNote This method remove parent service and add created target proxy services
     */
    public List<ScmService> findCallableServiceList() {
        List<ScmService> serviceList = findServiceList().stream()
                .filter(Objects::nonNull)
                .filter(service -> !ServiceImplementationType.PARENT.equals(service.getImplementationType()))
                .collect(Collectors.toList());
        return serviceList;
    }

    public List<ScmService> findParentServiceList() {
        return findServiceList().stream().filter(service -> ServiceImplementationType.PARENT.equals(service.getImplementationType()))
                .collect(Collectors.toList());
    }

    public List<ServiceRelation> findServiceRelationListBySourceServiceId(String sourceServiceId) {
        List<ServiceRelationEntity> relationEntities = serviceRelationRepository.findAllBySourceServiceId(sourceServiceId);
        return scmServiceMapper.relationEntitiesToModels(relationEntities);
    }

    @Override
    @Transactional
    public void deleteService(ServiceDeleteRequest request) {
        serviceRepository.findById(request.getId())
                .stream()
                .filter(service -> service.getId().equals(request.getId()))
                .findFirst()
                .ifPresentOrElse(found -> {
                    found.setLastEditDate(request.getLastEditDate());
                    try {
                        checkTerminalServiceAccess(found);
                        checkServiceRelations(found);
                        checkTransformers(found);
                        serviceRepository.delete(found);
                    } catch (ObjectOptimisticLockingFailureException e) {
                        throw new RecordVersionException("service");
                    }
                    cacheEvict();
                }, () -> {
                    throw new NoMatchRecordFoundException("service");
                });
    }

    private void checkTransformers(ScmServiceEntity found) {
        transformerRelationRepository
                .findAllBySourceId(found.getId())
                .stream().findFirst().ifPresent(db -> {
                    throw new UncheckedRecordChildException("transformerRelation", "service has unhandled transformer relation children");
                });
    }

    private void checkServiceRelations(ScmServiceEntity found) {
        serviceRelationRepository
                .findAllBySourceServiceId(found.getId())
                .stream().findFirst().ifPresent(db -> {
                    throw new UncheckedRecordChildException("serviceRelation", "service has unhandled service relation children");
                });
    }

    private void checkTerminalServiceAccess(ScmServiceEntity found) {
        terminalServiceAccessRepository
                .findAllByServiceId(found.getId())
                .stream()
                .findFirst()
                .ifPresent((f) -> {
                    throw new UncheckedRecordChildException("terminalServiceAccess", "service has unhandled terminal access children");
                });
    }

    @Override
    public PagedResponseData<TerminalServiceAccessAssignmentResponse> findAllServiceAccessOnTerminal(ServiceAccessFindRequest request) {
        ValidationUtils.checkBlankString(request.getTerminalId(), () -> new MissingRequiredInputException("terminalId"));
        terminalRepository.findById(request.getTerminalId()).orElseThrow(() -> new InvalidInputException("terminalId"));
        PagedResponseData<ScmService> serviceList = findServiceList(request);
        List<TerminalServiceAccessAssignmentResponse> result = serviceList.getData()
                .stream()
                .map(service -> {
                    TerminalServiceAccessAssignmentResponse accessAssignmentResponse = new TerminalServiceAccessAssignmentResponse();
                    accessAssignmentResponse.setService(service);
                    Boolean access = hasTerminalAccess(request, service);
                    accessAssignmentResponse.setHasTerminalAccess(access);
                    return accessAssignmentResponse;
                })
                .collect(Collectors.toList());
        return new PagedResponseData<>(serviceList.getPageNo(), serviceList.getPageSize(), serviceList.getTotalCount().longValue(), result);
    }

    @Override
    public Optional<ProviderTerminalCoding> findProviderTerminalCoding(String terminalCode, String clientId, String providerCode) {
//        TODO dariush
        return Optional.empty();
    }

    private Boolean hasTerminalAccess(ServiceAccessFindRequest request, ScmService service) {
        return terminalService
                .findAllTerminalServiceAccesses()
                .stream()
                .filter(serviceAccess -> request.getTerminalId().equals(serviceAccess.getTerminal().getId()))
                .filter(serviceAccess -> service.getId().equals(serviceAccess.getService().getId()))
                .map(serviceAccess -> true)
                .findFirst()
                .orElse(false);
    }


    private ServiceProviderFindResponse map(AbstractAuditableExternalServiceProvider provider) {
        return new ServiceProviderFindResponse()
                .setCode(provider.getCode())
                .setTitle(provider.getTitle())
                .setProtocol(provider.getProtocol())
                .setAssetProvider(provider.getAssetProvider())
                .setProviderClassName(provider.getProviderClassName())
                .setId(provider.getId())
                .setCreateDate(provider.getCreateDate())
                .setCreator(provider.getCreator())
                .setLastEditor(provider.getLastEditor())
                .setStatus(provider.getStatus())
                .setLastEditDate(provider.getLastEditDate());
    }

    @Override
    @Transactional
    public AbstractAuditableExternalServiceProvider createServiceProvider(ServiceProviderCreteRequest request) {
        validateServiceProviderCreteRequest(request);
        AbstractExternalServiceProviderEntity entity = mapToServiceProviderEntity(request);
        AbstractExternalServiceProviderEntity savedEntity = serviceProviderRepository.save(entity);
        AbstractAuditableExternalServiceProvider serviceProvider = scmServiceMapper.toServiceProvider(savedEntity);
        cacheEvict();
        return serviceProvider;
    }

    @Override
    @Transactional
    public AbstractAuditableExternalServiceProvider deleteServiceProvider(ServiceProviderDeleteRequest request) {
        AbstractExternalServiceProviderEntity serviceProvider = serviceProviderRepository.findById(request.getServiceProviderId()).orElseThrow(() -> new InvalidInputException("serviceProviderId"));
        services
                .stream()
                .filter(service -> service instanceof AbstractAuditableExternalService)
                .map(service -> (AbstractAuditableExternalService<?>) service)
                .filter(service -> service.getServiceProvider().getId().equals(serviceProvider.getId()))
                .findFirst()
                .ifPresent(service -> {
                    throw new UncheckedRecordChildException("serviceProviderId", "provider has unhandled service children");
                });
        serviceProvider.setLastEditDate(request.getLastEditDate());
        serviceProviderRepository.delete(serviceProvider);
        cacheEvict();
        return scmServiceMapper.toServiceProvider(serviceProvider);
    }

    @Override
    @Transactional
    public AbstractAuditableExternalServiceProvider changeServiceProvider(ServiceProviderChangeRequest request) {
        AbstractExternalServiceProviderEntity serviceProvider = serviceProviderRepository.findById(request.getServiceProviderId()).orElseThrow(() -> new InvalidInputException("serviceProviderId"));
        //General service provider properties
        mapServiceProvider(serviceProvider, request);
        String assetProviderId = request.getAssetProviderId();
        if (StringUtils.isNotBlank(assetProviderId) && StringUtils.isNumeric(assetProviderId)) {
            AssetProvider foundAssetProvider = assetProviderService.findAssetProviderById(Integer.parseInt(assetProviderId))
                    .orElseThrow(() -> new InvalidInputException("assetProviderId"));
            serviceProvider.setAssetProvider(assetProviderMapper.toEntity(foundAssetProvider));
        } else {
            serviceProvider.setAssetProvider(null);
        }
        AbstractExternalServiceProviderEntity saved = serviceProviderRepository.save(serviceProvider);
        cacheEvict();
        return scmServiceMapper.toServiceProvider(saved);
    }

    private void mapServiceProvider(AbstractExternalServiceProviderEntity serviceProvider, ServiceProviderChangeRequest request) {
        serviceProvider.setCode(request.getCode());
        serviceProvider.setTitle(request.getTitle());
        serviceProvider.setProviderClassName(request.getProviderClassName());
        serviceProvider.setStatus(request.getStatus());
        serviceProvider.setLastEditDate(request.getLastEditDate());
    }

    private AbstractExternalServiceProviderEntity mapToServiceProviderEntity(ServiceProviderCreteRequest request) {
        AbstractExternalServiceProviderEntity entity;
        ServiceProviderProtocol protocol = request.getProtocol();
        switch (protocol) {
            case REST -> {
                entity = new RestExternalServiceProviderEntity();
            }
            case CUSTOM -> {
                entity = new CustomExternalServiceProviderEntity();
            }
            default -> throw new InvalidInputException("the protocol does not supported yet");
        }
        entity.setStatus(request.getStatus());
        entity.setProtocol(protocol);
        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setProviderClassName(request.getProviderClassName());
        String assetProviderId = request.getAssetProviderId();
        if (StringUtils.isNotBlank(assetProviderId)) {
            AssetProvider assetProvider = assetProviderService.findAssetProviderById(Integer.parseInt(assetProviderId)).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId"));
            AssetProviderEntity assetProviderEntity = assetProviderMapper.toEntity(assetProvider);
            entity.setAssetProvider(assetProviderEntity);
        }
        return entity;
    }


    private void validateServiceProviderCreteRequest(ServiceProviderCreteRequest request) {
        ChainValidation
                .crateValidator(request.getAssetProviderId(), "assetProviderId")
                .breakCheckIfNull()
                .checkBlank()
                .checkNumeral()
                .checkFunction((assetProviderId) -> assetProviderService.findAssetProviderList()
                        .stream()
                        .anyMatch(assetProvider -> assetProvider.getId().equals(Integer.parseInt(String.valueOf(assetProviderId)))));

    }

    //SERVICE PROVIDER

    @Override
    public List<AbstractAuditableExternalServiceProvider> findServiceProviderList() {
        if (null == serviceProviders || serviceProviders.isEmpty()) {
            synchronized (this) {
                serviceProviders = new ArrayList<>(serviceProviderMapper.toModels(serviceProviderRepository.findAll()));
            }
        }
        return serviceProviders;
    }

    @Override
    public AbstractAuditableExternalServiceProvider findServiceProviderById(String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }
        return findServiceProviderList().stream().filter(serviceProvider -> id.equals(serviceProvider.getId())).findFirst().orElse(null);
    }

    @Override
    public AbstractAuditableExternalServiceProvider findServiceProviderByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findServiceProviderList().stream().filter(serviceProvider -> code.equalsIgnoreCase(serviceProvider.getCode())).findFirst().orElse(null);
    }

    @Override
    public AbstractAuditableExternalServiceProvider findServiceProviderByIdOrCode(String value) {
        AbstractAuditableExternalServiceProvider provider = findServiceProviderById(value);
        if (null != provider) {
            return provider;
        }
        return findServiceProviderByCode(value);
    }

    @Override
    public PagedResponseData<ServiceProviderFindResponse> findServiceProviderList(ServiceProviderFindRequest request) {
        return new PagedResponseData<>(request,
                findServiceProviderList()
                        .stream()
                        .filter(provider -> Objects.isNull(request) || Objects.isNull(request.getId()) || provider.getId().equals(request.getId()))
                        .filter(provider -> Objects.isNull(request) || Objects.isNull(request.getCode()) || provider.getCode().toLowerCase().contains(request.getCode().toLowerCase()))
                        .filter(provider -> Objects.isNull(request) || Objects.isNull(request.getTitle()) || (Objects.nonNull(provider.getTitle()) && provider.getTitle().toLowerCase().contains(request.getTitle().toLowerCase())))
                        .filter(provider -> Objects.isNull(request) || Objects.isNull(request.getProviderClassName()) || (Objects.nonNull(provider.getProviderClassName()) && provider.getProviderClassName().toLowerCase().contains(request.getProviderClassName().toLowerCase())))
                        .filter(provider -> Objects.isNull(request) || Objects.isNull(request.getProtocol()) || Objects.equals(provider.getProtocol(), request.getProtocol()))
                        .map(this::map)
                        .toList());
    }

    @Override
    public boolean checkServiceProviderExistById(String serviceProviderId) {
        if (StringUtils.isEmpty(serviceProviderId)) {
            return false;
        }
        return findServiceProviderList().stream().anyMatch(serviceProvider -> serviceProviderId.equals(serviceProvider.getId()));
    }

    // PARENT SERVICE

    @Override
    @Transactional
    public ParentService createParentService(ParentServiceCreateRequest request) {
        checkServiceCodeDuplicated(request.getCode());
        ParentServiceEntity entity = ServiceEntityFactory.createServiceEntity(request);
        ParentServiceEntity savedEntity = serviceRepository.save(entity);
        ParentService result = (ParentService) scmServiceMapper.toService(savedEntity);
        cacheEvict();
        return result;
    }

    @Override
    @Transactional
    public ParentService editParentService(ParentServiceEditRequest request) {
        ParentService foundCache = (ParentService) findServiceList()
                .stream()
                .filter(s -> ServiceType.PARENT.equals(s.getType()))
                .filter(s -> s.getId().equals(request.getId()))
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException(request.getId()));
        ParentServiceEntity entity = scmServiceMapper.toEntity(foundCache);
        entity.setTitle(request.getTitle());
        entity.setStatus(request.getStatus());
        entity.setLastEditDate(request.getLastEditDate());
        entity.setVersion((Objects.nonNull(request.getVersion())) ? request.getVersion() : entity.getVersion());
        ParentServiceEntity saved = serviceRepository.save(entity);
        cacheEvict();
        return scmServiceMapper.toModel(saved);
    }

    @Override
    public ParentService getParentService(String parentServiceId) {
        return findParentServiceById(parentServiceId).orElseThrow(() -> new NoMatchRecordFoundException(parentServiceId));
    }

    @Override
    public PagedResponseData<ScmService> findParentServiceList(ParentServiceFindRequest request) {
        List<ScmService> serviceList = findServiceList().stream()
                .filter(service -> !ServiceStatus.INACTIVE.equals(service.getStatus()))
                .filter(service -> ServiceType.PARENT.equals(service.getType()))
                .filter(service -> ServiceImplementationType.PARENT.equals(service.getImplementationType()))
                .filter(service -> Objects.isNull(service.getParent()))
                .filter(service ->
                        null == request
                        || null == request.getSearch()
                        || request.getSearch().isBlank()
                        || service.getCode().trim().toUpperCase().contains(request.getSearch().trim().toUpperCase())
                        || service.getTitle().trim().toLowerCase().contains(request.getSearch().trim().toLowerCase()))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, serviceList);

    }

    private Optional<ParentService> findParentServiceById(String id) {
        if (StringUtils.isEmpty(id)) {
            return Optional.empty();
        }
        return findServiceList()
                .stream()
                .filter(service -> service.getImplementationType().equals(ServiceImplementationType.PARENT))
                .filter(service -> id.equals(service.getId()))
                .filter(service -> service instanceof ParentService)
                .map(service -> (ParentService) service)
                .findFirst();
    }

    // JAVA SERVICE

    @Transactional
    @Override
    public JavaService createJavaService(JavaServiceCreateRequest request) {
        checkServiceCodeDuplicated(request.getCode());
        JavaServiceEntity entity = ServiceEntityFactory.createServiceEntity(request);
        if (Objects.nonNull(request.getParentId())) {
            entity.setParent(serviceRepository.findById(request.getParentId()).orElseThrow(() -> new NoMatchRecordFoundException(request.getCode())));
        }
        JavaServiceEntity savedEntity = serviceRepository.save(entity);
        JavaService result = (JavaService) scmServiceMapper.toService(savedEntity);
        cacheEvict();
        return result;
    }

    @Transactional
    @Override
    public JavaService editJavaService(JavaServiceEditRequest request) {
        JavaService foundCache = (JavaService) findServiceList()
                .stream()
                .filter(s -> ServiceImplementationType.JAVA.equals(s.getImplementationType()))
                .filter(s -> s.getId().equals(request.getId()))
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException(request.getId()));
        JavaServiceEntity entity = scmServiceMapper.toEntity(foundCache);
        if (Objects.nonNull(request.getParentId())) {
            entity.setParent(serviceRepository.findById(request.getParentId()).orElseThrow(() -> new NoMatchRecordFoundException(request.getId())));
        }
        entity.setTitle(request.getTitle());
        entity.setAlias(request.getAlias());
        entity.setVersion(request.getVersion());
        entity.setType(request.getType());
        entity.setStatus(request.getStatus());
        entity.setCheckAccessFirstAuthentication(request.getCheckAccessFirstAuthentication());
        entity.setCheckAccessSecondAuthentication(request.getCheckAccessSecondAuthentication());
        entity.setCheckAccessService(request.getCheckAccessService());
        entity.setCheckAccessAsset(request.getCheckAccessAsset());
        JavaServiceEntity saved = serviceRepository.save(entity);
        cacheEvict();
        return scmServiceMapper.toModel(saved);
    }

    @Override
    public JavaService getJavaService(String javaServiceId) {
        return findServiceList()
                .stream()
                .filter(s -> ServiceImplementationType.JAVA.equals(s.getImplementationType()))
                .map(JavaService.class::cast)
                .filter(s -> s.getId().equals(javaServiceId))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException(javaServiceId));
    }

    private void checkServiceCodeDuplicated(String serviceCode) {
        if (Objects.nonNull(findServiceByCode(serviceCode))) {
            throw new DuplicatedRecordFoundException(serviceCode);
        }
    }

    // COMPOSITION SERVICE


    @Override
    public CompositionService getCompositionService(String compositionServiceId) {
        return findServiceList()
                .stream()
                .filter(s -> ServiceImplementationType.COMPOSITION.equals(s.getImplementationType()))
                .map(CompositionService.class::cast)
                .filter(s -> s.getId().equals(compositionServiceId))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException(compositionServiceId));
    }

    @Override
    @Transactional
    public CompositionService editCompositionService(CompositionServiceEditRequest request) {
        CompositionService foundCache = (CompositionService) findServiceList()
                .stream()
                .filter(s -> ServiceImplementationType.COMPOSITION.equals(s.getImplementationType()))
                .filter(s -> s.getId().equals(request.getId()))
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException(request.getId()));
        CompositionServiceEntity entity = scmServiceMapper.toEntity(foundCache);
        if (Objects.nonNull(request.getParentId())) {
            entity.setParent(serviceRepository.findById(request.getParentId()).orElseThrow(() -> new NoMatchRecordFoundException(request.getId())));
        }
        entity.setCompositionType(request.getCompositionType());
        entity.setAmountProperty(request.getAmountProperty());
        entity.setAssetProperty(request.getAssetProperty());
        entity.setVersion(request.getVersion());
        entity.setTitle(request.getTitle());
        entity.setAlias(request.getAlias());
        entity.setType(request.getType());
        entity.setStatus(request.getStatus());
        entity.setCheckAccessFirstAuthentication(request.getCheckAccessFirstAuthentication());
        entity.setCheckAccessSecondAuthentication(request.getCheckAccessSecondAuthentication());
        entity.setCheckAccessService(request.getCheckAccessService());
        entity.setCheckAccessAsset(request.getCheckAccessAsset());
        CompositionServiceEntity saved = serviceRepository.save(entity);
        cacheEvict();
        return scmServiceMapper.toModel(saved);
    }

    @Override
    @Transactional
    public CompositionService createCompositionService(CompositionServiceCreateRequest request) {
        checkServiceCodeDuplicated(request.getCode());
        CompositionServiceEntity entity = ServiceEntityFactory.createServiceEntity(request);
        if (Objects.nonNull(request.getParentId())) {
            entity.setParent(serviceRepository.findById(request.getParentId()).orElseThrow(() -> new NoMatchRecordFoundException(request.getCode())));
        }
        CompositionServiceEntity savedEntity = serviceRepository.save(entity);
        CompositionService result = (CompositionService) scmServiceMapper.toService(savedEntity);
        cacheEvict();
        return result;
    }
}
