package ir.daneshrefah.scm.provider.scm.exception;

import ir.daneshrefah.scm.common.error.management.ExceptionResolver;
import ir.daneshrefah.scm.common.model.error.Error;

import java.util.List;
import java.util.Locale;

/**
 * Resolves stable SCM Resource boundary errors without database mappings.
 */
public final class ScmResourceProviderExceptionResolver
        extends ExceptionResolver<ScmResourceProviderException> {

    private static final String ERROR_SOURCE = "scm";

    @Override
    public List<Error> resolve(ScmResourceProviderException exception, Locale locale) {
        return List.of(new Error(
                ERROR_SOURCE,
                exception.getErrorCode(),
                exception.getMessage(),
                exception.getMessage(),
                exception.getStatus(),
                exception
        ));
    }
}
