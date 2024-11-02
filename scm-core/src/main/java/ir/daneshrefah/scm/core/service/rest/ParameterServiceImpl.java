package ir.daneshrefah.scm.core.service.rest;

import ir.daneshrefah.scm.common.constant.ParameterAutoCompleteDefinition;
import ir.daneshrefah.scm.common.constant.ParameterAutoCompleteProperty;
import ir.daneshrefah.scm.common.constant.ParameterTarget;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.*;
import ir.daneshrefah.scm.common.model.dynamic.rest.ParameterNode;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.rest.*;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import ir.daneshrefah.scm.core.mapper.ParameterMapper;
import ir.daneshrefah.scm.core.mapper.ResponseMapper;
import ir.daneshrefah.scm.core.mapper.ServiceMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.core.service.ParameterParser;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ChainValidation;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType.*;

@Service
@RequiredArgsConstructor
@Validated
public class ParameterServiceImpl implements ParameterService {

    private final static Map<String, List<String>> PARAMETER_AUTO_COMPLETE_CACHE = new ConcurrentHashMap<>();
    private final ParameterResponseRelationRepository responseRelationRepository;
    private final ParameterProviderRelationRepository providerRelationRepository;
    private final ParameterServiceRelationRepository serviceRelationRepository;
    private final ResponseRepository responseRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ParameterRepository parameterRepository;
    private final ServiceRepository serviceRepository;
    private final ParameterParser parameterParser;
    private final ServiceService serviceService;


    @Override
    @Transactional
    public Parameter remove(ParameterDeleteRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ParameterEntity entity = parameterRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkParameterRecordChildren(entity);
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        removeParameterCompletely(entity);
        evictEffectedCache();
        return ParameterMapper.INSTANCE.toModel(entity);
    }

    private void removeParameterCompletely(ParameterEntity entity) {
        ServiceEntity service = entity.getService();
        ResponseEntity responseCondition = entity.getResponseCondition();
        AbstractExternalServiceProviderEntity serviceProvider = entity.getServiceProvider();
        if (Objects.nonNull(service)) {
            service.getParameters().remove(entity);
        }
        if (Objects.nonNull(responseCondition)) {
            responseCondition.getResponseParameters().remove(entity);
        }
        if (Objects.nonNull(serviceProvider)) {
            serviceProvider.getParameters().remove(entity);
        }
        parameterRepository.delete(entity);

    }

    private void evictEffectedCache(){
        serviceService.cacheEvict();
        parameterParser.clearCache();
    }

    private void checkParameterRecordChildren(ParameterEntity entity) {
        if (!parameterRepository
                .findAllByParent(entity)
                .isEmpty()) {
            throw new RemoveParentRecordException("parent");
        }
    }


    @Override
    public Parameter findParameterById(String id) {
        ValidationUtils.checkNull(id, () -> new InvalidInputException("id"));
        ValidationUtils.checkBlankString(id, () -> new InvalidInputException("id"));
        ParameterEntity entity = parameterRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return ParameterMapper.INSTANCE.toModel(entity);
    }

    @Override
    public List<AutoComplete> autoCompleteParameter(ParameterAutoCompleteSearchRequest request) {
        validateParameterAutoCompleteSearchRequest(request);
        ParameterTarget targetType = ParameterTarget.fromValue(request.getTargetType());
        ParameterAutoCompleteProperty propertyName = ParameterAutoCompleteProperty.fromValue(request.getPropertyName());
        ParameterActionType actionType = ParameterActionType.findByValue(request.getParameterActionType());
        String cacheKey = targetType.name() + "::" + propertyName.name() + "::" + request.getParameterActionType();
        return PARAMETER_AUTO_COMPLETE_CACHE.computeIfAbsent(cacheKey, (key) -> {
                    Set<String> fieldsName = new HashSet<>();
                    ParameterAutoCompleteDefinition.find(targetType, actionType, propertyName)
                            .forEach(autoCompleteDefinition -> {
                                addToSet(fieldsName, autoCompleteDefinition);
                            });
                    return fieldsName.stream().toList();
                })
                .stream().filter(value -> StringUtils.isBlank(request.getSearch()) || value.toLowerCase().contains(request.getSearch().toLowerCase()))
                .map(param -> {
                    AutoComplete response = new AutoComplete();
                    response.setTitle(param);
                    response.setValue(param);
                    return response;
                })
                .toList();
    }

