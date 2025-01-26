package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class HandlerMethodValidationExceptionResolver extends ExceptionResolver<HandlerMethodValidationException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;
    private final ErrorMappingService errorMappingService;

    @Override
    public List<Error> resolve(HandlerMethodValidationException exception, Locale locale) {
        ErrorMapping errorMapping = errorMappingService.findByExceptionClassName(InvalidInputException.class.getName()).orElseThrow(RuntimeException::new);
        return createBeanValidationViolationsErrorList(errorMapping, locale, exception);
    }

    private List<Error> createBeanValidationViolationsErrorList(ErrorMapping errorMapping, Locale locale, HandlerMethodValidationException exception) {
        List<Error> errors = new ArrayList<>();
        List<ParameterErrors> beanResults = exception.getBeanResults();
        for (ParameterErrors error : beanResults) {
            errors.add(new Error(
                    getBeanValidationErrorFieldName(error),
                    errorMapping.getScmErrorCode(),
                    getMessage(locale),
                    getMessage(AccessibleLocale.FA_IR.getLocale()),
                    errorMapping.getStatus(),
                    exception));
        }
        return errors;
    }

    private String getBeanValidationErrorFieldName(ParameterErrors error) {
        FieldError fieldError = error.getFieldError();
        if (Objects.nonNull(fieldError)){
           return fieldError.getField();
        }
        return null;
    }


    private String getMessage(Locale locale) {
        return messageBundleProvider.getExceptionMessageClassPath(locale, InvalidInputException.class.getName(), getExceptionInformation().getParameter());
    }

    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
