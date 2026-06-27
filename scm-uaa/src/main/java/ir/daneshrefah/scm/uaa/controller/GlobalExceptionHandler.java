package ir.daneshrefah.scm.uaa.controller;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.error.management.ExceptionResolverHelper;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitExceededException;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.uaa.observation.UaaObservation;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
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

    private static final String X_CORRELATION_ID = "X-Correlation-Id";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";

    private final UaaObservation observation;

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> handleRateLimitExceeded(HttpServletRequest request, RateLimitExceededException exception) {
        RateLimitResult rateLimitResult = exception.getResult();
        Error error = new Error(
                "rateLimit",
                9000,
                "too many requests",
                "تعداد درخواست‌ها بیشتر از حد مجاز است",
                MessageStatus.SC_ACCESS_DENIED,
                exception
        );
        ResponseResult result = new ResponseResult()
                .setResult(null)
                .setStatus(MessageStatus.SC_ACCESS_DENIED)
                .setErrors(List.of(error));
        handleSpanException(request, exception, HttpStatus.TOO_MANY_REQUESTS);
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        rateLimitResult.toHeaders().forEach((name, value) -> responseBuilder.header(name, String.valueOf(value)));
        return responseBuilder.body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(HttpServletRequest request, Exception exception) {
        List<Error> resolves = ExceptionResolverHelper.getInstance().resolve(exception, detectRequesteLocale(request));

        ResponseResult result = new ResponseResult()
                .setResult(null)
                .setStatus(resolves.get(0).getStatus())
                .setErrors(resolves);
        if (resolves.get(0).getStatus().equals(MessageStatus.SC_ERROR_SYSTEM)) {
            handleSpanException(request, exception, HttpStatus.INTERNAL_SERVER_ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        handleSpanException(request, exception, HttpStatus.BAD_REQUEST);
        return ResponseEntity.badRequest().body(result);
    }

    private void handleSpanException(HttpServletRequest request, Exception exception, HttpStatus responseStatus) {
        try {
            UaaObservation.ControllerContext ctx = new UaaObservation.ControllerContext(
                    "globalException",
                    "handle",
                    request.getMethod(),
                    requestPath(request),
                    responseStatus.value(),
                    clientIp(request),
                    correlationId(request),
                    "failure",
                    observation.safeErrorMessage(exception)
            );
            ObservationScope scope = observation.traceController(ctx);
            try {
                observation.controllerAttributes(scope, ctx);
                scope.failure(exception);
                observation.controllerFailed(ctx, exception);
            } finally {
                scope.close();
            }
        } catch (Exception e) {
            log.error("Exception occurred while handling global exception observation", e);
        }
    }

    private String correlationId(HttpServletRequest request) {
        String correlationId = request.getHeader(X_CORRELATION_ID);
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = request.getHeader(Constants.SCM_PARAMETER_CORRELATION_ID);
        }
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = request.getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID);
        }
        return correlationId;
    }

    private String requestPath(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return StringUtils.isEmpty(request.getRequestURI()) ? request.getServletPath() : request.getRequestURI();
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(forwardedFor)) {
            int comma = forwardedFor.indexOf(',');
            return comma >= 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader(X_REAL_IP);
        if (!StringUtils.isEmpty(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
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
