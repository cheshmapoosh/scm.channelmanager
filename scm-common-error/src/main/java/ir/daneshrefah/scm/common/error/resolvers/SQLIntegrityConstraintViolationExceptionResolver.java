package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.ExceptionDynamicMessage;
import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SQLIntegrityConstraintViolationExceptionResolver extends ExceptionResolver<SQLIntegrityConstraintViolationException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public void resolve(Message message, SQLIntegrityConstraintViolationException exception, Locale locale) {
        Optional<ErrorMapping> errorMappingOptional = errorMappingService.findByExceptionClassName(exception.getClass().getName());
        message.addError(createErrorResponse(exception,locale,errorMappingOptional),errorMappingOptional.map(ErrorMapping::getStatus).orElse(MessageStatus.SC_ERROR_SYSTEM));
    }

    @Override
    public ResponseEntity<?> resolve(SQLIntegrityConstraintViolationException exception, Locale locale) {
        Optional<ErrorMapping> errorMappingOptional = errorMappingService.findByExceptionClassName(exception.getClass().getName());
        return ResponseEntity.badRequest().body(createErrorResponse(exception,locale,errorMappingOptional));
    }

    private Error createErrorResponse(SQLIntegrityConstraintViolationException exception,Locale locale,Optional<ErrorMapping> errorMapping){
        return new Error(
                "constraint",
                errorMapping.map(ErrorMapping::getScmErrorCode).orElse(""),
                getMessage(locale, exception),
                exception);
    }

    private String getMessage(Locale locale, SQLIntegrityConstraintViolationException exception) {
        return messageBundleProvider.getExceptionMessage(locale, exception,new HashMap<>());
    }
}
