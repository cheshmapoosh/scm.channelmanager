package ir.daneshrefah.scm.common.error.resolvers;


import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.ExceptionResolverLevel;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.ExceptionDynamicMessage;
import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DefaultBaseExceptionResolver extends ExceptionResolver<AbstractBaseException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public List<Error> resolve(AbstractBaseException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName()).orElseThrow(RuntimeException::new);
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(
                getSource(exception),
                errorMapping.getScmErrorCode(),
                getMessage(locale, exception),
                getMessage(AccessibleLocale.FA_IR.getLocale(), exception),
                errorMapping.getStatus(),
                exception));
        return errors;
    }

    @Override
    public ExceptionResolverLevel getResolverLevel() {
        return ExceptionResolverLevel.ALL_BASE_EXCEPTION;
    }

    private String getSource(Exception exception) {
        if (exception instanceof ExceptionSourceAware exceptionSourceAware) {
            return exceptionSourceAware.getSource();
        }
        return null;
    }

    private String getMessage(Locale locale, AbstractBaseException exception) {
        ExceptionInformation exceptionInformation = exception.getExceptionInformation();
        if (exceptionInformation.isDynamicMessage()) {
            ExceptionDynamicMessage exceptionDynamicMessage = exceptionInformation.getExceptionDynamicMessage();
            Map<String, String> parameters = exceptionDynamicMessage.getParameters();
            String bundleKey = exceptionDynamicMessage.getBundleKey();
            return messageBundleProvider.getDynamicExceptionMessage(locale, bundleKey, parameters);
        } else {
            return messageBundleProvider.getExceptionMessage(locale, exception, exception.getExceptionInformation().getParameter());
        }
    }
}
