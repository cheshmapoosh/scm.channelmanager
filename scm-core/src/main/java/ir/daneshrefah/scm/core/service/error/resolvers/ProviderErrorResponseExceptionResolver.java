package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.entity.service.ExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.repository.ServiceProviderRepository;
import ir.daneshrefah.scm.plugin.api.exception.ProviderErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProviderErrorResponseExceptionResolver extends ExceptionResolver<ProviderErrorResponseException> {

    private final ErrorMappingService errorMappingService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ExceptionMessageBundleProvider messageBundleProvider;

    @Override
    public void resolve(Message message, ProviderErrorResponseException exception, Locale locale) {
        Optional<ErrorMapping> errorMappingOptional = errorMappingService.findByRemoteErrorCodeAndProviderId(exception.getRemoteErrorCode(),findProvider(exception.getProviderCode()).getId());
        message.addError(createErrorResponse(exception,locale,errorMappingOptional), MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }


    private Error createErrorResponse(ProviderErrorResponseException exception, Locale locale,Optional<ErrorMapping> errorMapping) {
        String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, exception);
        return new Error(
                "provider",
                errorMapping.map(ErrorMapping::getScmErrorCode).orElse(""),
                exceptionMessage,
                exception);
    }

    private ExternalServiceProviderEntity findProvider(String providerCode) {
        return serviceProviderRepository.findByCode(providerCode).orElse(null);
    }

    @Override
    public ResponseEntity<?> resolve(ProviderErrorResponseException exception, Locale locale) {
        Optional<ErrorMapping> errorMappingOptional = errorMappingService.findByRemoteErrorCodeAndProviderId(exception.getRemoteErrorCode(),findProvider(exception.getProviderCode()).getId());
        return ResponseEntity.badRequest().body(createErrorResponse(exception,locale,errorMappingOptional));
    }


}
