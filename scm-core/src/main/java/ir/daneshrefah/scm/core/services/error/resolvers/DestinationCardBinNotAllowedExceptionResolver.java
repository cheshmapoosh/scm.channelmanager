package ir.daneshrefah.scm.core.services.error.resolvers;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.error.management.ExceptionMessageBundleProvider;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.plugin.specific.DestinationCardBinNotAllowedException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DestinationCardBinNotAllowedExceptionResolver
        extends ExceptionResolver<DestinationCardBinNotAllowedException> {

    private final ExceptionMessageBundleProvider messageBundleProvider;

    @Override
    public List<Error> resolve(DestinationCardBinNotAllowedException exception, Locale locale) {
        String localizedMessage = resolveMessage(locale, exception);
        String persianMessage = resolveMessage(AccessibleLocale.FA_IR.getLocale(), exception);
        return List.of(new Error(
                "destinationCardNumber",
                exception.getErrorCode(),
                localizedMessage,
                persianMessage,
                MessageStatus.SC_ERROR_BUSINESS,
                exception));
    }

    private String resolveMessage(Locale locale, DestinationCardBinNotAllowedException exception) {
        String message = messageBundleProvider.getExceptionMessage(locale, exception);
        return message == null || message.isBlank()
                ? messageBundleProvider.getDefaultExceptionMessage(locale)
                : message;
    }
}
