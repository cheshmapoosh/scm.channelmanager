package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class SpanUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static void setRequestSpanAttributes(HttpServletRequest request, Span span) {
        span.setAttribute(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName(), request.getRemoteAddr());
        span.setAttribute(LogAttribute.METHOD_TYPE.getAttributeName(), request.getMethod());
        span.setAttribute(LogAttribute.REQUEST.getAttributeName(), getRequestBody(request).trim());
        span.setAttribute(LogAttribute.HOST_ADDRESS.getAttributeName(), request.getLocalAddr());
        span.setAttribute(LogAttribute.END_POINT.getAttributeName(), "rest::%s".formatted(request.getServletPath()));
        span.setAttribute(LogAttribute.CLIENT_FLOW_ID.getAttributeName(), request.getHeader(Constants.SCM_PARAMETER_CLIENT_FLOW_ID));
        span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), UUID.randomUUID().toString());
        span.setAttribute(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), request.getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID));
        span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), UUID.randomUUID().toString());
        setSpanAttributes(span);
    }

    public static void setResponseSpanAttributes(Object returnValue, Span span) {
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        int statusCode = response != null ? response.getStatus() : 0;
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);
        span.setAttribute(LogAttribute.HTTP_STATUS_CODE.getAttributeName(), statusCode);
        span.setAttribute(LogAttribute.MESSAGE_STATUS.getAttributeName(), httpStatus != null ? httpStatus.getReasonPhrase() : "");
        span.setAttribute(LogAttribute.RESPONSE.getAttributeName(), getResponseBody(returnValue));
    }

    public static void setSpanAttributes(Span span) {
        span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), AuthenticationUtils.getLoggedInUser() != null ? AuthenticationUtils.getLoggedInUser().getTerminalCode() : "");
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.THREAD_NAME.getAttributeName(), Thread.currentThread().getName());
    }

    public static void setException(Throwable ex, Span span) {
        span.setAttribute(LogAttribute.MESSAGE_STATUS.getAttributeName(), MessageStatus.SC_ERROR_SYSTEM.toString());
        span.setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
        span.setAttribute(LogAttribute.HTTP_STATUS_CODE.getAttributeName(), HttpStatus.BAD_REQUEST.value());
        StackTraceElement[] stackTraceElements = Arrays.stream(ex.getStackTrace()).limit(3).toArray(StackTraceElement[]::new);
        ex.setStackTrace(stackTraceElements);
        String stackTraceString = ExceptionUtils.getStackTrace(ex);
        span.setAttribute(LogAttribute.ERROR_DETAILS.getAttributeName(), stackTraceString);
        span.setStatus(StatusCode.ERROR);
        String spanId = span.getSpanContext().getSpanId();
        String traceId = span.getSpanContext().getTraceId();
        String delimiter = "==========".repeat(10);
        log.error(
                "\n{}\nSpanId: {}\nTraceId: {}\nException Message: {}",
                delimiter, spanId, traceId, ex.getMessage(), ex
        );
//        ex.setStackTrace(stackTraceElements);
    }

    public static String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] content = wrapper.getContentAsByteArray();
                if (content.length > 0) {
                    String rawBody = new String(content, StandardCharsets.UTF_8);
                    return objectMapper.writeValueAsString(rawBody);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    public static String getResponseBody(Object inputArgs) {
        try {
            return objectMapper.writeValueAsString(inputArgs);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }
}
