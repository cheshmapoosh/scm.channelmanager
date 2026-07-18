package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.error.ErrorMappingService;
import ir.daneshrefah.scm.common.error.ErrorMapping;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.CardException;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CardExceptionReslover extends ExceptionResolver<CardException> {
    private final ExceptionMessageBundleProvider exceptionMessageBundleProvider;
    private final ErrorMappingService errorMappingService;

    private String getMessage(Locale locale, CardException exception, String errorCode) {
        return exceptionMessageBundleProvider.getExceptionMessage(locale, exception, new HashMap<>(), errorCode);
    }

    @Override
    public List<Error> resolve(CardException exception, Locale locale) {
        if (Objects.nonNull(exception.getIsoErrorCode())) {
            return resolve(exception, locale, exception.getIsoErrorCode());
        }

        ErrorMapping errorMapping = errorMappingService
                .findByExceptionClassNameAndErrorCode(exception.getClass().getName(), exception.getCode())
                .orElseThrow(() -> new IllegalStateException(
                        "No error mapping found for card error code: " + exception.getCode()));

        return List.of(new Error(
                null,
                exception.getCode(),
                getMessage(locale, exception, exception.getCode()),
                getMessage(AccessibleLocale.FA_IR.getLocale(), exception, exception.getCode()),
                errorMapping.getStatus(),
                exception));
    }

    @Override
    public List<Error> resolve(CardException exception, Locale locale, String isoErrorCode) {
        ErrorMapping errorMapping = errorMappingService.findByRemoteErrorCode(isoErrorCode)
                .orElseThrow(() -> new IllegalStateException(
                        "No error mapping found for ISO error code: " + isoErrorCode));

        return List.of(new Error(
                null,
                errorMapping.getScmErrorCode(),
                getMessage(locale, exception, isoErrorCode),
                getMessage(AccessibleLocale.FA_IR.getLocale(), exception, isoErrorCode),
                errorMapping.getStatus(),
                exception));
    }
}
