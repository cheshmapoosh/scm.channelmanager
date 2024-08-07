package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RestExternalServiceProviderExceptionResolver extends ExceptionResolver<RestExternalServiceProviderException> {

    private final ErrorMappingService errorMappingService;
    private final ExceptionMessageBundleProvider messageBundleProvider;

    @Override
    public Error resolve(RestExternalServiceProviderException exception, Locale locale) {
        if (Objects.isNull(exception.getResponseCondition())) {
            return defaultExceptionMessage(exception, locale);
        }
        return customProviderException(exception, locale);
    }


    private Error customProviderException(RestExternalServiceProviderException exception, Locale locale) {
        ResponseCondition responseCondition = exception.getResponseCondition();
        String exceptionMessage = getProviderExceptionMessage(exception, locale);
        String exceptionMessageFa = getProviderExceptionMessage(exception, AccessibleLocale.FA_IR.getLocale());
        return new Error(
                "provider",
                responseCondition.getResponseExceptionErrorCodeProperty(),
                exceptionMessage,
                exceptionMessageFa,
                exception.getExceptionInformation().getMessageStatus(),
                exception);
    }

    private String getProviderExceptionMessage(RestExternalServiceProviderException exception, Locale locale) {
        String key = exception.getResponseCondition().getResponseExceptionErrorMessageProperty();
        Map<String, String> params = getProviderExceptionParameters(exception);
        String bundleValue = messageBundleProvider.getExceptionMessage(locale, key);
        Set<String> paramKeys = params.keySet();
        for (String paramKey : paramKeys) {
            bundleValue = bundleValue.replace(":" + paramKey, params.get(paramKey));
        }
        return bundleValue;
    }

    private Map<String, String> getProviderExceptionParameters(RestExternalServiceProviderException exception) {
        Map<String, String> params = new HashMap<>();
        params.put("serviceCode", exception.getServiceCode());
        params.put("providerCode", exception.getProvider());
        return params;
    }

    private Error defaultExceptionMessage(RestExternalServiceProviderException exception, Locale locale) {
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
