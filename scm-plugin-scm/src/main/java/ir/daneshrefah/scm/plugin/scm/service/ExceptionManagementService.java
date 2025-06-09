package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingCreateRequest;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingEditRequest;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingFindRequest;
import ir.daneshrefah.scm.common.dto.error.ErrorMappingSearchRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Service
public class ExceptionManagementService extends AbstractJavaService {

    private final ErrorMappingService errorMappingService;

    public ExceptionManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper,ErrorMappingService errorMappingService) {
        super(producerTemplate, objectMapper);
        this.errorMappingService = errorMappingService;
    }

    @JavaService(operationCode = SVC_ERROR_LIST)
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
                .filter(error -> null == request || null == request.getBundleKey() || request.getBundleKey().equals(error.isBundleKey()))
                .filter(error -> null == request || null == request.getErrorMessage() || error.getErrorMessage().toLowerCase().contains(request.getErrorMessage().toLowerCase()))
                .map(this::normalizeResponse)
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, result);
    }

    @JavaService(operationCode = SVC_ERROR_SEARCH)
    @SuppressWarnings("unused")
    public PagedResponseData<ErrorMapping> search(ErrorMappingSearchRequest request){
        ValidationUtils.checkNull(request,()->new MissingRequiredInputException("payload"));
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

    @JavaService(operationCode = SVC_ERROR_FIND_BY_ID)
    @SuppressWarnings("unused")
    public ErrorMapping findById(String id){
        ValidationUtils.checkNull(id,()-> new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(id,()-> new InvalidInputException("id"));
        return errorMappingService.findById(Long.parseLong(id));
    }

    @JavaService(operationCode = SVC_ERROR_CREATE)
    @SuppressWarnings("unused")
    public ErrorMapping create(ErrorMappingCreateRequest request){
        return errorMappingService.create(request);
    }

    @JavaService(operationCode = SVC_ERROR_EDIT)
    @SuppressWarnings("unused")
    public ErrorMapping edit(ErrorMappingEditRequest request){
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
