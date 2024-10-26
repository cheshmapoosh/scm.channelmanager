package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.AbstractServiceProviderException;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AbstractServiceProviderExceptionResolver extends ExceptionResolver<AbstractServiceProviderException> {

    private final ErrorMappingService errorMappingService;
    private final ExceptionMessageBundleProvider messageBundleProvider;

    @Override
    public Error resolve(AbstractServiceProviderException exception, Locale locale) {
        String providerErrorCode = exception.getProviderErrorCode();
        String providerErrorMessage = exception.getProviderErrorMessage();
        if (Objects.isNull(providerErrorCode) || Objects.isNull(providerErrorMessage)) {
            return defaultExceptionMessage(exception, locale);
        }
        return customProviderException(exception, locale);
    }


    private Error customProviderException(AbstractServiceProviderException exception, Locale locale) {
        String exceptionMessage = getProviderExceptionMessage(exception, locale);
        String exceptionMessageFa = getProviderExceptionMessage(exception, AccessibleLocale.FA_IR.getLocale());
        return new Error(
                "provider",
                exception.getProviderErrorCode(),
                exceptionMessage,
                exceptionMessageFa,
                exception.getExceptionInformation().getMessageStatus(),
                exception);
    }

    private String getProviderExceptionMessage(AbstractServiceProviderException exception, Locale locale) {
        String key = exception.getProviderErrorMessage();
        Map<String, String> params = getProviderExceptionParameters(exception);
        String bundleValue = messageBundleProvider.getExceptionMessage(locale, key);
        Set<String> paramKeys = params.keySet();
        for (String paramKey : paramKeys) {
            bundleValue = bundleValue.replace(":" + paramKey, params.get(paramKey));
        }
        return bundleValue;
    }

    private Map<String, String> getProviderExceptionParameters(AbstractServiceProviderException exception) {
        Map<String, String> params = new HashMap<>();
        params.put("serviceCode", exception.getServiceCode());
        params.put("providerCode", exception.getServiceProviderCode());
        return params;
    }

    private Error defaultExceptionMessage(AbstractServiceProviderException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName()).orElseThrow();
        String key = BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX + exception.getClass().getName();
        String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, key);
        String exceptionMessageFa = messageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR.getLocale(), key);
        return new Error(
                "provider",
                errorMapping.getScmErrorCode(),
                exceptionMessage,
                exceptionMessageFa,
                errorMapping.getStatus(),
                exception);
    }


}
