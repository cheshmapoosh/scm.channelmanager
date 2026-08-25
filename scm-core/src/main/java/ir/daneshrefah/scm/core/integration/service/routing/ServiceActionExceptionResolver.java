package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public final class ServiceActionExceptionResolver extends ExceptionResolver<ServiceActionException> {
    private static final String ERROR_SOURCE = "scm";

    @Override
    public List<Error> resolve(ServiceActionException exception, Locale locale) {
        return List.of(new Error(
                ERROR_SOURCE,
                exception.getErrorCode(),
                exception.getPublicMessage(),
                exception.getPublicMessage(),
                exception.getStatus(),
                exception
        ));
    }
}
