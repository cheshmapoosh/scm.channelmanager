package ir.daneshrefah.scm.core.services.error.handler;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.ProviderError;
import ir.daneshrefah.scm.common.error.ProviderErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.exception.BaseServiceProviderException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class ProviderErrorHandler {

    private final ErrorMappingService errorMappingService;
    private final ResourceBundleService resourceBundleService;
    private final ExceptionMessageBundleProvider exceptionMessageBundleProvider;
    private final ServiceService serviceService;


    public List<Error> handle(Exception exception, Locale locale) {
        List<Error> errors = mapProviderError(exception, locale);
        if (errors.isEmpty()) {
            errors.add(defaultExceptionMessage(locale));
        }
        return errors;
    }

    private List<Error> mapProviderError(Exception exception, Locale locale) {
        List<Error> errors = new ArrayList<>();
        if (exception instanceof HttpOperationFailedException httpOperationFailedException) {
            handleHttpOperationFailedException(errors, httpOperationFailedException, locale);
        } else if (exception instanceof BaseServiceProviderException baseServiceProviderException) {
            handleBaseServiceProviderException(errors, baseServiceProviderException, locale);
        }
        return errors;
    }

    private void handleHttpOperationFailedException(List<Error> errors, HttpOperationFailedException exception, Locale locale) {
        String exceptionMessage;
        String exceptionMessageFa;
        ErrorMapping errorMapping = errorMappingService.findByErrorMessage(exception.getClass().getName()).orElseThrow();
        if (errorMapping.isBundleKey()) {
            exceptionMessage = resourceBundleService.get(locale, errorMapping.getErrorMessage()).orElseThrow();
            exceptionMessageFa = null;
        } else {
            exceptionMessage = exceptionMessageBundleProvider.getExceptionMessage(locale, exception);
            exceptionMessageFa = exceptionMessageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR, exception);
        }
        Error error = new Error(
                exception.getClass().getSimpleName(),
                errorMapping.getScmErrorCode(),
                exceptionMessage,
                exceptionMessageFa,
                errorMapping.getStatus(),
                null);
        errors.add(error);
    }

    private void handleBaseServiceProviderException(List<Error> errors, BaseServiceProviderException exception, Locale locale) {
        List<ProviderError> providerErrors = createProviderErrorList(exception);
        if (Objects.isNull(providerErrors) || providerErrors.isEmpty()) {
            errors.add(defaultExceptionMessage(locale));
        }else {
            AbstractExternalServiceProvider provider = serviceService.findServiceProviderByCode(exception.getProviderErrorMapping().getServiceProviderCode());
            providerErrors.forEach(providerError -> errorMappingService
                    .findByRemoteErrorCodeAndProviderId(providerError.getProviderErrorCode(), provider.getId())
                    .ifPresent(errorMapping -> errors.add(createError(exception, errorMapping, locale))));
        }
    }

    private List<ProviderError> createProviderErrorList(BaseServiceProviderException exception) {
        ProviderErrorMapping errorMapping = exception.getProviderErrorMapping();
        List<String> messages = errorMapping.getProviderErrorMessage();
        List<String> codes = errorMapping.getProviderErrorCode();
        List<ProviderError> providerErrors = null;
        if (Objects.isNull(messages) || Objects.isNull(codes)){
            return providerErrors;
        }
        else if (messages.size() == codes.size() && (!messages.isEmpty())) {
            providerErrors = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                String message = messages.get(i);
                String code = codes.get(i);
                ProviderError providerError = new ProviderError(code, message);
                providerErrors.add(providerError);
            }
        } else if (messages.isEmpty() && !codes.isEmpty()) {
            providerErrors = new ArrayList<>();
            codes.stream().map(code -> new ProviderError(code, null)).forEach(providerErrors::add);
        } else if (!messages.isEmpty() && codes.isEmpty()) {
            providerErrors = new ArrayList<>();
            messages.stream().map(msg -> new ProviderError(null, msg)).forEach(providerErrors::add);
        }
        return providerErrors;
    }

    private Error createError(BaseServiceProviderException exception, ErrorMapping errorMapping, Locale locale) {
        Error error = defaultExceptionMessage(locale);
        String persianMessage = StringUtils.EMPTY;
        String localeBasedMessage = StringUtils.EMPTY;
        if (errorMapping.isBundleKey()) {
            persianMessage = exceptionMessageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR, exception);
            localeBasedMessage = exceptionMessageBundleProvider.getExceptionMessage(locale, exception);
        }
        if (persianMessage.isBlank()) {
            persianMessage = resourceBundleService.get(AccessibleLocale.FA_IR.getLocale(), errorMapping.getErrorMessage()).orElse(error.getMessageFa());
        }
        if (localeBasedMessage.isBlank()) {
            persianMessage = resourceBundleService.get(locale, errorMapping.getErrorMessage()).orElse(error.getMessage());
        }
        return new Error(
                exception.getProviderErrorMapping().getServiceProviderCode(),
                errorMapping.getScmErrorCode(),
                localeBasedMessage,
                persianMessage,
                errorMapping.getStatus(),
                null);
    }


    private Error defaultExceptionMessage(Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(BaseServiceProviderException.class.getName()).orElseThrow();
        String key = BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX + BaseServiceProviderException.class.getName();
        String exceptionMessage = exceptionMessageBundleProvider.getExceptionMessage(locale, key);
        String exceptionMessageFa = exceptionMessageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR.getLocale(), key);
        return new Error(
                "provider",
                errorMapping.getScmErrorCode(),
                exceptionMessage,
                exceptionMessageFa,
                errorMapping.getStatus(),
                null);
    }


}
