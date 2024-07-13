package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.repository.ServiceProviderRepository;
import ir.daneshrefah.scm.plugin.api.exception.ProviderErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProviderErrorResponseExceptionResolver extends ExceptionResolver<ProviderErrorResponseException> {

    private final ErrorMappingService errorMappingService;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ExceptionMessageBundleProvider messageBundleProvider;

    @Override
    public Error resolve(ProviderErrorResponseException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByRemoteErrorCodeAndProviderId(exception.getRemoteErrorCode(), findProvider(exception.getProviderCode()).getId()).orElseThrow(RuntimeException::new);
        String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, exception);
        return new Error(
                "provider",
                errorMapping.getScmErrorCode(),
                exceptionMessage,
                errorMapping.getStatus(),
                exception);
    }


    private AbstractExternalServiceProviderEntity findProvider(String providerCode) {
        return serviceProviderRepository.findByCode(providerCode).orElse(null);
    }


}
