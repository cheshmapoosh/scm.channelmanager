package ir.daneshrefah.scm.core.service.rest;

import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.dynamic.rest.ParameterNode;
import ir.daneshrefah.scm.common.model.service.parameter.*;
import ir.daneshrefah.scm.common.service.rest.*;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceConditionEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseConditionEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import ir.daneshrefah.scm.core.mapper.ParameterDatasourceConditionMapper;
import ir.daneshrefah.scm.core.mapper.ParameterMapper;
import ir.daneshrefah.scm.core.mapper.ResponseConditionMapper;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.core.service.ParameterParser;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ChainValidation;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class ParameterServiceImpl implements ParameterService {

    private static final List<RestExternalProviderResponse> EXTERNAL_PROVIDER_NAME_CACHE = new ArrayList<>();
    private final ParameterResponseConditionRelationRepository responseConditionRelationRepository;
    private final ParameterDatasourceConditionRepository datasourceConditionRepository;
    private final ParameterProviderRelationRepository providerRelationRepository;
    private final ParameterServiceRelationRepository serviceRelationRepository;
    private final ResponseConditionRepository responseConditionRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final TransformerRepository transformerRepository;
    private final ParameterRepository parameterRepository;
    private final ErrorMappingService errorMappingService;
    private final ServiceRepository serviceRepository;
    private final ParameterParser parameterParser;
    private final ServiceServiceImpl serviceServiceImpl;

    @Override
    public List<RestExternalProviderResponse> getRestExternalProviderNameList() {
        if (EXTERNAL_PROVIDER_NAME_CACHE.isEmpty()) {
            synchronized (this) {
                if (EXTERNAL_PROVIDER_NAME_CACHE.isEmpty()) {
                    return serviceProviderRepository
                            .findAllRestProviders()
                            .stream()
                            .map(entity -> {
                                RestExternalProviderResponse response = new RestExternalProviderResponse();
                                response.setId(entity.getId());
                                response.setCode(entity.getCode());
                                response.setTitle(entity.getTitle());
                                return response;
                            }).toList();
                }
            }
        }
        return EXTERNAL_PROVIDER_NAME_CACHE;
    }

    private void removeParameterRelation(Long parameterId) {
        serviceRelationRepository.findById(parameterId)
                .ifPresentOrElse(serviceRelationRepository::delete, () -> {
                    providerRelationRepository.findById(parameterId).ifPresentOrElse(providerRelationRepository::delete, () -> {
                        responseConditionRelationRepository.findById(parameterId).ifPresent(responseConditionRelationRepository::delete);
                    });
                });
    }

    @Override
    public Parameter remove(ParameterDeleteRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ParameterEntity entity = parameterRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkParameterRecordChildren(entity);
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        removeParameterRelation(entity.getId());
        parameterRepository.delete(entity);
        ParameterParser.clearCache();
        return ParameterMapper.INSTANCE.toModel(entity);
    }

    private void checkParameterRecordChildren(ParameterEntity entity) {
        if (!parameterRepository
                .findAllByParent(entity)
                .isEmpty()) {
            throw new RemoveParentRecordException("parent");
        }
    }

    @Override
    public ResponseCondition removeResponseCondition(ResponseConditionDeleteRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ResponseConditionEntity entity = responseConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        responseConditionRepository.delete(entity);
        ParameterParser.clearCache();
        return ResponseConditionMapper.INSTANCE.toModel(entity);
    }

    @Override
    public Parameter findParameterById(String id) {
        ValidationUtils.checkNull(id, () -> new InvalidInputException("id"));
        ValidationUtils.checkBlankString(id, () -> new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(id, () -> new InvalidInputException("id"));
        ParameterEntity entity = parameterRepository.findById(Long.parseLong(id)).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return ParameterMapper.INSTANCE.toModel(entity);
    }

    @Override
    public List<ParameterDatasourceCondition> findResponseConditionDatasourceList(ResponseConditionDatasourceFindRequest request) {
        ValidationUtils.checkNull(request.getResponseConditionId(), () -> new MissingRequiredInputException("responseConditionId"));
        ResponseConditionEntity entity = responseConditionRepository.findById(request.getResponseConditionId()).orElseThrow(() -> new InvalidInputException("responseConditionId"));
        return entity.getConditions()
                .stream()
                .map(ParameterDatasourceConditionMapper.INSTANCE::toModel)
                .toList();
    }

    @Override
    public PagedResponseData<ResponseCondition> findResponseCondition(ResponseConditionFindRequest request) {
        String providerId = request.getProviderId();
        String serviceId = request.getServiceId();
        List<ResponseCondition> responseConditions = new ArrayList<>();
        if (Objects.nonNull(providerId) && !providerId.isBlank()) {
            if (Objects.nonNull(serviceId)) {
                throw new InvalidInputException("service Id must be empty");
            }
            responseConditions = serviceProviderRepository
                    .findById(providerId).orElseThrow(() -> new NoMatchRecordFoundException("providerId"))
                    .getResponseConditions().stream().map(ResponseConditionMapper.INSTANCE::toModel).toList();
        }
        if (Objects.nonNull(serviceId) && !serviceId.isBlank()) {
            if (Objects.nonNull(providerId)) {
                throw new InvalidInputException("provider Id must be empty");
            }
            RestExternalServiceEntity service = (RestExternalServiceEntity) serviceRepository.findById(serviceId).orElseThrow(() -> new NoMatchRecordFoundException("serviceId"));
            responseConditions = service.getResponseConditions().stream().map(ResponseConditionMapper.INSTANCE::toModel).toList();
        }
        return new PagedResponseData<>(request, responseConditions);
    }

    @Override
    public ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request) {
        ValidationUtils.checkNull(request.getServiceId(), () -> new InvalidInputException("serviceId"));
        ValidationUtils.checkBlankStringIfNotNull(request.getActionType(), () -> new InvalidInputException("actionType"));
        ValidationUtils.checkBlankString(request.getServiceId(), () -> new InvalidInputException("serviceId"));
        ValidationUtils.checkBlankString(request.getActionType(), () -> new InvalidInputException("actionType"));
        ServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new NoMatchRecordFoundException("serviceID"));
        RestExternalServiceEntity service = (RestExternalServiceEntity) serviceEntity;
        ParameterParser.RestExternalServiceParameterCache parametersCache = parameterParser.getParametersCache(ServiceMapper.INSTANCE.toModel(service));
        ParameterActionType actionType = ParameterActionType.findByValue(request.getActionType());
        Long responseConditionId = request.getResponseConditionId();
        ResponseCondition responseCondition = null;
        if (actionType.equals(ParameterActionType.RESPONSE_BODY) && Objects.isNull(responseConditionId)) {
            throw new InvalidInputException("responseConditionId");
        }
        if (Objects.nonNull(responseConditionId)) {
            ResponseConditionEntity entity = responseConditionRepository.findById(responseConditionId).orElseThrow(() -> new NoMatchRecordFoundException("responseConditionId"));
            responseCondition = ResponseConditionMapper.INSTANCE.toModel(entity);
        }
        ParameterNode foundNode = switch (actionType) {
            case REQUEST_BODY -> parametersCache.getRequestBodyNode();
            case REQUEST_HEADER -> parametersCache.getRequestHeaderVariableNode();
            case REQUEST_PATH_VARIABLE -> parametersCache.getRequestPathVariableNode();
            case REQUEST_QUERY_STRING -> parametersCache.getRequestQueryStringVariableNode();
            case RESPONSE_BODY -> parametersCache.getConditionCache().getResponseBodyNode(responseCondition);
            case RESPONSE_HEADER -> parametersCache.getResponseHeaderVariableNode();
            case PROXY_OVERRIDE -> null;
        };
        return new ParameterTreeFindResponse().setTree(foundNode);
    }

    @Override
    public PagedResponseData<Parameter> findParameter(ParameterFindRequest request) {
        validateParameterFindRequest(request);
        String serviceProviderId = request.getServiceProviderId();
        String serviceId = request.getServiceId();
        Long responseConditionId = request.getResponseConditionId();
        List<ParameterEntity> parameters;
        if (Objects.nonNull(serviceProviderId)) {
            parameters = serviceProviderRepository.findById(serviceProviderId).orElseThrow().getParameters();
        } else if (Objects.nonNull(serviceId)) {
            ServiceEntity serviceEntity = serviceRepository.findById(serviceId).orElseThrow();
            RestExternalServiceEntity entity = (RestExternalServiceEntity) serviceEntity;
            parameters = entity.getParameters();
        } else {
            parameters = responseConditionRepository.findById(responseConditionId).orElseThrow().getResponseParameters();
        }
        return applyParameterFindFilters(parameters, request);
    }

    private PagedResponseData<Parameter> applyParameterFindFilters(List<ParameterEntity> parameters, ParameterFindRequest request) {
        return new PagedResponseData<>(request,
                parameters
                        .stream()
                        .map(ParameterMapper.INSTANCE::toModel)
                        .filter(parameter -> Objects.isNull(request.getParameterName()) || parameter.getName().contains(request.getParameterName()))
                        .filter(parameter -> Objects.isNull(request.getParentId()) || Objects.isNull(parameter.getParent()) || parameter.getParent().getId().equals(request.getParentId()))
                        .filter(parameter -> Objects.isNull(request.getActionType()) || parameter.getActionType().equals(ParameterActionType.findByValue(request.getActionType())))
                        .toList());
    }

    private void validateParameterFindRequest(ParameterFindRequest request) {
        final AtomicBoolean hasParameterTargetId = new AtomicBoolean(false);
        ValidationUtils.checkBlankStringIfNotNull(request.getParameterName(), () -> new InvalidInputException("parameterName"));
        ChainValidation.crateValidator(request.getActionType(), "actionType").breakCheckIfNull()
                .checkFunction(input -> !Objects.isNull(ParameterActionType.findByValue(String.valueOf(input))));
        ChainValidation.crateValidator(request.getParentId(), "parentId").breakCheckIfNull().checkNumeral()
                .checkFunction((input) -> parameterRepository.findById(Long.parseLong(String.valueOf(input))).isPresent());
        ChainValidation.crateValidator(request.getServiceProviderId(), "serviceProviderId").breakCheckIfNull().checkBlank()
                .checkFunction(input -> {
                    boolean present = serviceProviderRepository.findById(String.valueOf(input)).isPresent();
                    hasParameterTargetId.set(present);
                    return present;
                });
        ChainValidation.crateValidator(request.getServiceId(), "serviceId").breakCheckIfNull().checkBlank()
                .checkFunction(input -> {
                    boolean present = serviceRepository.findById(String.valueOf(input)).isPresent();
                    hasParameterTargetId.set(present);
                    return present;
                });
        ChainValidation.crateValidator(request.getResponseConditionId(), "responseConditionId").breakCheckIfNull().checkNumeral()
                .checkFunction(input -> {
                    boolean present = responseConditionRepository.findById(Long.parseLong(String.valueOf(input))).isPresent();
                    hasParameterTargetId.set(present);
                    return present;
                });
        if (!hasParameterTargetId.get()) {
            throw new MissingRequiredInputException("parameter target relation id");
        }
    }

    @Override
    public ParameterDatasourceCondition removeResponseConditionDatasource(ResponseConditionDatasourceRemoveRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ParameterDatasourceConditionEntity entity = datasourceConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        datasourceConditionRepository.delete(entity);
        ParameterParser.clearCache();
        return ParameterDatasourceConditionMapper.INSTANCE.toModel(entity);
    }

    @Override
    public ParameterDatasourceCondition changeResponseConditionDatasource(ResponseConditionDatasourceChangeRequest request) {
        ParameterDatasourceConditionEntity entity = datasourceConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getConditionValue(), entity::setConditionValue);
        ParameterDatasourceEntity datasource = entity.getParameter();
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getConvertorCode(), datasource::setConvertorCode);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getValue(), datasource::setValue);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getLength(), datasource::setLength);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getProperty(), datasource::setProperty);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getOperation(), (value)-> entity.setOperation(DatasourceConditionOperation.findByValue(request.getOperation())));
        entity.setLastEditor(getCurrentUser());
        datasourceConditionRepository.save(entity);
        ParameterParser.clearCache();
        return ParameterDatasourceConditionMapper.INSTANCE.toModel(entity);
    }

    @Override
    @Transactional
    public ResponseCondition changeResponseCondition(ResponseConditionChangeRequest request) {
        ResponseConditionEntity entity = responseConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getTransformerId(), (transformerId -> {
            TransformerEntity transformer = transformerRepository
                    .findById(transformerId)
                    .orElseThrow(() -> new NoMatchRecordFoundException("transformerId"));
            entity.setResponseTransformer(transformer);
        }));
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getErrorCode(), entity::setResponseExceptionErrorCodeProperty);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getErrorMessage(), entity::setResponseExceptionErrorMessageProperty);
        entity.setLastEditDate(LocalDateTime.now());
        entity.setLastEditor(getCurrentUser());
        responseConditionRepository.save(entity);
        ParameterParser.clearCache();
        return ResponseConditionMapper.INSTANCE.toModel(entity);
    }

    @Override
    @Transactional
    public Parameter change(ParameterChangeRequest request) {
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ParameterEntity entity = parameterRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        DynamicUpdateUtils.applyChangesIfNotNull(request.getParentId(), parentId -> {
            ParameterEntity parent = parameterRepository.findById(parentId).orElseThrow(() -> new InvalidInputException("parentId"));
            entity.setParent(parent);
        });
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getParameterName(), entity::setName);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getDefaultValue(), entity::setDefaultValue);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getTag(), entity::setTag);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getActionType(), entity::setActionType);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getOrder(), entity::setOrder);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getRequired(), entity::setRequired);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getInternal(), entity::setInternal);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getParameterType(), entity::setType);
        ParameterDatasourceEntity datasource = entity.getDatasource();
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getValue(), datasource::setValue);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getConvertorCode(), datasource::setConvertorCode);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getLength(), datasource::setLength);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getDatasourcePropertyType(), datasource::setProperty);
        entity.setLastEditDate(LocalDateTime.now());
        entity.setLastEditor(getCurrentUser());
        parameterRepository.save(entity);
        ParameterParser.clearCache();
        return ParameterMapper.INSTANCE.toModel(entity);
    }

    private void checkOptimisticRecordVersion(LocalDateTime request, LocalDateTime entity) {
        if (!request.equals(entity)) {
            throw new RecordVersionException("lastEditDate");
        }
    }

    @Override
    @Transactional
    public ResponseCondition createResponseCondition(ResponseConditionRequest request) {
        validateResponseConditionRequest(request);
        String transformerId = request.getTransformerId();
        ResponseConditionEntity entity = new ResponseConditionEntity();
        if (Objects.nonNull(transformerId)) {
            TransformerEntity transformerEntity = transformerRepository.findById(transformerId).orElseThrow(() -> new NoMatchRecordFoundException("transformerId"));
            entity.setResponseTransformer(transformerEntity);
        }
        entity.setResponseExceptionErrorCodeProperty(request.getErrorCode());
        String errorMessage = errorMappingService
                .findByExceptionClassNameAndErrorCode(request.getErrorMessage(), request.getErrorCode())
                .map(ErrorMapping::getExceptionClassName)
                .orElse(request.getErrorMessage());
        entity.setResponseExceptionErrorMessageProperty(errorMessage);
        responseConditionRepository.save(entity);
        setResponseConditionTargetId(request, entity);
        ParameterParser.clearCache();
        return ResponseConditionMapper.INSTANCE.toModel(entity);
    }

    private void setResponseConditionTargetId(ResponseConditionRequest request, ResponseConditionEntity entity) {
        String serviceId = request.getServiceId();
        String serviceProviderId = request.getServiceProviderId();
        if (Objects.nonNull(serviceProviderId) && !serviceProviderId.isBlank()) {
            AbstractExternalServiceProviderEntity provider = serviceProviderRepository.findById(serviceProviderId).orElseThrow(() -> new NoMatchRecordFoundException("serviceProviderId"));
            List<ResponseConditionEntity> responseConditions = provider.getResponseConditions();
            if (Objects.isNull(responseConditions)) {
                responseConditions = new ArrayList<>();
            }
            responseConditions.add(entity);
            serviceProviderRepository.save(provider);
        } else {
            ServiceEntity serviceEntity = serviceRepository.findById(serviceId).orElseThrow(() -> new NoMatchRecordFoundException("serviceId"));
            if (serviceEntity instanceof RestExternalServiceEntity restService) {
                List<ResponseConditionEntity> responseConditions = restService.getResponseConditions();
                if (Objects.isNull(responseConditions)) {
                    responseConditions = new ArrayList<>();
                }
                responseConditions.add(entity);
                serviceRepository.save(restService);
            } else {
                throw new InvalidInputException("serviceId");
            }
        }
        serviceServiceImpl.cacheEvict();
    }

    private void validateResponseConditionRequest(ResponseConditionRequest request) {
        ValidationUtils.checkBlankStringIfNotNull(request.getTransformerId(), () -> new InvalidInputException("transformerId"));
        ValidationUtils.checkBlankStringIfNotNull(request.getErrorCode(), () -> new InvalidInputException("errorCode"));
        ValidationUtils.checkBlankStringIfNotNull(request.getErrorMessage(), () -> new InvalidInputException("errorMessage"));
        if (StringUtils.isBlank(request.getServiceId()) && StringUtils.isBlank(request.getServiceProviderId())) {
            throw new MissingRequiredInputException("targetId(serviceId or serviceProviderId)");
        }
        if (StringUtils.isNotBlank(request.getErrorMessage()) && StringUtils.isBlank(request.getErrorCode())) {
            throw new MissingRequiredInputException("errorCode");
        }
    }

    @Override
    @Transactional
    public ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request) {
        validateResponseConditionDatasourceRequest(request);
        ResponseConditionEntity responseConditionEntity = responseConditionRepository
                .findById(request.getResponseConditionId()).orElseThrow(() -> new NoMatchRecordFoundException("responseConditionId"));
        List<ParameterDatasourceConditionEntity> conditions = responseConditionEntity.getConditions();
        if (Objects.isNull(conditions)) {
            conditions = new ArrayList<>();
        }
        ParameterDatasourceConditionEntity entity = createParameterDatasourceConditionEntity(request);
        entity = datasourceConditionRepository.save(entity);
        conditions.add(entity);
        responseConditionEntity.setConditions(conditions);
        responseConditionRepository.save(responseConditionEntity);
        ParameterParser.clearCache();
        return ParameterDatasourceConditionMapper.INSTANCE.toModel(entity);
    }

    private ParameterDatasourceConditionEntity createParameterDatasourceConditionEntity(ResponseConditionDatasourceRequest request) {
        ParameterDatasourceConditionEntity entity = new ParameterDatasourceConditionEntity();
        ParameterDatasourceEntity datasourceEntity = new ParameterDatasourceEntity();
        //Create datasource
        datasourceEntity.setProperty(request.getProperty());
        datasourceEntity.setValue(request.getValue());
        datasourceEntity.setLength(request.getLength());
        datasourceEntity.setConvertorCode(request.getConvertorCode());
        //Adding datasource
        entity.setParameter(datasourceEntity);
        entity.setConditionValue(request.getConditionValue());
        entity.setCreator(getCurrentUser());
        entity.setLastEditor(getCurrentUser());
        entity.setCreateDate(LocalDateTime.now());
        entity.setOperation(request.getOperation());
        entity.setLastEditDate(LocalDateTime.now());
        return entity;
    }

    private void validateResponseConditionDatasourceRequest(ResponseConditionDatasourceRequest request) {
        ChainValidation
                .crateValidator(request.getResponseConditionId(), "responseConditionId")
                .checkNull().checkNumeral();
        ValidationUtils.checkNull(request.getProperty(), () -> new InvalidInputException("property"));
        ValidationUtils.checkBlankStringIfNotNull(request.getValue(), () -> new InvalidInputException("value"));
        ValidationUtils.checkBlankStringIfNotNull(request.getConvertorCode(), () -> new InvalidInputException("convertorCode"));
        ValidationUtils.checkBlankStringIfNotNull(request.getConditionValue(), () -> new InvalidInputException("conditionValue"));
        ValidationUtils.checkNumericInputIfNotNull(request.getLength(), () -> new InvalidInputException("length"));
    }

    @Override
    @Transactional
    public Parameter create(ParameterCreateRequest request) {
        validateParameterCreateRequest(request);
        ParameterEntity parameterEntity = createParameterEntity(request);
        parameterEntity = applyParameterRelation(request, parameterEntity);
        ParameterParser.clearCache();
        return ParameterMapper.INSTANCE.toModel(parameterEntity);
    }

    private ParameterEntity applyParameterRelation(ParameterCreateRequest request, ParameterEntity parameterEntity) {
        String serviceProviderId = request.getServiceProviderId();
        String serviceId = request.getServiceId();
        Long responseConditionId = request.getResponseConditionId();
        if (Objects.nonNull(serviceProviderId)) {
            AbstractExternalServiceProviderEntity provider = serviceProviderRepository.findById(serviceProviderId).orElseThrow(() -> new InvalidInputException("serviceProviderId"));
            parameterEntity.setServiceProvider(provider);
        } else if (Objects.nonNull(request.getApplyOnProvider()) && request.getApplyOnProvider()) {
            ServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new InvalidInputException("serviceId"));
            parameterEntity.setService(serviceEntity);
        } else if (Objects.nonNull(serviceId)) {
            ServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new InvalidInputException("serviceId"));
            parameterEntity.setService(serviceEntity);
        } else if (Objects.nonNull(responseConditionId)) {
            ResponseConditionEntity entity = responseConditionRepository.findById(responseConditionId).orElseThrow(() -> new InvalidInputException("responseConditionId"));
            parameterEntity.setResponseCondition(entity);
        }
        return parameterRepository.save(parameterEntity);
    }

    private ParameterEntity createParameterEntity(ParameterCreateRequest request) {
        ParameterEntity entity = new ParameterEntity();
        entity.setName(request.getParameterName());
        //Creating datasource
        ParameterDatasourceEntity datasource = new ParameterDatasourceEntity();
        datasource.setProperty(request.getDatasourcePropertyType());
        datasource.setValue(request.getValue());
        datasource.setLength(request.getLength());
        datasource.setConvertorCode(request.getConvertorCode());
        //Adding datasource
        entity.setDatasource(datasource);
        entity.setType(request.getParameterType());
        entity.setInternal(request.isInternal());
        entity.setTag(request.getTag());
        entity.setRequired(request.isRequired());
        entity.setOrder(request.getOrder());
        if (Objects.nonNull(request.getParentId())) {
            entity.setParent(findParameterParent(request.getParentId()));
        }
        entity.setActionType(request.getActionType());
        entity.setDefaultValue(request.getDefaultValue());
        entity.setCreator(getCurrentUser());
        entity.setLastEditor(getCurrentUser());
        entity.setCreateDate(LocalDateTime.now());
        entity.setLastEditDate(LocalDateTime.now());
        return entity;
    }

    private ParameterEntity findParameterParent(Long parentId) {
        return parameterRepository
                .findById(parentId)
                .orElseThrow(() -> new NoMatchRecordFoundException("parentId"));
    }

    private void validateParameterCreateRequest(ParameterCreateRequest request) {
        ValidationUtils.checkNull(request.getDatasourcePropertyType(), () -> new MissingRequiredInputException("datasourcePropertyType"));
        ValidationUtils.checkNull(request.getActionType(), () -> new MissingRequiredInputException("actionType"));
        ValidationUtils.checkNull(request.getParameterType(), () -> new MissingRequiredInputException("parameterType"));
        ValidationUtils.checkBlankStringIfNotNull(request.getValue(), () -> new MissingRequiredInputException("value"));
        ValidationUtils.checkBlankStringIfNotNull(request.getTag(), () -> new InvalidInputException("tag"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getDefaultValue()), () -> new InvalidInputException("defaultValue"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getOrder()), () -> new InvalidInputException("order"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getParentId()), () -> new InvalidInputException("parentId"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getConvertorCode()), () -> new InvalidInputException("convertorCode"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getApplyOnProvider()), () -> new InvalidInputException("applyOnProvider"));
        checkParameterTargetId(request);
    }

    private void checkParameterTargetId(ParameterCreateRequest request) {
        if (Objects.isNull(request.getServiceId())
            && Objects.isNull(request.getServiceProviderId())
            && Objects.isNull(request.getResponseConditionId())) {
            throw new MissingRequiredInputException("targetId(serviceId,serviceProviderId,responseConditionId)");
        }
        //checking service provider if exists
        ChainValidation
                .crateValidator(request.getServiceProviderId(), "serviceProviderId")
                .breakCheckIfNull()
                .checkFunction((input) -> serviceProviderRepository.findById(String.valueOf(input)).isPresent());
        //checking service if exists
        ChainValidation
                .crateValidator(request.getServiceId(), "serviceId")
                .breakCheckIfNull()
                .checkFunction((input -> serviceRepository.findById(String.valueOf(input)).isPresent()));
        //checking response condition if exists
        ChainValidation
                .crateValidator(request.getResponseConditionId(), "responseConditionId")
                .breakCheckIfNull()
                .checkNumeral()
                .checkFunction((input -> responseConditionRepository.findById(Long.parseLong(String.valueOf(input))).isPresent()));
    }

    private String getCurrentUser() {
        return AuthenticationUtils.getLoggedInGlobalUsername();
    }
}
