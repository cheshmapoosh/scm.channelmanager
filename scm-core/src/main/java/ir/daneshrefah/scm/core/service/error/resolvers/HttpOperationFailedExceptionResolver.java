package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class HttpOperationFailedExceptionResolver extends ExceptionResolver<HttpOperationFailedException> {

    //TODO CONNECT PROVIDER CODE TO THIS EXCEPTION HTTP STATUS CODE
    private final ErrorMappingService errorMappingService;
    private final ExceptionMessageBundleProvider messageBundleProvider;

    @Override
    public Error resolve(HttpOperationFailedException exception, Locale locale) {
        return defaultExceptionMessage(locale);
    }

    private Error defaultExceptionMessage(Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(RestExternalServiceProviderException.class.getName()).orElseThrow();
        String key = BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX + RestExternalServiceProviderException.class.getName();
        String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, key);
        String exceptionMessageFa = messageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR.getLocale(), key);
        return new Error(
                "provider",
                errorMapping.getScmErrorCode(),
                exceptionMessage,
                exceptionMessageFa,
                errorMapping.getStatus(),
                null);
    }


}
