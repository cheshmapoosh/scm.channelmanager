package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.BundleDefaults;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataIntegrityViolationExceptionResolver extends ExceptionResolver<DataIntegrityViolationException> {

    private final ErrorMappingService errorMappingService;
    private final ExceptionMessageBundleProvider messageBundleProvider;


    @Override
    public Error resolve(DataIntegrityViolationException exception, Locale locale) {
        return createErrorResponse(locale,exception);
    }


    private Error createErrorResponse(Locale locale,DataIntegrityViolationException exception){

        if (exception.getMessage().contains("SQLCODE=-803") || exception.getMessage().contains("Duplicate")) {
            Optional<ErrorMapping> foundErrorMapping = errorMappingService.findByExceptionClassNameAndOverrideName(exception.getClass().getName(), "SQLCODE=-803");
            if (foundErrorMapping.isPresent()) {
                ErrorMapping errorMapping = foundErrorMapping.get();
                String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, exception, "SQLCODE=-803", new HashMap<>());
                String exceptionMessageFa = messageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR.getLocale(), exception, "SQLCODE=-803", new HashMap<>());
                return new Error("Duplicate", errorMapping.getScmErrorCode(), exceptionMessage, exceptionMessageFa, errorMapping.getStatus(), exception);
            }
        }
         if (exception.getMessage().toLowerCase().contains("integrity")) {
            Optional<ErrorMapping> foundErrorMapping = errorMappingService.findByExceptionClassNameAndOverrideName(exception.getClass().getName(), "integrity");
            if (foundErrorMapping.isPresent()) {
                ErrorMapping errorMapping = foundErrorMapping.get();
                String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, exception, "integrity", new HashMap<>());
                String exceptionMessageFa = messageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR.getLocale(), exception, "integrity", new HashMap<>());
                return new Error("Integrity", errorMapping.getScmErrorCode(), exceptionMessage,exceptionMessageFa,errorMapping.getStatus(), exception);
            }
        }
            return createDefaultErrorResponse(locale,exception);

    }

    private Error createDefaultErrorResponse(Locale locale, DataIntegrityViolationException exception) {
        Optional<ErrorMapping> foundErrorMapping = errorMappingService.findByExceptionClassName(exception.getClass().getName());
        if (foundErrorMapping.isPresent()) {
            ErrorMapping errorMapping = foundErrorMapping.get();
            String key = BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX+exception.getClass().getName();
            String exceptionMessage = messageBundleProvider.getExceptionMessage(locale, key);
            String exceptionMessageFa = messageBundleProvider.getExceptionMessage(AccessibleLocale.FA_IR.getLocale(), key);
            return new Error("Integrity", errorMapping.getScmErrorCode(), exceptionMessage,exceptionMessageFa,errorMapping.getStatus(), exception);
        }
        return new Error("",0,"",exception);
    }

}
