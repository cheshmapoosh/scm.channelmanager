package ir.daneshrefah.scm.logging.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.logging.model.LogContext;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.*;

@Aspect
@Component
@ConditionalOnProperty(name = "scm.log.aspect.enable", havingValue = "true",matchIfMissing = true)
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    @Around("@within(org.springframework.web.bind.annotation.RestController)    &&" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)    ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)    ||" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping)   ||" +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)  ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)&& " +
            "!@annotation(ir.daneshrefah.scm.logging.aspect.annotation.SkipLog)")
    private Object logAdviser(ProceedingJoinPoint joinPoint) throws Throwable {
        LogContext logContext = LogContext.builder()
                .correlationId(UUID.randomUUID().toString())
                .messageId(UUID.randomUUID().toString())
                .build();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        Span parentSpan = tracer.spanBuilder(request.getServletPath()).setSpanKind(SpanKind.SERVER).startSpan();
        parentSpan.setStatus(StatusCode.OK);
        try {
            setSpanAttributes(request, response, null, logContext, parentSpan);
            Object result = joinPoint.proceed();
            try (Scope rootScope = parentSpan.makeCurrent()) {
                LogContext context = LogContext.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .parentMessageId(logContext.getMessageId())
                        .messageId(UUID.randomUUID().toString())
                        .build();
                Span childSpan = tracer.spanBuilder(request.getServletPath()).setSpanKind(SpanKind.SERVER).startSpan();
                setSpanAttributes(request, response, result, context, childSpan);
                childSpan.setStatus(StatusCode.OK);
                childSpan.setAttribute(LogAttribute.MESSAGE_STATUS.getAttributeName(), MessageStatus.SC_SUCCESS.toString());
                childSpan.end();
            }
            parentSpan.setAttribute(LogAttribute.MESSAGE_STATUS.getAttributeName(), MessageStatus.SC_SUCCESS.toString());
            parentSpan.setStatus(StatusCode.OK);
            return result;
        } catch (Throwable ex) {
            parentSpan.setStatus(StatusCode.ERROR);
            parentSpan.setAttribute(LogAttribute.MESSAGE_STATUS.getAttributeName(), MessageStatus.SC_ERROR_SYSTEM.toString());
            parentSpan.setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
            parentSpan.recordException(ex);
            throw ex;
        } finally {
            parentSpan.end();
        }
    }

    public void setSpanAttributes(HttpServletRequest request, HttpServletResponse response, Object returnValue, LogContext logContext, Span span) {
        span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), AuthenticationUtils.getLoggedInUser() != null ? AuthenticationUtils.getLoggedInUser().getTerminalCode() : "");
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), logContext.getCorrelationId());
        span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), logContext.getMessageId());
        span.setAttribute(LogAttribute.PARENT_MESSAGE_ID.getAttributeName(), logContext.getParentMessageId());
        span.setAttribute(LogAttribute.THREAD_NAME.getAttributeName(), Thread.currentThread().getName());
        span.setAttribute(LogAttribute.HOST_ADDRESS.getAttributeName(), request.getRemoteAddr());
        span.setAttribute(LogAttribute.METHOD_TYPE.getAttributeName(), request.getMethod());
        span.setAttribute(LogAttribute.REQUEST.getAttributeName(), getRequestBody(request));
        span.setAttribute(LogAttribute.HOST_ADDRESS.getAttributeName(), request.getRemoteAddr());
        span.setAttribute(LogAttribute.RESPONSE_STATUS_CODE.getAttributeName(), String.valueOf(response.getStatus()));
        span.setAttribute(LogAttribute.RESPONSE.getAttributeName(), getResponseBody(returnValue));
        span.setAttribute(LogAttribute.END_POINT.getAttributeName(), "rest::%s".formatted(request.getServletPath()));
        span.setAttribute(LogAttribute.CLIENT_FLOW_ID.getAttributeName(), request.getHeader(Constants.SCM_PARAMETER_CLIENT_FLOW_ID));
    }

    public String getRequestBody(HttpServletRequest request) {
        String requestBody = null;
        if (request instanceof ContentCachingRequestWrapper contentCachingRequestWrapper) {
            requestBody = new String(contentCachingRequestWrapper.getContentAsByteArray());
        }
        return requestBody != null ? requestBody.trim() : null;
    }

    public String getResponseBody(Object inputArgs) {
        try {
            String responseStr = objectMapper.writeValueAsString(inputArgs);
            return responseStr == null ? null : responseStr.replace(" ", "").trim();
        } catch (Exception e) {
            return null;
        }
    }
}


