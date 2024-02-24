package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.exception.DisableServiceExecutionException;
import ir.daneshrefah.scm.common.exception.ServiceNotFoundException;
import ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
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

    TRANSFORMER_EXCEPTION(TransformException.class, ErrorCodes.ERROR_CODE_TRANSFORMER_UNKNOWN_EXCEPTION,
            MessageStatus.SC_ERROR_SYSTEM),
    SERVICE_NOT_FOUND_EXCEPTION(ServiceNotFoundException.class, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_FOUND,
            MessageStatus.SC_NOT_FOUND),
    TERMINAL_SERVICE_NOT_FOUND_EXCEPTION(TerminalServiceNotFoundException.class, ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_NOT_ASSIGNED_TO_TERMINAL,
            MessageStatus.SC_NOT_FOUND);

    private final Class<? extends Exception> exception;
    @Getter
    private final Integer errorCode;
    @Getter
    private final MessageStatus status;

    public static Optional<ExceptionMapper> findByException(Class clazz) {
        return Arrays.stream(ExceptionMapper.values())
                .filter(m -> m.exception.equals(clazz))
                .findFirst();
    }

}
