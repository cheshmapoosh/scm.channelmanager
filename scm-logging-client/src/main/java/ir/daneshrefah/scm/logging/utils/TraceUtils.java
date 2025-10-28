package ir.daneshrefah.scm.logging.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.camel.Exchange;
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

    @PostConstruct
    public void init() {
        instance = this;
    }

    public void traceBeforeRoute(Exchange exchange, Service service) {
//        SpanAdapter adapter = ActiveSpanManager.getSpan(exchange);
        Span span = Span.current();
        span.setStatus(StatusCode.OK);
        span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), UUID.randomUUID().toString().replace("-", "")); // maximum char must be 32
        span.setAttribute(LogAttribute.MESSAGE_REQUEST.getAttributeName(), LogUtils.getInstance().getMessage(exchange));
        span.setAttribute(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), TRANSACTION_TYPE_REQUEST);
        trace(exchange, service, span);
    }

    public void traceAfterRoute(Exchange exchange, Service service) {
//        SpanAdapter span = ActiveSpanManager.getSpan(exchange);
        Span span = Span.current();
        span.setStatus(StatusCode.OK);
        span.setAttribute(LogAttribute.MESSAGE_RESPONSE.getAttributeName(), LogUtils.getInstance().getMessage(exchange));
        span.setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), TRANSACTION_TYPE_RESPONSE);
        trace(exchange, service, span);
    }

    public void traceException(Exchange exchange, Exception exception) {
//        SpanAdapter span = ActiveSpanManager.getSpan(exchange);
        Span span = Span.current();
        span.setStatus(StatusCode.ERROR);
        recordExceptionTrace(exception, span);
    }

    private void trace(Exchange exchange, Service service, Span span) {
        span.setAttribute(LogAttribute.CHANNEL_CODE.getAttributeName(), exchange.getProperty(Message.CHANNEL_CODE) != null ? exchange.getProperty(Message.CHANNEL_CODE).toString() : "");
        span.setAttribute(LogAttribute.CLIENT_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_ID, String.class));
        span.setAttribute(LogAttribute.SERVICE_ID.getAttributeName(), service != null ? service.getId() : null);
        span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_TERMINAL, String.class));
        span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class));
        span.setAttribute(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class));
        span.setAttribute(LogAttribute.FLOW_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_FLOW_ID, String.class));
        span.setAttribute(LogAttribute.HOST_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HEADER_HOST, String.class));
        span.setAttribute(LogAttribute.SERVICE_CODE.getAttributeName(), service != null ? service.getCode() : "");
        span.setAttribute(LogAttribute.END_POINT.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class));
        span.setAttribute(LogAttribute.METHOD_TYPE.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class));
        span.setAttribute(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_REFERER, String.class));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_USERNAME, String.class));
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.VERSION.getAttributeName(), version);
    }

    public void recordExceptionTrace(Exception ex, Span span) {
        StackTraceElement[] stackTraceElements = Arrays.stream(ex.getStackTrace()).limit(3).toArray(StackTraceElement[]::new);
        ex.setStackTrace(stackTraceElements);
        String stackTraceString = ExceptionUtils.getStackTrace(ex);
        span.setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
        span.setAttribute(LogAttribute.ERROR_DETAILS.getAttributeName(), stackTraceString);
    }
}