package ir.daneshrefah.scm.common.data.service.error;

import ir.daneshrefah.scm.common.data.entity.error.ErrorMappingEntity;
import ir.daneshrefah.scm.common.data.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.common.data.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingEditRequest;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RecordVersionException;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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



    @PostConstruct
    public void init() {
        synchronized (ERROR_MAPPINGS_CACHE) {
            errorMappingRepository.findAll().stream().map(ErrorMappingMapper.INSTANCE::toModel).peek(errorMapping -> {
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
                .filter(errorMapping -> Objects.equals(errorMapping.getProviderErrorCode(),remoteErrorCode))
                .filter(errorMapping -> Objects.equals(errorMapping.getProviderId(),providerId))
                .findFirst();
    }

    public List<ErrorMapping> getErrorMappingsCache() {
        return ERROR_MAPPINGS_CACHE;
    }

    public ErrorMapping findRefreshRecord(long id) {
        ErrorMappingEntity foundEntity = errorMappingRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return ErrorMappingMapper.INSTANCE.toModel(foundEntity);
    }


    public ErrorMapping dynamicUpdate(ErrorMappingEditRequest request) {
        ErrorMapping refreshRecord = findRefreshRecord(Long.parseLong(request.getId()));
        if (!refreshRecord.getLastEditDate().equals(request.getLastEditDate())) {
            throw new RecordVersionException("lastEditDate");
        }
        prepareDynamicUpdate(refreshRecord, request);
        errorMappingRepository.save(ErrorMappingMapper.INSTANCE.toEntity(refreshRecord));
        reloadCache();
        return refreshRecord;
    }

    private void prepareDynamicUpdate(ErrorMapping refreshRecord, ErrorMappingEditRequest request) {
        DynamicUpdateUtils.applyChangesIfNotNull(request.getStatus(), refreshRecord::setStatus);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getScmErrorCode(), refreshRecord::setScmErrorCode);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getProviderId(), refreshRecord::setProviderId);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getExceptionOverrideName(), refreshRecord::setExceptionOverrideName);
        DynamicUpdateUtils.applyChangesIfNotBlankOrNull(request.getProviderErrorCode(), refreshRecord::setProviderErrorCode);
    }
}
