package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.exception.DisableServiceExecutionException;
import ir.daneshrefah.scm.common.exception.ServiceNotFoundException;
import ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.exception.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.http.base.HttpOperationFailedException;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
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

    UNKNOWN_HOST_EXCEPTION(UnknownHostException.class, ErrorCodes.ERROR_CODE_UNKNOWN_HOST,
            MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER, "unknown host", false),
    NO_ROUTE_TO_HOST_EXCEPTION(NoRouteToHostException.class, ErrorCodes.ERROR_CODE_HOST_UNREACHABLE,
            MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER, "no route to host", false),
    HTTP_OPERATION_FAILED_EXCEPTION(HttpOperationFailedException.class, ErrorCodes.ERROR_CODE_INVALID_REMOTE_RESPONSE,
            MessageStatus.SC_ERROR_SYSTEM, "Http Operation Failed Exception", false),
    TRANSFORMER_EXCEPTION(TransformException.class, ErrorCodes.ERROR_CODE_TRANSFORMER_UNKNOWN_EXCEPTION,
            MessageStatus.SC_ERROR_SYSTEM, "", false),
    SERVICE_NOT_FOUND_EXCEPTION(ServiceNotFoundException.class, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_FOUND,
            MessageStatus.SC_NOT_FOUND, "", false),
    TERMINAL_SERVICE_NOT_FOUND_EXCEPTION(TerminalServiceNotFoundException.class, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_ASSIGNED_TO_TERMINAL,
            MessageStatus.SC_NOT_FOUND, "", false);

    private final Class<? extends Exception> exception;
    @Getter
    private final Integer errorCode;
    @Getter
    private final MessageStatus status;
    @Getter
    private final String defaultMessage;
    @Getter
    private final boolean foreMessage;

    public static Optional<ExceptionMapper> findByException(Class clazz) {
        return Arrays.stream(ExceptionMapper.values())
                .filter(m -> m.exception.equals(clazz))
                .findFirst();
    }

}
