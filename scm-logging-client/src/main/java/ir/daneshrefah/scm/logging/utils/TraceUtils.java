package ir.daneshrefah.scm.logging.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.camel.Exchange;
import org.apache.camel.tracing.ActiveSpanManager;
import org.apache.camel.tracing.SpanAdapter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
public class TraceUtils {

    private static final Integer TRANSACTION_TYPE_REQUEST = 1;
    private static final Integer TRANSACTION_TYPE_RESPONSE = 2;

    @Getter
    private static TraceUtils instance;
    @Value("${scm.application.version:#{null}}")
    private String version;
    @Value("${scm.application.build:#{null}}")
    private String build;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public void traceBeforeRoute(Exchange exchange, Service service) {
        Span span = Span.current();
        try (Scope scope = span.makeCurrent()) {
            span.setStatus(StatusCode.OK);
            SpanAdapter spanAdapter = ActiveSpanManager.getSpan(exchange);
            spanAdapter.setTag(LogAttribute.MESSAGE_ID.getAttributeName(), UUID.randomUUID().toString().replace("-", "")); // maximum char must be 32
            spanAdapter.setTag(LogAttribute.MESSAGE_REQUEST.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : "");
            spanAdapter.setTag(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), TRANSACTION_TYPE_REQUEST);
            trace(exchange, service, spanAdapter);
        } finally {
            span.end();
        }
    }

    public void traceAfterRoute(Exchange exchange, Service service) {
        Span span = Span.current();
        try (Scope scope = span.makeCurrent()) {
            SpanAdapter spanAdapter = ActiveSpanManager.getSpan(exchange);
            span.setStatus(StatusCode.OK);
            span.setAttribute(LogAttribute.MESSAGE_RESPONSE.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : "");
            span.setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), TRANSACTION_TYPE_RESPONSE);
            trace(exchange, service, spanAdapter);
        } finally {
            span.end();
        }
    }

    public void traceException(Exchange exchange, Exception exception) {
        Span span = Span.current();
        try (Scope scope = span.makeCurrent()) {
            recordExceptionTrace(exception, span);
            span.setStatus(StatusCode.ERROR);
        } finally {
            span.end();
        }
    }

    private void trace(Exchange exchange, Service service, SpanAdapter spanAdapter) {
        spanAdapter.setTag(LogAttribute.CHANNEL_CODE.getAttributeName(), exchange.getProperty(Message.CHANNEL_CODE) != null ? exchange.getProperty(Message.CHANNEL_CODE).toString() : "");
        spanAdapter.setTag(LogAttribute.CLIENT_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_ID, String.class));
        spanAdapter.setTag(LogAttribute.SERVICE_ID.getAttributeName(), service != null ? service.getId() : null);
        spanAdapter.setTag(LogAttribute.TERMINAL_CODE.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_TERMINAL, String.class));
        spanAdapter.setTag(LogAttribute.CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class));
        spanAdapter.setTag(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class));
        spanAdapter.setTag(LogAttribute.FLOW_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_FLOW_ID, String.class));
        spanAdapter.setTag(LogAttribute.HOST_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HEADER_HOST, String.class));
        spanAdapter.setTag(LogAttribute.SERVICE_CODE.getAttributeName(), service != null ? service.getCode().trim() : "");
        spanAdapter.setTag(LogAttribute.END_POINT.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class));
        spanAdapter.setTag(LogAttribute.METHOD_TYPE.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class));
        spanAdapter.setTag(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_REFERER, String.class));
        spanAdapter.setTag(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_USERNAME, String.class));
        spanAdapter.setTag(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        spanAdapter.setTag(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        spanAdapter.setTag(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        spanAdapter.setTag(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        spanAdapter.setTag(LogAttribute.VERSION.getAttributeName(), version + build);
    }

    private void recordExceptionTrace(Exception ex, Span span) {
        StackTraceElement[] stackTraceElements = Arrays.stream(ex.getStackTrace()).limit(3).toArray(StackTraceElement[]::new);
        ex.setStackTrace(stackTraceElements);
        String stackTraceString = ExceptionUtils.getStackTrace(ex);
        span.setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
        span.setAttribute(LogAttribute.ERROR_DETAILS.getAttributeName(), stackTraceString);
    }
}