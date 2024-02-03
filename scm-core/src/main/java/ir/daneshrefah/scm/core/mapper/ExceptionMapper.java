package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.exception.DisableServiceExecutionException;
import ir.daneshrefah.scm.common.exception.ServiceNotFoundException;
import ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.plugin.api.exception.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-18
 */
@RequiredArgsConstructor
public enum ExceptionMapper {

    JAVA_SERVICE_METHOD_NOT_FOUND_EXCEPTION(JavaServiceMethodNotFoundException.class, ErrorCodes.ERROR_CODE_JAVA_SERVICE_METHOD_NOT_FOUND,
            Status.SC_ERROR_SYSTEM),
    TRANSFORMER_EXCEPTION(TransformException.class, ErrorCodes.ERROR_CODE_TRANSFORMER_UNKNOWN_EXCEPTION,
            Status.SC_ERROR_SYSTEM),
    INVALID_PROVIDER_RESPONSE_EXCEPTION(InvalidProviderResponseException.class, ErrorCodes.ERROR_CODE_INVALID_PROVIDER_RESPONSE,
            Status.SC_ERROR_UNREACHABLE_PROVIDER),
    PROVIDER_UNSUCCESSFUL_RESPONSE_EXCEPTION(ProviderUnSuccessfulResponseException.class, ErrorCodes.ERROR_CODE_UNSUCCESSFUL_PROVIDER_RESPONSE,
            Status.SC_ERROR_UNREACHABLE_PROVIDER),
    PROVIDER_UNKNOWN_EXCEPTION(ProviderUnknownException.class, ErrorCodes.ERROR_CODE_PROVIDER_UNKNOWN_EXCEPTION,
            Status.SC_ERROR_UNREACHABLE_PROVIDER),
    PROVIDER_UNREACHABLE_EXCEPTION(ProviderUnreachableException.class, ErrorCodes.ERROR_CODE_HOST_UNREACHABLE,
            Status.SC_ERROR_UNREACHABLE_PROVIDER),
    SERVICE_NOT_FOUND_EXCEPTION(ServiceNotFoundException.class, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_FOUND,
            Status.SC_NOT_FOUND),
    TERMINAL_SERVICE_NOT_FOUND_EXCEPTION(TerminalServiceNotFoundException.class, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_ASSIGNED_TO_TERMINAL,
            Status.SC_NOT_FOUND),
    DISABLE_SERVICE_EXCEPTION(DisableServiceExecutionException.class, ErrorCodes.ERROR_CODE_JAVA_SERVICE_IS_DISABLED,
            Status.SC_NOT_FOUND);

    private final Class<? extends Exception> exception;
    @Getter
    private final Integer errorCode;
    @Getter
    private final Status status;

    public static Optional<ExceptionMapper> findByException(Class clazz) {
        return Arrays.stream(ExceptionMapper.values())
                .filter(m -> m.exception.equals(clazz))
                .findFirst();
    }

}