    private void addToSet(Set<String> fieldsName, ParameterAutoCompleteDefinition autoCompleteDefinition) {
        List<String> ignoreList = Arrays.stream(autoCompleteDefinition.getIgnoreProperties().split(",")).toList();
        Class<?> modelClass = autoCompleteDefinition.getModelClass();
        Arrays.stream(modelClass.getDeclaredFields()).forEach(field ->
        {
            if (!ignoreList.contains(field.getName())) {
                String prefix = autoCompleteDefinition.getPrefix();
                fieldsName.add(StringUtils.isBlank(prefix) ? field.getName() : prefix + "." + field.getName());
            }
        });
    }

    private void validateParameterAutoCompleteSearchRequest(ParameterAutoCompleteSearchRequest request) {
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request"));
        ValidationUtils.checkBlankString(request.getTargetType(), () -> new MissingRequiredInputException("targetType"));
        ValidationUtils.checkBlankString(request.getPropertyName(), () -> new MissingRequiredInputException("propertyName"));
        ValidationUtils.checkBlankStringIfNotNull(request.getParameterActionType(), () -> new InvalidInputException("actionType"));
    }

    @Override
    public List<ParameterActionTypeFindResponse> findParameterActionTypeList(ParameterActionTypeFindRequest request) {
        ParameterActionTypeFindRequest.ActionTypeUsage actionTypeUsage = ParameterActionTypeFindRequest.ActionTypeUsage.fromValue(request.getUsage());
        ValidationUtils.checkNull(actionTypeUsage, () -> new MissingRequiredInputException("usage"));
        assert actionTypeUsage != null;
        return (switch (actionTypeUsage) {
            case REQUEST -> List.of(REQUEST_HEADER, REQUEST_BODY, REQUEST_QUERY_STRING, REQUEST_PATH_VARIABLE, CONFIG);
            case RESPONSE -> List.of(RESPONSE_BODY, RESPONSE_HEADER, CONFIG);
            case CONFIG -> List.of(CONFIG);
            default -> Arrays.stream(values()).toList();
        }).stream().map(parameterActionType -> {
            ParameterActionTypeFindResponse response = new ParameterActionTypeFindResponse();
            response.setTitle(parameterActionType.name());
            response.setValue(parameterActionType.name());
            return response;
        }).toList();
    }

