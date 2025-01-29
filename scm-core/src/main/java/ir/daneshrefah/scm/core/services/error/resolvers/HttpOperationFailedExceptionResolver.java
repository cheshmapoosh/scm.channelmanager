package ir.daneshrefah.scm.core.services.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.core.services.error.handler.ProviderErrorHandler;
import ir.daneshrefah.scm.core.services.error.handler.constants.ProviderExceptionResolver;
import lombok.RequiredArgsConstructor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@ProviderExceptionResolver
public class HttpOperationFailedExceptionResolver extends ExceptionResolver<HttpOperationFailedException> {

    private final ProviderErrorHandler providerErrorHandler;

    @Override
    public List<Error> resolve(HttpOperationFailedException exception, Locale locale) {
        return providerErrorHandler.handle(exception,locale);
    }


}
