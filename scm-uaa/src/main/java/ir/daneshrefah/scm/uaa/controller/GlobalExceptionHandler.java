package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.common.exception.ValidationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        DefaultErrorResponse errorResponse = new DefaultErrorResponse(ex.getSource(), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Object> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        DefaultErrorResponse errorResponse = new DefaultErrorResponse("constraint", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @Getter
    @RequiredArgsConstructor
    public class DefaultErrorResponse {

        private final String source;
        private final int errorCode;
        private final String message;

    }
}
