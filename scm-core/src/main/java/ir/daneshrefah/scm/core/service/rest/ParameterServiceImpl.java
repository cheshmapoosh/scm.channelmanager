package ir.daneshrefah.scm.core.service.rest;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import ir.daneshrefah.scm.common.service.rest.*;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceConditionEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterDatasourceEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseConditionEntity;
import ir.daneshrefah.scm.core.entity.service.rest.RestExternalServiceEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import ir.daneshrefah.scm.core.mapper.ParameterDatasourceConditionMapper;
import ir.daneshrefah.scm.core.mapper.ParameterMapper;
import ir.daneshrefah.scm.core.mapper.ResponseConditionMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
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
public class ParameterServiceImpl implements ParameterService {

    private final ParameterDatasourceConditionRepository datasourceConditionRepository;
    private final ResponseConditionRepository responseConditionRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final TransformerRepository transformerRepository;
    private final ParameterRepository parameterRepository;
    private final ServiceRepository serviceRepository;
    private static final List<RestExternalProviderResponse> EXTERNAL_PROVIDER_NAME_CACHE = new ArrayList<>();


    @Override
    public List<RestExternalProviderResponse> getRestExternalProviderNameList() {
        if (EXTERNAL_PROVIDER_NAME_CACHE.isEmpty()) {
           synchronized (this){
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
        entity.setResponseExceptionErrorMessageProperty(request.getErrorMessage());
        responseConditionRepository.save(entity);
        return ResponseConditionMapper.INSTANCE.toModel(entity);
    }

    private void validateResponseConditionRequest(ResponseConditionRequest request) {
        ValidationUtils.checkBlankStringIfNotNull(request.getTransformerId(), () -> new InvalidInputException("transformerId"));
        ValidationUtils.checkBlankStringIfNotNull(request.getErrorCode(), () -> new InvalidInputException("errorCode"));
        ValidationUtils.checkBlankStringIfNotNull(request.getErrorMessage(), () -> new InvalidInputException("errorMessage"));
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
        datasourceConditionRepository.save(entity);
        conditions.add(entity);
        responseConditionEntity.setConditions(conditions);
        responseConditionRepository.save(responseConditionEntity);
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
        parameterRepository.save(parameterEntity);
        applyParameterRelation(request, parameterEntity);
        return ParameterMapper.INSTANCE.toModel(parameterEntity);
    }

    private void applyParameterRelation(ParameterCreateRequest request, ParameterEntity parameterEntity) {
        String serviceProviderId = request.getServiceProviderId();
        String serviceId = request.getServiceId();
        Long responseConditionId = request.getResponseConditionId();
        if (Objects.nonNull(serviceProviderId)) {
            updateServiceProviderParameters(serviceProviderId, parameterEntity);
        } else if (Objects.nonNull(serviceId)) {
            updateServiceParameters(serviceId, parameterEntity);
        } else if (Objects.nonNull(responseConditionId)) {
            updateResponseConditionParameters(responseConditionId, parameterEntity);
        }
    }

    private void updateResponseConditionParameters(Long responseConditionId, ParameterEntity parameterEntity) {
        responseConditionRepository
                .findById(responseConditionId)
                .ifPresent(responseCondition -> {
                    List<ParameterEntity> responseParameters = responseCondition.getResponseParameters();
                    if (Objects.isNull(responseParameters)) {
                        responseParameters = new ArrayList<>();
                    }
                    responseParameters.add(parameterEntity);
                    responseCondition.setResponseParameters(responseParameters);
                    responseConditionRepository.save(responseCondition);
                });
    }

    private void updateServiceParameters(String serviceId, ParameterEntity parameterEntity) {
        serviceRepository
                .findById(serviceId)
                .ifPresent(service -> {
                    RestExternalServiceEntity restService = (RestExternalServiceEntity) service;
                    List<ParameterEntity> parameters = restService.getParameters();
                    if (Objects.isNull(parameters)) {
                        parameters = new ArrayList<>();
                    }
                    parameters.add(parameterEntity);
                    restService.setParameters(parameters);
                    serviceRepository.save(restService);
                });
    }

    private void updateServiceProviderParameters(String serviceProviderId, ParameterEntity parameterEntity) {
        serviceProviderRepository
                .findById(serviceProviderId)
                .ifPresent(serviceProvider -> {
                    List<ParameterEntity> parameters = serviceProvider.getParameters();
                    if (Objects.isNull(parameters)) {
                        parameters = new ArrayList<>();
                    }
                    parameters.add(parameterEntity);
                    serviceProvider.setParameters(parameters);
                    serviceProviderRepository.save(serviceProvider);
                });
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
        entity.setParent(findParameterParent(request.getParentId()));
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
        ValidationUtils.checkNumericInput(request.getParentId(), () -> new InvalidInputException("parentId"));
        checkParameterTargetId(request);
    }

    private void checkParameterTargetId(ParameterCreateRequest request) {
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
