package ir.daneshrefah.scm.common.error.resolvers;


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
import ir.daneshrefah.scm.common.model.message.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultBaseExceptionResolver extends ExceptionResolver<AbstractBaseException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public void resolve(Message message, AbstractBaseException exception, Locale locale) {
        Optional<ErrorMapping> errorMappingOptional = errorMappingService.findByExceptionClassName(exception.getClass().getName());
        Error error = createErrorResponse(exception, locale, errorMappingOptional);
        message.addError(error, exception.getExceptionInformation().getMessageStatus());
    }

    private Error createErrorResponse(AbstractBaseException exception, Locale locale, Optional<ErrorMapping> errorMappingOptional) {
        return new Error(
                getSource(exception),
                errorMappingOptional.map(ErrorMapping::getScmErrorCode).orElse(""),
                getMessage(locale, exception),
                exception);
    }

    @Override
    public ResponseEntity<?> resolve(AbstractBaseException exception, Locale locale) {
        Optional<ErrorMapping> errorMappingOptional = errorMappingService.findByExceptionClassName(exception.getClass().getName());
        Error errorResponse = createErrorResponse(exception, locale, errorMappingOptional);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @Override
    public ExceptionResolverLevel getPriority() {
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
