package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.NabError;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NabExptionResolver extends ExceptionResolver<NabError> {
    private final ExceptionMessageBundleProvider exceptionMessageBundleProvider;
    private final ErrorMappingService errorMappingService;

    private String getMessage(Locale locale, NabError exception) {
        return exceptionMessageBundleProvider.getExceptionMessage(locale, exception, new HashMap<>());
    }

    @Override
    public List<Error> resolve(NabError exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName()).orElseThrow(RuntimeException::new);

        List<Error> errors = new ArrayList<>();
        errors.add(new Error(
                null,
                exception.getCode(),
                getMessage(locale, exception),
                getMessage(AccessibleLocale.FA_IR.getLocale(), exception),
                errorMapping.getStatus(),
                exception));
        return errors;
    }
}
