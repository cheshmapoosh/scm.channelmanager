package ir.daneshrefah.scm.core.service.rest;

import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.dto.rest.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RecordVersionException;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.model.service.parameter.DatasourceConditionOperation;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.common.service.rest.DynamicRestService;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.service.composition.CompositionServiceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceConditionEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import ir.daneshrefah.scm.core.mapper.ParameterDatasourceConditionMapper;
import ir.daneshrefah.scm.core.mapper.ResponseMapper;
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

@Service
@RequiredArgsConstructor
public class DynamicRestServiceImpl implements DynamicRestService {
    private static final List<RestExternalProviderResponse> EXTERNAL_PROVIDER_NAME_CACHE = new ArrayList<>();
    private final ParameterDatasourceConditionRepository datasourceConditionRepository;
    private final ResponseRepository responseConditionRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final TransformerRepository transformerRepository;
    private final ErrorMappingService errorMappingService;
    private final ServiceRepository serviceRepository;
    private final ServiceServiceImpl serviceServiceImpl;
    private final ParameterParser parameterParser;


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

    private void evictEffectCache() {
        parameterParser.clearCache();
        serviceServiceImpl.cacheEvict();
    }

    @Override
    public Response removeResponse(ResponseDeleteRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ResponseEntity entity = responseConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        responseConditionRepository.delete(entity);
        evictEffectCache();
        return ResponseMapper.INSTANCE.toModel(entity);
    }

    @Override
    public List<ParameterDatasourceCondition> findResponseConditionDatasourceList(ResponseConditionDatasourceFindRequest request) {
        ValidationUtils.checkNull(request.getResponseId(), () -> new MissingRequiredInputException("responseId"));
        ResponseEntity entity = responseConditionRepository.findById(request.getResponseId()).orElseThrow(() -> new InvalidInputException("responseId"));
        return entity.getConditions()
                .stream()
                .map(ParameterDatasourceConditionMapper.INSTANCE::toModel)
                .toList();
    }

    @Override
    public PagedResponseData<Response> findResponse(ResponseFindRequest request) {
        String providerId = request.getServiceProviderId();
        String serviceId = request.getServiceId();
        if (Objects.isNull(providerId) && Objects.isNull(serviceId)) {
            throw new MissingRequiredInputException("serviceId or serviceProviderId is empty");
        }
        List<Response> responseConditions = new ArrayList<>();
        if (Objects.nonNull(providerId) && !providerId.isBlank()) {
            if (Objects.nonNull(serviceId)) {
                throw new InvalidInputException("service Id must be empty");
            }
            responseConditions = serviceProviderRepository
                    .findById(providerId).orElseThrow(() -> new NoMatchRecordFoundException("providerId"))
                    .getResponseConditions().stream().map(ResponseMapper.INSTANCE::toModel).toList();
        }
        if (Objects.nonNull(serviceId) && !serviceId.isBlank()) {
            if (Objects.nonNull(providerId)) {
                throw new InvalidInputException("provider Id must be empty");
            }
            ServiceEntity service =  serviceRepository.findById(serviceId).orElseThrow(() -> new NoMatchRecordFoundException("serviceId"));
            if (service instanceof AbstractExternalServiceEntity<?> externalServiceEntity) {
                responseConditions = externalServiceEntity.getResponseList().stream().map(ResponseMapper.INSTANCE::toModel).toList();
            }else if (service instanceof CompositionServiceEntity compositionService){
                responseConditions = compositionService.getResponseList().stream().map(ResponseMapper.INSTANCE::toModel).toList();
            }
        }
        return new PagedResponseData<>(request, responseConditions);
    }

