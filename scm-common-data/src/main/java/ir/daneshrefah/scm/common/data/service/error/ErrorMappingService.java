package ir.daneshrefah.scm.common.data.service.error;

import ir.daneshrefah.scm.common.data.entity.error.ErrorMappingEntity;
import ir.daneshrefah.scm.common.data.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.common.data.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RecordVersionException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.service.error.ErrorMappingEditRequest;
import ir.daneshrefah.scm.utils.data.DynamicUpdateUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            errorMappingRepository
                    .findAll()
                    .stream()
                    .map(ErrorMappingMapper.INSTANCE::toModel)
                    .forEach(ERROR_MAPPINGS_CACHE::add);
            log.info(">>> All {} ErrorMapping has been cached.", ERROR_MAPPINGS_CACHE.size());
        }
    }

    public void reloadCache(){
        synchronized (ERROR_MAPPINGS_CACHE){
            ERROR_MAPPINGS_CACHE.clear();
            init();
        }
    }

    public Optional<ErrorMapping> findByExceptionClassName(String className) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getExceptionClassName().equals(className))
                .findFirst();
    }

    public Optional<ErrorMapping> findByExceptionClassNameAndOverrideName(String className, String overrideName) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getExceptionClassName().equals(className))
                .filter(errorMapping -> Objects.nonNull(errorMapping.getExceptionOverrideName()))
                .filter(errorMapping -> errorMapping.getExceptionOverrideName().equals(overrideName))
                .findFirst();
    }

    public Optional<ErrorMapping> findByExceptionClassNameAndErrorCode(String className, String errorCode) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getExceptionClassName().equals(className))
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
                .filter(errorMapping -> errorMapping.getProviderErrorCode().equals(remoteErrorCode))
                .filter(errorMapping -> errorMapping.getProviderId().equals(providerId))
                .findFirst();
    }

    public List<ErrorMapping> getErrorMappingsCache() {
        return ERROR_MAPPINGS_CACHE;
    }

    public ErrorMapping findRefreshRecord(long id) {
        ErrorMappingEntity foundEntity = errorMappingRepository
                .findById(id)
                .orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return ErrorMappingMapper.INSTANCE.toModel(foundEntity);
    }


    public ErrorMapping dynamicUpdate(ErrorMappingEditRequest request) {
        ErrorMapping refreshRecord = findRefreshRecord(Long.parseLong(request.getId()));
        if (!refreshRecord.getLastEditDate().equals(request.getLastEditDate())){
            throw new RecordVersionException("lastEditDate");
        }
        prepareDynamicUpdate(refreshRecord,request);
        errorMappingRepository.save(ErrorMappingMapper.INSTANCE.toEntity(refreshRecord));
        reloadCache();
        return refreshRecord;
    }

    private void prepareDynamicUpdate(ErrorMapping refreshRecord, ErrorMappingEditRequest request) {
        DynamicUpdateUtils.applyChangesIfNotNull(request.getStatus(),refreshRecord::setStatus);
        DynamicUpdateUtils.applyChangesIfNotNull(request.getScmErrorCode(),refreshRecord::setScmErrorCode);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getProviderId(),refreshRecord::setProviderId);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getExceptionOverrideName(),refreshRecord::setExceptionOverrideName);
        DynamicUpdateUtils.applyChangesIfNotBlank(request.getProviderErrorCode(),refreshRecord::setProviderErrorCode);
    }
}
