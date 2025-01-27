package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ObjectOptimisticLockingFailureExceptionResolver  extends ExceptionResolver<ObjectOptimisticLockingFailureException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public List<Error> resolve(ObjectOptimisticLockingFailureException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName()).orElseThrow(RuntimeException::new);
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(
                "record-version",
                errorMapping.getScmErrorCode(),
                getMessage(locale, exception),
                getMessage(AccessibleLocale.FA_IR.getLocale(), exception),
                errorMapping.getStatus(),
                exception));
        return errors;
    }

    private String getMessage(Locale locale, ObjectOptimisticLockingFailureException exception) {
        return messageBundleProvider.getExceptionMessage(locale, exception, new HashMap<>());
    }
}