    @Override
    public ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request) {
        ValidationUtils.checkNull(request.getServiceId(), () -> new InvalidInputException("serviceId"));
        ValidationUtils.checkBlankStringIfNotNull(request.getActionType(), () -> new InvalidInputException("actionType"));
        ValidationUtils.checkBlankString(request.getServiceId(), () -> new InvalidInputException("serviceId"));
        ValidationUtils.checkBlankString(request.getActionType(), () -> new InvalidInputException("actionType"));
        ServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new NoMatchRecordFoundException("serviceID"));
        ParameterParser.ServiceParameterCache parametersCache = parameterParser.getParametersCache(ServiceMapper.INSTANCE.toService(serviceEntity));
        ParameterActionType actionType = ParameterActionType.findByValue(request.getActionType());
        String responseId = request.getResponseId();
        Response responseCondition = null;
        if (actionType.equals(ParameterActionType.RESPONSE_BODY) && Objects.isNull(responseId)) {
            throw new InvalidInputException("responseId");
        }
        if (Objects.nonNull(responseId)) {
            ResponseEntity entity = responseRepository.findById(responseId).orElseThrow(() -> new NoMatchRecordFoundException("responseId"));
            responseCondition = ResponseMapper.INSTANCE.toModel(entity);
        }
        ParameterNode foundNode = switch (actionType) {
            case REQUEST_BODY -> parametersCache.getRequestBodyNode();
            case REQUEST_HEADER -> parametersCache.getRequestHeaderVariableNode();
            case REQUEST_PATH_VARIABLE -> parametersCache.getRequestPathVariableNode();
            case REQUEST_QUERY_STRING -> parametersCache.getRequestQueryStringVariableNode();
            case RESPONSE_BODY -> parametersCache.getResponseCache().getResponseBodyNode(responseCondition);
            case RESPONSE_HEADER -> parametersCache.getResponseHeaderVariableNode();
            case CONFIG -> parametersCache.getConfig();
        };
        return new ParameterTreeFindResponse().setTree(foundNode);
    }


    @Override
    public PagedResponseData<Parameter> findParameter(ParameterFindRequest request) {
        String serviceProviderId = request.getServiceProviderId();
        String serviceId = request.getServiceId();
        String responseConditionId = request.getResponseId();
        validateParameterFindRequest(request);
        List<ParameterEntity> parameters;
        if (Objects.nonNull(serviceProviderId)) {
            parameters = serviceProviderRepository.findById(serviceProviderId).orElseThrow(() -> new InvalidInputException("serviceProviderId")).getParameters();
        } else if (Objects.nonNull(serviceId)) {
            ServiceEntity serviceEntity = serviceRepository.findById(serviceId).orElseThrow(() -> new InvalidInputException("serviceId"));
            parameters = serviceEntity.getParameters();
        } else {
            parameters = responseRepository.findById(responseConditionId).orElseThrow(() -> new InvalidInputException("responseId")).getResponseParameters();
        }
        return applyParameterFindFilters(parameters, request);
    }

    private PagedResponseData<Parameter> applyParameterFindFilters(List<ParameterEntity> parameters, ParameterFindRequest request) {
        //REPLACE EMPTY TITLE FOR FRONT-END HANDLING
        return new PagedResponseData<>(request,
                parameters
                        .stream()
                        .map(ParameterMapper.INSTANCE::toModel)
                        .filter(parameter -> Objects.isNull(request.getParameterName()) || parameter.getName().contains(request.getParameterName()))
                        .filter(parameter -> Objects.isNull(request.getTitle()) || parameter.getTitle().contains(request.getTitle()))
                        .filter(parameter -> Objects.isNull(request.getParentId()) || Objects.isNull(parameter.getParent()) || parameter.getParent().getId().equals(request.getParentId()))
                        .filter(parameter -> Objects.isNull(request.getActionType()) || parameter.getActionType().equals(ParameterActionType.findByValue(request.getActionType())))
                        .peek(this::assignParameterTitle)
                        .toList());
    }

    private void assignParameterTitle(Parameter parameter) {
        if (Objects.nonNull(parameter.getParent())) {
            assignParameterTitle(parameter.getParent());
        }
        String title = parameter.getTitle();
        if (StringUtils.isBlank(title)) {
            title = parameter.getName();
        }
        if (StringUtils.isBlank(title)) {
            ParameterType type = parameter.getType();
            title = "[" + type.name() + "]";
        }
        parameter.setTitle(title);
    }

    private void validateParameterFindRequest(ParameterFindRequest request) {
        final AtomicBoolean hasParameterTargetId = new AtomicBoolean(false);
        ValidationUtils.checkBlankStringIfNotNull(request.getParameterName(), () -> new InvalidInputException("parameterName"));
        ChainValidation.crateValidator(request.getActionType(), "actionType").breakCheckIfNull()
                .checkFunction(input -> !Objects.isNull(ParameterActionType.findByValue(String.valueOf(input))));
        ChainValidation.crateValidator(request.getParentId(), "parentId").breakCheckIfNull()
                .checkFunction((input) -> parameterRepository.findById(String.valueOf(input)).isPresent());
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
        ChainValidation.crateValidator(request.getResponseId(), "responseId").breakCheckIfNull()
                .checkFunction(input -> {
                    boolean present = responseRepository.findById(String.valueOf(input)).isPresent();
                    hasParameterTargetId.set(present);
                    return present;
                });
        if (!hasParameterTargetId.get()) {
            throw new MissingRequiredInputException("parameter target relation id");
        }
    }


    @Override
    @Transactional
    public Parameter change(ParameterChangeRequest request) {
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        validateParameterLength(request.getParameterType(), request.getLength());
        ParameterEntity entity = parameterRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        DynamicUpdateUtils.applyChangesIfNotNull(request.getParentId(), parentId -> {
            ParameterEntity parent = parameterRepository.findById(parentId).orElseThrow(() -> new InvalidInputException("parentId"));
            entity.setParent(parent);
        });
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getParameterName(), entity::setName);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getTitle(), entity::setTitle);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getDefaultValue(), entity::setDefaultValue);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getTag(), entity::setTag);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getActionType(), entity::setActionType);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getOrder(), entity::setOrder);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getRequired(), entity::setRequired);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getParameterType(), entity::setType);
        ParameterDatasourceEntity datasource = entity.getDatasource();
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getValue(), datasource::setValue);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getConvertorCode(), datasource::setConvertorCode);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getLength(), datasource::setLength);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getDatasourcePropertyType(), datasource::setProperty);
        entity.setLastEditDate(LocalDateTime.now());
        entity.setLastEditor(getCurrentUser());
        ParameterEntity save = parameterRepository.save(entity);
        evictEffectedCache();
        return ParameterMapper.INSTANCE.toModel(save);
    }

    private void checkOptimisticRecordVersion(LocalDateTime request, LocalDateTime entity) {
        if (!request.equals(entity)) {
            throw new RecordVersionException("lastEditDate");
        }
    }

    @Override
    @Transactional
    public Parameter create(ParameterCreateRequest request) {
        validateParameterCreateRequest(request);
        ParameterEntity parameterEntity = createParameterEntity(request);
        parameterEntity = applyParameterRelation(request, parameterEntity);
        evictEffectedCache();
        return ParameterMapper.INSTANCE.toModel(parameterEntity);
    }

    private void validateParameterLength(ParameterType parameterType, Integer length) {
        if ((parameterType.equals(ParameterType.OBJECT) ||
             parameterType.equals(ParameterType.BOOLEAN)) &&
            Objects.nonNull(length)) {
            throw new InvalidInputException("length");
        }
    }

    private ParameterEntity applyParameterRelation(ParameterCreateRequest request, ParameterEntity parameterEntity) {
        String serviceProviderId = request.getServiceProviderId();
        String serviceId = request.getServiceId();
        String responseConditionId = request.getResponseId();
        if (Objects.nonNull(serviceProviderId)) {
            AbstractExternalServiceProviderEntity provider = serviceProviderRepository.findById(serviceProviderId).orElseThrow(() -> new InvalidInputException("serviceProviderId"));
            parameterEntity.setServiceProvider(provider);
        } else if (Objects.nonNull(serviceId)) {
            ServiceEntity serviceEntity = serviceRepository.findById(request.getServiceId()).orElseThrow(() -> new InvalidInputException("serviceId"));
            parameterEntity.setService(serviceEntity);
        } else if (Objects.nonNull(responseConditionId)) {
            ResponseEntity entity = responseRepository.findById(responseConditionId).orElseThrow(() -> new InvalidInputException("responseConditionId"));
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
        entity.setTag(request.getTag());
        entity.setRequired(request.isRequired());
        entity.setOrder(request.getOrder());
        if (Objects.nonNull(request.getParentId())) {
            entity.setParent(findParameterParent(request.getParentId()));
        }
        entity.setTitle(request.getTitle());
        entity.setActionType(request.getActionType());
        entity.setDefaultValue(request.getDefaultValue());
        entity.setCreator(getCurrentUser());
        entity.setLastEditor(getCurrentUser());
        entity.setCreateDate(LocalDateTime.now());
        entity.setLastEditDate(LocalDateTime.now());
        return entity;
    }

    private ParameterEntity findParameterParent(String parentId) {
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
        ValidationUtils.checkBlankStringIfNotNull(request.getTitle(), () -> new InvalidInputException("title"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getDefaultValue()), () -> new InvalidInputException("defaultValue"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getOrder()), () -> new InvalidInputException("order"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getParentId()), () -> new InvalidInputException("parentId"));
        ValidationUtils.checkBlankStringIfNotNull(String.valueOf(request.getConvertorCode()), () -> new InvalidInputException("convertorCode"));
        validateParameterLength(request.getParameterType(), request.getLength());
        checkParameterTargetId(request);
    }

    private void checkParameterTargetId(ParameterCreateRequest request) {
        if (Objects.isNull(request.getServiceId())
            && Objects.isNull(request.getServiceProviderId())
            && Objects.isNull(request.getResponseId())) {
            throw new MissingRequiredInputException("targetId(serviceId,serviceProviderId,responseId)");
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
                .crateValidator(request.getResponseId(), "responseId")
                .breakCheckIfNull()
                .checkFunction((input -> responseRepository.findById(String.valueOf(input)).isPresent()));
    }

    private String getCurrentUser() {
        return AuthenticationUtils.getLoggedInGlobalUsername();
    }
}
