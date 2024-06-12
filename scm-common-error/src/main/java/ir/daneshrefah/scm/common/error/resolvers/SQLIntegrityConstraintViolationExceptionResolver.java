package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class SQLIntegrityConstraintViolationExceptionResolver extends ExceptionResolver<SQLIntegrityConstraintViolationException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public Error resolve(SQLIntegrityConstraintViolationException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName()).orElseThrow(RuntimeException::new);
        return new Error(
                "constraint",
                errorMapping.getScmErrorCode(),
                getMessage(locale, exception),
                errorMapping.getStatus(),
                exception);
    }


    private String getMessage(Locale locale, SQLIntegrityConstraintViolationException exception) {
        return messageBundleProvider.getExceptionMessage(locale, exception, new HashMap<>());
    }


}
