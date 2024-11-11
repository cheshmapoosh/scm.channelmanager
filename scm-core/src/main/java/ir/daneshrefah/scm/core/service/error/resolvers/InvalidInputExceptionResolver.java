package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.error.Error;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class InvalidInputExceptionResolver extends ExceptionResolver<InvalidInputException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public List<Error> resolve(InvalidInputException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName()).orElseThrow(RuntimeException::new);
        Set<ConstraintViolation<Object>> violations = exception.getViolations();
        if (Objects.nonNull(violations) && !violations.isEmpty()) {
            return createBeanValidationViolationsErrorList(errorMapping, locale, exception,violations);
        } else {
            return createDefaultError(errorMapping, locale, exception);
        }
    }

    private List<Error> createDefaultError(ErrorMapping errorMapping, Locale locale, InvalidInputException exception) {
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(
                exception.getSource(),
                errorMapping.getScmErrorCode(),
                getMessage(locale, exception),
                getMessage(AccessibleLocale.FA_IR.getLocale(), exception),
                errorMapping.getStatus(),
                exception));
        return errors;
    }

    private List<Error> createBeanValidationViolationsErrorList(ErrorMapping errorMapping, Locale locale, InvalidInputException exception, Set<ConstraintViolation<Object>> violations) {
        List<Error> errors = new ArrayList<>();
        for (ConstraintViolation<Object> error : violations) {
            errors.add(new Error(
                    error.getPropertyPath().toString(),
                    errorMapping.getScmErrorCode(),
                    getMessage(locale, exception),
                    getMessage(AccessibleLocale.FA_IR.getLocale(), exception),
                    errorMapping.getStatus(),
                    exception));
        }
        return errors;
    }

    private String getMessage(Locale locale, AbstractBaseException exception) {
        return messageBundleProvider.getExceptionMessage(locale, exception, exception.getExceptionInformation().getParameter());
    }
}
