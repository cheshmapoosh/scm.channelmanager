package ir.daneshrefah.scm.common.data.service.error;

import ir.daneshrefah.scm.common.data.mapper.ErrorMappingMapper;
import ir.daneshrefah.scm.common.data.repository.ErrorMappingRepository;
import ir.daneshrefah.scm.common.error.ErrorMapping;
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
        errorMappingRepository
                .findAll()
                .stream()
                .map(ErrorMappingMapper.INSTANCE::toModel)
                .forEach(ERROR_MAPPINGS_CACHE::add);
        log.info(">>> All {} ErrorMapping has been cached.", ERROR_MAPPINGS_CACHE.size());
    }

    public Optional<ErrorMapping> findByExceptionClassName(String className) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getExceptionClassName().equals(className))
                .findFirst();
    }

    public Optional<ErrorMapping> findByExceptionClassNameAndOverrideName(String className,String overrideName) {
        return ERROR_MAPPINGS_CACHE
                .stream()
                .filter(errorMapping -> errorMapping.getExceptionClassName().equals(className))
                .filter(errorMapping -> Objects.nonNull(errorMapping.getExceptionOverrideName()))
                .filter(errorMapping -> errorMapping.getExceptionOverrideName().equals(overrideName))
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
}
