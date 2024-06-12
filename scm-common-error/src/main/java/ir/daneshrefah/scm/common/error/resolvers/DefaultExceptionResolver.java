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
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultExceptionResolver extends ExceptionResolver<Exception> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;
    private final int ERROR_CODE_SYSTEM_ERROR = 1001;

    @Override
    public Error resolve(Exception exception, Locale locale) {
        ErrorMapping errorMapping =
                errorMappingService.findByExceptionClassName(exception.getClass().getName())
                        .orElseGet(this::getDefaultErrorMapping);
        String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, exception);
        if (StringUtils.isEmpty(exceptionMessage)) {
            exceptionMessage = messageBundleProvider.getDefaultExceptionMessage(locale);
        }
        return new Error(
                getSource(exception),
                errorMapping.getScmErrorCode(), exceptionMessage,
                errorMapping.getStatus(),
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
                    em.setScmErrorCode(ERROR_CODE_SYSTEM_ERROR);
                    return em;
                });
    }


    @Override
    public ExceptionResolverLevel getResolverLevel() {
        return ExceptionResolverLevel.ALL_EXCEPTION;
    }
}
