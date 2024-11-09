package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingEditRequest;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingFindRequest;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingSearchRequest;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExceptionManagementService extends AbstractJavaService {

    private final ErrorMappingService errorMappingService;

    public ExceptionManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper,ErrorMappingService errorMappingService) {
        super(producerTemplate, objectMapper);
        this.errorMappingService = errorMappingService;
    }

    @JavaService
    @SuppressWarnings("unused")
    public PagedResponseData<ErrorMapping> list(ErrorMappingFindRequest request){
        List<ErrorMapping> errorMappingsList = errorMappingService.getErrorMappingsCache();
        List<ErrorMapping> result = errorMappingsList
                .stream()
                .filter(error -> null == request || null == request.getStatus() || request.getStatus().equals(error.getStatus()))
                .filter(error -> null == request || null == request.getProviderId() || request.getProviderId().equals(error.getProviderId()))
                .filter(error -> null == request || null == request.getExceptionOverrideName() || error.getExceptionOverrideName().toLowerCase().contains(request.getExceptionOverrideName().toLowerCase()))
                .filter(error -> null == request || null == request.getScmErrorCode() || request.getScmErrorCode().equals(error.getScmErrorCode()))
                .filter(error -> null == request || null == request.getProviderErrorCode() || request.getProviderErrorCode().equals(error.getProviderErrorCode()))
                .filter(error -> null == request || null == request.getExceptionClassName() || error.getErrorMessage().toLowerCase().contains(request.getExceptionClassName().toLowerCase()))
                .map(this::normalizeResponse)
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, result);
    }

    @JavaService
    @SuppressWarnings("unused")
    public PagedResponseData<ErrorMapping> search(ErrorMappingSearchRequest request){
        assert request != null;
        List<ErrorMapping> errorMappingsList = errorMappingService.getErrorMappingsCache();
        List<ErrorMapping> result = errorMappingsList
                .stream()
                .filter(error -> Objects.isNull(request.getSearch()) ||
                             request.getSearch().isBlank() ||
                             error.getErrorMessage().toLowerCase().contains(request.getSearch().toLowerCase()) ||
                             String.valueOf(error.getScmErrorCode()).contains(request.getSearch().toLowerCase()))
                .map(this::normalizeResponse)
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, result);
    }

    @JavaService
    @SuppressWarnings("unused")
    public ErrorMapping findById(String id){
        ValidationUtils.checkNull(id,()-> new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(id,()-> new InvalidInputException("id"));
        return errorMappingService.findRefreshRecord(Long.parseLong(id));
    }

    @JavaService
    @SuppressWarnings("unused")
    public ErrorMapping edit(ErrorMappingEditRequest request){
        ValidationUtils.checkNull(request.getId(),()-> new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(request.getId(),()-> new InvalidInputException("id"));
        ValidationUtils.checkNull(request.getLastEditDate(),()-> new InvalidInputException("lastEditDate"));
        return errorMappingService.dynamicUpdate(request);
    }

    private ErrorMapping normalizeResponse(ErrorMapping errorMapping) {
        //As front-end need
        String exceptionClassName = errorMapping.getErrorMessage();
        String providerId = errorMapping.getProviderId();
        String exceptionOverrideName = errorMapping.getExceptionOverrideName();
        String providerErrorCode = errorMapping.getProviderErrorCode();
        errorMapping.setErrorMessage(Objects.nonNull(exceptionClassName) ? exceptionClassName : "");
        errorMapping.setProviderId(Objects.nonNull(providerId) ? providerId : "");
        errorMapping.setExceptionOverrideName(Objects.nonNull(exceptionOverrideName) ? exceptionOverrideName : "");
        errorMapping.setProviderErrorCode(Objects.nonNull(providerErrorCode) ? providerErrorCode : "");
        return errorMapping;
    }


}
