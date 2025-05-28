package ir.daneshrefah.scm.common.data.service.error;

import ir.daneshrefah.scm.common.data.entity.error.ErrorMappingEntity;
import ir.daneshrefah.scm.common.data.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.common.data.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingCreateRequest;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingEditRequest;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RecordVersionException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorMappingService {
    private static final List<ErrorMapping> ERROR_MAPPINGS_CACHE = new ArrayList<>(100);
    private final ErrorMappingRepository errorMappingRepository;
    private final ErrorMappingMapper errorMappingMapper;


    @PostConstruct
    public void init() {
        synchronized (ERROR_MAPPINGS_CACHE) {
            errorMappingRepository.findAll().stream().map(errorMappingMapper::toModel).peek(errorMapping -> {
                String errorMessage = errorMapping.getErrorMessage();
                if (errorMessage.startsWith("${") && errorMessage.endsWith("}")) {
                    errorMapping.setBundleKey(true);
                    errorMapping.setErrorMessage(removeBundlePattern(errorMessage));
                }
            }).forEach(ERROR_MAPPINGS_CACHE::add);
            log.info(">>> All {} ErrorMapping has been cached.", ERROR_MAPPINGS_CACHE.size());
        }
    }

    public void reloadCache() {
        synchronized (ERROR_MAPPINGS_CACHE) {
            ERROR_MAPPINGS_CACHE.clear();
            init();
        }
    }

    public String errorMessageAsBundleKey(String errorMessage) {
        return "${" + errorMessage + "}";
    }

    private String removeBundlePattern(String errorMessage) {
        errorMessage = StringUtils.removeStart(errorMessage, "${");
        errorMessage = StringUtils.removeEnd(errorMessage, "}");
        return errorMessage;
    }

    public Optional<ErrorMapping> findByErrorMessage(String errorMessage) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getErrorMessage().equals(errorMessage))
                .findFirst();
    }

    public Optional<ErrorMapping> findByExceptionClassName(String className) {
        return findByErrorMessage(className);
    }

    public Optional<ErrorMapping> findByExceptionClassNameAndOverrideName(String className, String overrideName) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getErrorMessage().equals(className))
                .filter(errorMapping -> Objects.nonNull(errorMapping.getExceptionOverrideName()))
                .filter(errorMapping -> errorMapping.getExceptionOverrideName().equals(overrideName))
                .findFirst();
    }

    public Optional<ErrorMapping> findByExceptionClassNameAndErrorCode(String className, String errorCode) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getErrorMessage().equals(className))
                .filter(errorMapping -> errorMapping.getScmErrorCode().equals(Integer.parseInt(errorCode)))
                .findFirst();
    }

    public Optional<ErrorMapping> findByRemoteErrorCode(String remoteErrorCode) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getProviderErrorCode().equals(remoteErrorCode))
                .findFirst();
    }

    public Optional<ErrorMapping> findByRemoteErrorCodeAndProviderId(String remoteErrorCode, String providerId) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> Objects.equals(errorMapping.getProviderErrorCode(), remoteErrorCode))
                .filter(errorMapping -> Objects.equals(errorMapping.getProviderId(), providerId))
                .findFirst();
    }

    public List<ErrorMapping> getErrorMappingsCache() {
        return ERROR_MAPPINGS_CACHE;
    }

    public ErrorMapping findRefreshRecord(long id) {
        ErrorMappingEntity foundEntity = errorMappingRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return errorMappingMapper.toModel(foundEntity);
    }

    public ErrorMapping findById(long id) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> Objects.equals(errorMapping.getId(), id))
                .findFirst()
                .orElseThrow(()->new NoMatchRecordFoundException("id"));
    }


    public ErrorMapping dynamicUpdate(ErrorMappingEditRequest request) {
        ErrorMapping refreshRecord = findRefreshRecord(Long.parseLong(request.getId()));
        if (!refreshRecord.getLastEditDate().equals(request.getLastEditDate())) {
            throw new RecordVersionException("lastEditDate");
        }
        prepareDynamicUpdate(refreshRecord, request);
        ErrorMappingEntity entity = errorMappingRepository.save(errorMappingMapper.toEntity(refreshRecord));
        reloadCache();
        return findById(entity.getId());
    }

    private void prepareDynamicUpdate(ErrorMapping refreshRecord, ErrorMappingEditRequest request) {
        refreshRecord.setBundleKey(request.getBundleKey());
        refreshRecord.setProviderId(request.getProviderId());
        refreshRecord.setErrorMessage(request.getBundleKey() ? errorMessageAsBundleKey(request.getErrorMessage()) : request.getErrorMessage());
        refreshRecord.setProviderErrorCode(request.getProviderErrorCode());
        refreshRecord.setExceptionOverrideName(request.getExceptionOverrideName());
        refreshRecord.setStatus(request.getStatus());
        refreshRecord.setScmErrorCode(request.getScmErrorCode());
    }

    public ErrorMapping create(ErrorMappingCreateRequest request) {
        ErrorMappingEntity mapping = mapToErrorMapping(request);
        ErrorMappingEntity entity = errorMappingRepository.save(mapping);
        reloadCache();
        return errorMappingMapper.toModel(entity);
    }

    private ErrorMappingEntity mapToErrorMapping(ErrorMappingCreateRequest request) {
        ErrorMappingEntity entity = new ErrorMappingEntity();
        entity.setProviderId(request.getProviderId());
        entity.setProviderErrorCode(request.getProviderErrorCode());
        entity.setExceptionOverrideName(request.getExceptionOverrideName());
        entity.setScmErrorCode(request.getScmErrorCode());
        entity.setStatus(request.getStatus());
        entity.setErrorMessage(request.getBundleKey() ? errorMessageAsBundleKey(request.getErrorMessage()) : request.getErrorMessage());
        entity.setCreateDate(LocalDateTime.now());
        entity.setLastEditDate(LocalDateTime.now());
        return entity;
    }

}
