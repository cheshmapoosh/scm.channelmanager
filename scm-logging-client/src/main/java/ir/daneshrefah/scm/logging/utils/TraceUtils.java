package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Getter
    private static TraceUtils instance;
    private final Tracer tracer;
    private final LegacyGatewayLogSpanEnricher legacyGatewayLogSpanEnricher;
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

    private void apply(Exchange exchange, Span span,SpanProcess spanProcess, boolean endSpan) {
        if (Objects.nonNull(span)) {
            try (Scope ignored = span.makeCurrent()) {
                spanProcess.process(span);
            } finally {
                if (endSpan) {
                    span.end();
                    exchange.removeProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
                }
            }
        }
    }

    private void apply(Exchange exchange, SpanProcess spanProcess, boolean endSpan) {
        Span span = getSpan(exchange);
        if (Objects.nonNull(span)) {
            try (Scope ignored = span.makeCurrent()) {
                spanProcess.process(span);
            } finally {
                if (endSpan) {
                    span.end();
                    exchange.removeProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
                }
            }
        }
    }

    public void traceScmRequest(Exchange exchange,Service service) {
        Span span = createExchangeSpan(exchange);
        String messageId = UUID.randomUUID().toString().replace("-", "");
        span.setStatus(StatusCode.OK);
        span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), messageId); // maximum char must be 32
        span.setAttribute(LogAttribute.MESSAGE_REQUEST.getAttributeName(), exchange.getIn() != null ? maskCvv2(exchange.getIn().getBody(String.class)) : "");
        span.setAttribute(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), TRANSACTION_TYPE_REQUEST);
        trace(exchange, service, span);
        legacyGatewayLogSpanEnricher.enrichGatewayRequest(exchange, service, span, messageId);
        putTraceMdc(span);
    }

    public void traceScmResponse(Exchange exchange,Service service) {
        Span localSpan = legacyGatewayLogSpanEnricher.gatewaySpan(exchange);
        apply(exchange, localSpan, (span) -> {
            span.setStatus(StatusCode.OK);
            trace(exchange, service, span);
            legacyGatewayLogSpanEnricher.enrichGatewayResponse(exchange, service, span);
        },true);
    }


    public void traceBeforeOperation(Exchange exchange, Operation operation) {
        Span span = createExchangeSpan(exchange);
        span.setStatus(StatusCode.OK);
        span.setAttribute(LogAttribute.MESSAGE_ID.getAttributeName(), UUID.randomUUID().toString().replace("-", "")); // maximum char must be 32
        span.setAttribute(LogAttribute.MESSAGE_REQUEST.getAttributeName(), exchange.getIn() != null ? exchange.getIn().getBody(String.class) : "");
        span.setAttribute(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), TRANSACTION_TYPE_REQUEST);
        trace(exchange, operation, span);
        legacyGatewayLogSpanEnricher.markScmWebSpan(exchange, span);
        putTraceMdc(span);
    }


    public void traceAfterOperation(Exchange exchange, Operation operation) {
        legacyGatewayLogSpanEnricher.captureProviderResponse(exchange, operation);
        apply(exchange, span -> {
            Span exchangeSpan = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
            span.setStatus(StatusCode.OK);
            span.setAttribute(LogAttribute.MESSAGE_RESPONSE.getAttributeName(), exchange.getIn() != null ? removeImageUrl(exchange.getIn().getBody(String.class)) : "");
            span.setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), TRANSACTION_TYPE_RESPONSE);
            trace(exchange, operation, exchangeSpan);
            legacyGatewayLogSpanEnricher.markScmWebSpan(exchange, exchangeSpan);
        }, true);
    }

    public void traceException(Exchange exchange, Exception exception) {
        legacyGatewayLogSpanEnricher.recordException(exchange, exception);
        Span currentSpan = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
        if (legacyGatewayLogSpanEnricher.isGatewaySpan(exchange, currentSpan)) {
            apply(exchange, currentSpan, span -> {
                if (exception != null) {
                    recordExceptionTrace(exception, span);
                }
                span.setStatus(StatusCode.ERROR);
                putTraceMdc(span);
            }, false);
            return;
        }
        apply(exchange, span -> {
            if (exception != null) {
                recordExceptionTrace(exception, span);
            }
            span.setStatus(StatusCode.ERROR);
            putTraceMdc(span);
        },true);
    }

    private void putTraceMdc(Span span) {
        SpanContext spanContext = span.getSpanContext();
        MDC.put("traceId", spanContext.getTraceId());
        MDC.put("spanId", spanContext.getSpanId());
    }

    private void trace(Exchange exchange, Service service, Span span) {
        span.setAttribute(LogAttribute.CHANNEL_CODE.getAttributeName(), exchange.getProperty(Message.CHANNEL_CODE) != null ? exchange.getProperty(Message.CHANNEL_CODE).toString() : "");
        span.setAttribute(LogAttribute.CLIENT_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_ID, String.class));
        span.setAttribute(LogAttribute.SERVICE_ID.getAttributeName(), service != null ? String.valueOf(service.getId()) : "null");
        span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_TERMINAL, String.class));
        span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class));
        span.setAttribute(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class));
        span.setAttribute(LogAttribute.FLOW_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_FLOW_ID, String.class));
        span.setAttribute(LogAttribute.HOST_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HEADER_HOST, String.class));
        span.setAttribute(LogAttribute.SERVICE_CODE.getAttributeName(), service != null ? service.getCode().trim() : "");
        span.setAttribute(LogAttribute.END_POINT.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class));
        span.setAttribute(LogAttribute.METHOD_TYPE.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class));
        span.setAttribute(LogAttribute.CLIENT_IP_ADDRESS.getAttributeName(), legacyGatewayLogSpanEnricher.clientIpAddress(exchange));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_USERNAME, String.class));
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.VERSION.getAttributeName(), version);
    }

    private void trace(Exchange exchange, Operation operation, Span span) {
        span.setAttribute(LogAttribute.CHANNEL_CODE.getAttributeName(), exchange.getProperty(Message.CHANNEL_CODE) != null ? exchange.getProperty(Message.CHANNEL_CODE).toString() : "");
        span.setAttribute(LogAttribute.TERMINAL_CODE.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_TERMINAL, String.class));
        span.setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class));
        span.setAttribute(LogAttribute.CLIENT_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_ID, String.class));
        span.setAttribute(LogAttribute.HOST_ADDRESS.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HEADER_HOST, String.class));
        span.setAttribute(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class));
        span.setAttribute(LogAttribute.FLOW_ID.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_FLOW_ID, String.class));
        span.setAttribute(LogAttribute.SERVICE_CODE.getAttributeName(), operation != null ? operation.getName().trim() : "");
        span.setAttribute(LogAttribute.METHOD_TYPE.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_METHOD, String.class));
        span.setAttribute(LogAttribute.END_POINT.getAttributeName(), exchange.getMessage().getHeader(Constants.CAMEL_PARAMETER_HTTP_URI, String.class));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), exchange.getMessage().getHeader(Constants.SCM_PARAMETER_USERNAME, String.class));
        span.setAttribute(LogAttribute.CLIENT_IP_ADDRESS.getAttributeName(), legacyGatewayLogSpanEnricher.clientIpAddress(exchange));
        span.setAttribute(LogAttribute.DELEGATOR_NICKNAME.getAttributeName(), AuthenticationUtils.getDelegatorNickname().orElse(""));
        span.setAttribute(LogAttribute.USERNAME.getAttributeName(), AuthenticationUtils.getEffectiveUsername().orElse(""));
        span.setAttribute(LogAttribute.NICKNAME.getAttributeName(), AuthenticationUtils.getEffectiveNickname().orElse(""));
        span.setAttribute(LogAttribute.DELEGATOR_USERNAME.getAttributeName(), AuthenticationUtils.getDelegatorUsername().orElse(""));
        span.setAttribute(LogAttribute.VERSION.getAttributeName(), version);
    }

    private void recordExceptionTrace(Exception ex, Span span) {
        StackTraceElement[] stackTraceElements = Arrays.stream(ex.getStackTrace()).limit(3).toArray(StackTraceElement[]::new);
        ex.setStackTrace(stackTraceElements);
        String stackTraceString = ExceptionUtils.getStackTrace(ex);
        span.setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), ex.getClass().getName());
        span.setAttribute(LogAttribute.ERROR_DETAILS.getAttributeName(), stackTraceString);
    }

    private String maskCvv2(String body) {
        if (StringUtils.isBlank(body)) {
            return body;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            maskCvv2(root);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            return body;
        }
    }

    private void maskCvv2(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode cvv2 = objectNode.get("cvv2");
            if (cvv2 != null) {
                objectNode.put("cvv2", maskLongValue(cvv2.asText()));
            }
            objectNode.fields().forEachRemaining(entry -> maskCvv2(entry.getValue()));
            return;
        }
        if (node.isArray()) {
            node.forEach(this::maskCvv2);
        }
    }

    private String removeImageUrl(String body) {
        if (StringUtils.isBlank(body)) {
            return body;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            removeImageUrl(root);
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            return body;
        }
    }

    private void removeImageUrl(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.remove("imageUrl");
            objectNode.fields().forEachRemaining(entry -> removeImageUrl(entry.getValue()));
            return;
        }
        if (node.isArray()) {
            node.forEach(this::removeImageUrl);
        }
    }

    private String maskLongValue(String value) {
        if (value == null || value.length() <= 6) {
            return "****";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }
}