    @Override
    public ParameterDatasourceCondition removeResponseConditionDatasource(ResponseConditionDatasourceRemoveRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(), () -> new InvalidInputException("lastEditDate"));
        ParameterDatasourceConditionEntity entity = datasourceConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        datasourceConditionRepository.delete(entity);
        evictEffectCache();
        return ParameterDatasourceConditionMapper.INSTANCE.toModel(entity);
    }

    @Override
    public ParameterDatasourceCondition changeResponseConditionDatasource(ResponseConditionDatasourceChangeRequest request) {
        ParameterDatasourceConditionEntity entity = datasourceConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getConditionValue(), entity::setConditionValue);
        ParameterDatasourceEntity datasource = entity.getParameter();
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getConvertorCode(), datasource::setConvertorCode);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getValue(), datasource::setValue);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getLength(), datasource::setLength);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getProperty(), datasource::setProperty);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getOperation(), (value) -> entity.setOperation(DatasourceConditionOperation.findByValue(request.getOperation())));
        entity.setLastEditor(getCurrentUser());
        datasourceConditionRepository.save(entity);
        evictEffectCache();
        return ParameterDatasourceConditionMapper.INSTANCE.toModel(entity);
    }

    @Override
    @Transactional
    public Response changeResponse(ResponseChangeRequest request) {
        ResponseEntity entity = responseConditionRepository.findById(request.getId()).orElseThrow(() -> new InvalidInputException("id"));
        checkOptimisticRecordVersion(request.getLastEditDate(), entity.getLastEditDate());
        DynamicUpdateUtils.applyChangesIfNotNull(request.getTransformerId(), (transformerId -> {
            TransformerEntity transformer = transformerRepository
                    .findById(transformerId)
                    .orElseThrow(() -> new NoMatchRecordFoundException("transformerId"));
            entity.setResponseTransformer(transformer);
        }));
        DynamicUpdateUtils.applyChangesIfNotNull(request.getEnable(), entity::setEnable);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getTitle(), entity::setTitle);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getErrorCode(), entity::setResponseErrorCodeProperty);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getErrorMessage(), entity::setResponseErrorMessageProperty);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getResponseBodyType(), responseBodyType -> entity.setResponseBodyType(ExternalServiceBodyType.find(responseBodyType)));
        entity.setLastEditDate(LocalDateTime.now());
        entity.setLastEditor(getCurrentUser());
        responseConditionRepository.save(entity);
        evictEffectCache();
        return ResponseMapper.INSTANCE.toModel(entity);
    }

    @Override
    @Transactional
    public Response createResponse(ResponseCreateRequest request) {
        validateResponseConditionRequest(request);
        String transformerId = request.getTransformerId();
        ResponseEntity entity = new ResponseEntity();
        if (Objects.nonNull(transformerId)) {
            TransformerEntity transformerEntity = transformerRepository.findById(transformerId).orElseThrow(() -> new NoMatchRecordFoundException("transformerId"));
            entity.setResponseTransformer(transformerEntity);
        }
        entity.setResponseErrorCodeProperty(request.getErrorCode());
        entity.setTitle(request.getTitle());
        entity.setEnable(request.getEnable());
        String errorMessage = errorMappingService
                .findByExceptionClassNameAndErrorCode(request.getErrorMessage(), request.getErrorCode())
                .map(ErrorMapping::getErrorMessage)
                .orElse(request.getErrorMessage());
        entity.setResponseErrorMessageProperty(errorMessage);
        entity.setResponseBodyType(request.getResponseBodyType());
        responseConditionRepository.save(entity);
        setResponseConditionTargetId(request, entity);
        evictEffectCache();
        return ResponseMapper.INSTANCE.toModel(entity);
    }

    @Override
    @Transactional
    public ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request) {
        validateResponseConditionDatasourceRequest(request);
        ResponseEntity responseConditionEntity = responseConditionRepository
                .findById(request.getResponseId()).orElseThrow(() -> new NoMatchRecordFoundException("responseId"));
        List<ParameterDatasourceConditionEntity> conditions = responseConditionEntity.getConditions();
        if (Objects.isNull(conditions)) {
            conditions = new ArrayList<>();
        }
        ParameterDatasourceConditionEntity entity = createParameterDatasourceConditionEntity(request);
        entity = datasourceConditionRepository.save(entity);
        conditions.add(entity);
        responseConditionEntity.setConditions(conditions);
        responseConditionRepository.save(responseConditionEntity);
        evictEffectCache();
        return ParameterDatasourceConditionMapper.INSTANCE.toModel(entity);
    }


    private void setResponseConditionTargetId(ResponseCreateRequest request, ResponseEntity entity) {
        String serviceId = request.getServiceId();
        String serviceProviderId = request.getServiceProviderId();
        if (Objects.nonNull(serviceProviderId) && !serviceProviderId.isBlank()) {
            addResponseToProvider(serviceProviderId,entity);
        } else {
            addResponseToService(serviceId,entity);
        }
        evictEffectCache();
    }

    private void addResponseToService(String serviceId, ResponseEntity entity) {
        ServiceEntity serviceEntity = serviceRepository.findById(serviceId).orElseThrow(() -> new NoMatchRecordFoundException("serviceId"));
        List<ResponseEntity> responseConditions = null;
        boolean validService = false;
        if (serviceEntity instanceof RestExternalServiceEntity restService) {
            responseConditions = restService.getResponseList();
            restService.setResponseList(responseConditions);
            validService = true;
        } else if (serviceEntity instanceof CompositionServiceEntity compositionService) {
            responseConditions = compositionService.getResponseList();
            if (Objects.isNull(responseConditions)) {
                responseConditions = new ArrayList<>();
                compositionService.setResponseList(responseConditions);
            }
            validService = true;
        }
        if (validService) {
            responseConditions.add(entity);
            serviceRepository.save(serviceEntity);
        } else {
            throw new InvalidInputException("serviceId");
        }
    }

    private void addResponseToProvider(String serviceProviderId, ResponseEntity entity) {
        AbstractExternalServiceProviderEntity provider = serviceProviderRepository.findById(serviceProviderId).orElseThrow(() -> new NoMatchRecordFoundException("serviceProviderId"));
        List<ResponseEntity> responseConditions = provider.getResponseConditions();
        if (Objects.isNull(responseConditions)) {
            responseConditions = new ArrayList<>();
        }
        responseConditions.add(entity);
        serviceProviderRepository.save(provider);
    }


    private void validateResponseConditionRequest(ResponseCreateRequest request) {
        ValidationUtils.checkBlankStringIfNotNull(request.getTransformerId(), () -> new InvalidInputException("transformerId"));
        ValidationUtils.checkBlankStringIfNotNull(request.getErrorCode(), () -> new InvalidInputException("errorCode"));
        ValidationUtils.checkBlankStringIfNotNull(request.getErrorMessage(), () -> new InvalidInputException("errorMessage"));
        ValidationUtils.checkBlankStringIfNotNull(request.getTitle(), () -> new InvalidInputException("title"));
        ValidationUtils.checkBlankString(String.valueOf(request.getEnable()), () -> new InvalidInputException("status"));
        if (StringUtils.isBlank(request.getServiceId()) && StringUtils.isBlank(request.getServiceProviderId())) {
            throw new MissingRequiredInputException("targetId(serviceId or serviceProviderId)");
        }
        if (StringUtils.isNotBlank(request.getErrorMessage()) && StringUtils.isBlank(request.getErrorCode())) {
            throw new MissingRequiredInputException("errorCode");
        }
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
                .crateValidator(request.getResponseId(), "responseId")
                .checkNull();
        ValidationUtils.checkNull(request.getProperty(), () -> new InvalidInputException("property"));
        ValidationUtils.checkBlankStringIfNotNull(request.getValue(), () -> new InvalidInputException("value"));
        ValidationUtils.checkBlankStringIfNotNull(request.getConvertorCode(), () -> new InvalidInputException("convertorCode"));
        ValidationUtils.checkBlankStringIfNotNull(request.getConditionValue(), () -> new InvalidInputException("conditionValue"));
        ValidationUtils.checkNumericInputIfNotNull(request.getLength(), () -> new InvalidInputException("length"));
    }

    private void checkOptimisticRecordVersion(LocalDateTime request, LocalDateTime entity) {
        if (!request.equals(entity)) {
            throw new RecordVersionException("lastEditDate");
        }
    }

    private String getCurrentUser() {
        return AuthenticationUtils.getLoggedInGlobalUsername();
    }


}
