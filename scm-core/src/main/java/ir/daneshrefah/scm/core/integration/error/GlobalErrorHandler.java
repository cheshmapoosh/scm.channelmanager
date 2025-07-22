package ir.daneshrefah.scm.core.integration.error;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.error.management.ErrorHandlerChain;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GlobalErrorHandler {

    private final ErrorHandlerChain errorHandlerChain;

    public void handle(Exchange exchange) {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        AccessibleLocale accessibleLocale = getRequestLocale(exchange);
        errorHandlerChain
                .getOrdersErrorHandlers()
                .stream()
                .filter(errorHandler -> errorHandler.support(exception))
                .findFirst()
                .ifPresent(errorHandler -> {
                    ScmFault scmFault = errorHandler.handle(exception, accessibleLocale.getLocale());
                    exchange.getMessage().setBody(scmFault);
                });

    }

    private AccessibleLocale getRequestLocale(Exchange exchange) {
        return Optional
                .ofNullable(exchange.getMessage().getHeader("accept-language", String.class))
                .flatMap(AccessibleLocale::findByLocale).orElse(AccessibleLocale.DEFAULT_LOCALE);
    }

}
