package ir.daneshrefah.scm.uaa.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.error.management.ExceptionResolverHelper;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.logging.utils.SpanUtil;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Locale;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class
GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(HttpServletRequest request, Exception exception) {
        List<Error> resolves = ExceptionResolverHelper.getInstance().resolve(exception, detectRequesteLocale(request));

        ResponseResult result = new ResponseResult()
                .setResult(null)
                .setStatus(resolves.get(0).getStatus())
                .setErrors(resolves);
        handleSpanException(request, exception);
        if (resolves.get(0).getStatus().equals(MessageStatus.SC_ERROR_SYSTEM)) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    private void handleSpanException(HttpServletRequest request, Exception exception) {
        Span span = null;
        try {
            span = (Span) request.getAttribute("otel.span");
            if (span == null) {
                span = tracer.spanBuilder(request.getServletPath()).setSpanKind(SpanKind.SERVER).startSpan();
                SpanUtil.setRequestSpanAttributes(request, span);
                SpanUtil.setException(exception,span);
                span.setAttribute(LogAttribute.HTTP_STATUS_CODE.getAttributeName(), HttpStatus.BAD_REQUEST.value());
            }
        } catch (Exception e) {
            log.error("Exception occurred while handling global exception span", e);
        } finally {
            if (span != null) {
                span.end();
            }
        }
    }

    private Locale detectRequesteLocale(HttpServletRequest request) {
        String acceptLanguageHeader = request.getHeader("accept-language");
        if (StringUtils.isEmpty(acceptLanguageHeader)) {
            return AccessibleLocale.EN_US.getLocale();
        } else {
            return request.getLocale();
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    private static class ResponseResult {
        private List<Error> errors;
        private MessageStatus status;
        private Object result;
    }



    /*@ExceptionHandler(Exception.class)
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

        HttpMessageNotReadableException(HttpMessageNotReadableException.class, HttpConstants.HTTP_STATUS_BAD_REQUEST, ERROR_CODE_REQUEST_IS_NULL, null),
        SocketTimeoutException(java.net.SocketTimeoutException.class, HttpConstants.HTTP_STATUS_GATEWAY_TIMEOUT, ERROR_CODE_SOCKET_TIMEOUT, null),
        HttpRequestMethodNotSupportedException(org.springframework.web.HttpRequestMethodNotSupportedException.class, HttpConstants.HTTP_STATUS_METHOD_NOT_ALLOWED, ERROR_CODE_HTTP_METHOD_NOT_ALLOWED, null),
        UnknownHostException(java.net.UnknownHostException.class, HttpConstants.HTTP_STATUS_BAD_GATEWAY, ERROR_CODE_UNKNOWN_HOST, null);

        private final Class exception;
        private final int statusCode;
        private final int errorCode;
        private final String message;

        public static Optional<ExceptionMap> findByException(Class exception) {
            return Arrays.stream(ExceptionMap.values())
                    .filter(s -> s.exception.isAssignableFrom(exception))
                    .findFirst();
        }
    }*/


}
