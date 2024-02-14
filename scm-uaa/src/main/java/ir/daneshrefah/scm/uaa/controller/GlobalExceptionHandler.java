package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.common.exception.AbstractValidationException;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AbstractValidationException.class)
    public ResponseEntity<Object> handleAbstractValidationException(AbstractValidationException ex) {
        DefaultErrorResponse errorResponse = new DefaultErrorResponse(ex.getSource(), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Object> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        DefaultErrorResponse errorResponse = new DefaultErrorResponse("constraint", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

//    @ExceptionHandler(BaseCIFException.class)
//    public ResponseEntity<Object> handleBaseCIFException(BaseCIFException ex) {
//        return ResponseEntity.badRequest().body(new DefaultErrorResponse("CIF", 0, ex.getMessage()));
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        Optional<ExceptionMap> exceptionMap = ExceptionMap.findByException(ex.getClass());
        if (exceptionMap.isEmpty() && null != ex.getCause()) {
            exceptionMap = ExceptionMap.findByException(ex.getCause().getClass());
        }
        if (exceptionMap.isPresent()) {
            return ResponseEntity.status(exceptionMap.get().getStatusCode()).body(new DefaultErrorResponse(ex.getClass().getSimpleName(),
                    exceptionMap.get().getErrorCode(), null != exceptionMap.get().getMessage() ? exceptionMap.get().getMessage() : ex.getMessage()));
        }
        return ResponseEntity.internalServerError().body(new DefaultErrorResponse(ex.getClass().getSimpleName(), ERROR_CODE_SYSTEM_ERROR,
                ex.getMessage()));
    }

    @Getter
    @RequiredArgsConstructor
    private enum ExceptionMap {

        HttpMessageNotReadableException(HttpMessageNotReadableException.class, HttpConstants.HTTP_STATUS_BAD_REQUEST,
                ERROR_CODE_REQUEST_IS_NULL, null),
        SocketTimeoutException(java.net.SocketTimeoutException.class, HttpConstants.HTTP_STATUS_GATEWAY_TIMEOUT,
                ERROR_CODE_SOCKET_TIMEOUT, null),
        UnknownHostException(java.net.UnknownHostException.class, HttpConstants.HTTP_STATUS_BAD_GATEWAY,
                ERROR_CODE_UNKNOWN_HOST, null);

        private final Class exception;
        private final int statusCode;
        private final int errorCode;
        private final String message;

        public static Optional<ExceptionMap> findByException(Class exception) {
            return Arrays.stream(ExceptionMap.values())
                    .filter(s -> s.exception.isAssignableFrom(exception))
                    .findFirst();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public class DefaultErrorResponse {

        private final String source;
        private final int errorCode;
        private final String message;

    }
}
