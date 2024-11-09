package ir.daneshrefah.scm.core.service.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.exception.BaseServiceProviderException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.core.service.error.handler.ProviderErrorHandler;
import ir.daneshrefah.scm.core.service.error.handler.constants.ProviderExceptionResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@ProviderExceptionResolver
public class BaseServiceProviderExceptionResolver extends ExceptionResolver<BaseServiceProviderException> {

    private final ProviderErrorHandler providerErrorHandler;

    @Override
    public List<Error> resolve(BaseServiceProviderException exception, Locale locale) {
        return providerErrorHandler.handle(exception,locale);
    }


}
