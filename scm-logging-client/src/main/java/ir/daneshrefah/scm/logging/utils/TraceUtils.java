package ir.daneshrefah.scm.logging.utils;

import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.constant.Constants;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TraceUtils {

    private static final Integer TRANSACTION_TYPE_REQUEST = 1;
    private static final Integer TRANSACTION_TYPE_RESPONSE = 2;
    @Getter
    private static TraceUtils instance;
    private final Tracer tracer;
    @Value("${scm.application.version:#{null}}")
    private String version;
    @Value("${scm.application.build:#{null}}")
    private String build;

    @PostConstruct
    public void init() {
        instance = this;
    }

    private Span getSpan(Exchange exchange) {
        Span span = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
        return Optional.ofNullable(span).orElseGet(() -> createExchangeSpan(exchange));
    }

    private Span createExchangeSpan(Exchange exchange) {
        Service service = (Service) exchange.getProperty(Message.SERVICE);
        Span parent = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
        SpanBuilder spanBuilder = tracer.spanBuilder(StringUtils.trim(service.getCode())).setSpanKind(SpanKind.SERVER);
        if (Objects.nonNull(parent)) {
            spanBuilder.setParent(Context.current().with(parent));
        }
        Span span = spanBuilder.startSpan();
        exchange.setProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN, span);
        return span;
    }

    private void apply(Exchange exchange, SpanProcess spanProcess) {
        Span span = getSpan(exchange);
        try (Scope ignored = span.makeCurrent()) {
            spanProcess.process(span);
        } finally {
            span.end();
            createExchangeSpan(exchange);
        }

    }

    public void traceBeforeRoute(Exchange exchange, Service service) {
        apply(exchange, span -> {
            span.setStatus(StatusCode.OK);
            span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), UUID.randomUUID().toString().replace("-", "")); // maximum char must be 32
            span.setAttribute(LogAttribute.MESSAGE_REQUEST.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : "");
            span.setAttribute(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), TRANSACTION_TYPE_REQUEST);
            trace(exchange, service, span);
        });
    }

    public void traceAfterRoute(Exchange exchange, Service service) {
        apply(exchange, span -> {
            Span exchangeSpan = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
            span.setStatus(StatusCode.OK);
            span.setAttribute(LogAttribute.MESSAGE_RESPONSE.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : "");
            span.setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), TRANSACTION_TYPE_RESPONSE);
            trace(exchange, service, exchangeSpan);
        });
    }

    public void traceException(Exchange exchange, Exception exception) {
        apply(exchange, span -> {
            recordExceptionTrace(exception, span);
            span.setStatus(StatusCode.ERROR);
        });
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
        span.setAttribute(LogAttribute.SERVICE_CODE.getAttributeName(), service != null ? service.getCode().trim() : "");
        span.setAttribute(LogAttribute.END_POINT.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class));
        span.setAttribute(LogAttribute.METHOD_TYPE.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class));
        span.setAttribute(LogAttribute.CLIENT_IP_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_REFERER, String.class));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_USERNAME, String.class));
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.VERSION.getAttributeName(), version + build);
    }

    private void recordExceptionTrace(Exception ex, Span span) {
        StackTraceElement[] stackTraceElements = Arrays.stream(ex.getStackTrace()).limit(3).toArray(StackTraceElement[]::new);
        ex.setStackTrace(stackTraceElements);
        String stackTraceString = ExceptionUtils.getStackTrace(ex);
        span.setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
        span.setAttribute(LogAttribute.ERROR_DETAILS.getAttributeName(), stackTraceString);
    }
}