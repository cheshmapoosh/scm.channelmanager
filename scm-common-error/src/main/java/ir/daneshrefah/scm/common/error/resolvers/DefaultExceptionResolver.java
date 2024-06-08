package ir.daneshrefah.scm.common.error.resolvers;


import ir.daneshrefah.scm.common.constant.ExceptionResolverLevel;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultExceptionResolver extends ExceptionResolver<Exception> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;
    private final int ERROR_CODE_SYSTEM_ERROR = 1001;

    @Override
    public void resolve(Message message, Exception exception, Locale locale) {
        ErrorMapping defaultErrorMapping = getDefaultErrorMapping();
        message.addError(createErrorResponse(exception,defaultErrorMapping,locale), defaultErrorMapping.getStatus());
    }

    @Override
    public ResponseEntity<?> resolve(Exception exception, Locale locale) {
        ErrorMapping defaultErrorMapping = getDefaultErrorMapping();
        return ResponseEntity
                .internalServerError()
                .body(createErrorResponse(exception,defaultErrorMapping,locale));
    }

    private Error createErrorResponse(Exception exception,ErrorMapping errorMapping, Locale locale) {
        return new Error(
                getSource(exception),
                errorMapping.getScmErrorCode(),
                messageBundleProvider.getDefaultExceptionMessage(locale),
                exception);
    }

    private String getSource(Exception exception) {
        if (exception instanceof ExceptionSourceAware exceptionSourceAware){
            return exceptionSourceAware.getSource();
        }
        return null;
    }

    private ErrorMapping getDefaultErrorMapping(){
        return errorMappingService.findByExceptionClassName("java.lang.Exception")
                .orElseGet(()-> {
                    ErrorMapping em = new ErrorMapping();
                    em.setStatus(MessageStatus.SC_ERROR_SYSTEM);
                    em.setScmErrorCode("SCM-"+ERROR_CODE_SYSTEM_ERROR);
                    return em;
                });
    }

    @Override
    public ExceptionResolverLevel getPriority() {
        return ExceptionResolverLevel.ALL_EXCEPTION;
    }
}
