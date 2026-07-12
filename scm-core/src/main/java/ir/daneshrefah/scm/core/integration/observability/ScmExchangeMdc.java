package ir.daneshrefah.scm.core.integration.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.TraceContext;
import ir.daneshrefah.scm.observation.starter.logging.ScmMdcKeys;
import ir.daneshrefah.scm.utils.constant.Constants;
import org.apache.camel.Exchange;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScmExchangeMdc {
    private static final String X_CORRELATION_ID = "X-Correlation-Id";

    private static final List<String> MDC_KEYS = List.of(
            ScmMdcKeys.TRACE_ID,
            ScmMdcKeys.SPAN_ID,
            ScmMdcKeys.CORRELATION_ID,
            ScmMdcKeys.CORRELATION_TYPE,
            ScmMdcKeys.GATEWAY_NAME,
            ScmMdcKeys.CHANNEL_CODE,
            ScmMdcKeys.SERVICE_CODE,
            ScmMdcKeys.SERVICE_VERSION,
            ScmMdcKeys.OPERATION_NAME,
            ScmMdcKeys.ROUTE_ID,
            ScmMdcKeys.EXCHANGE_ID);

    public Map<String, String> put(Exchange exchange) {
        Map<String, String> fields = fields(exchange);
        fields.forEach((key, value) -> {
            if (value != null) {
                MDC.put(key, value);
            }
        });
        exchange.setProperty(Message.TRACE_ID, fields.get("traceId"));
        exchange.setProperty(Message.SPAN_ID, fields.get("spanId"));
        exchange.setProperty(Message.CORRELATION_ID, fields.get("correlationId"));
        return fields;
    }

    public Binding bind(Exchange exchange) {
        Binding binding = Binding.capture(MDC_KEYS);
        put(exchange);
        return binding;
    }

    public void clear() {
        MDC_KEYS.forEach(MDC::remove);
    }

    public Map<String, String> fields(Exchange exchange) {
        Map<String, String> fields = new LinkedHashMap<>();
        TraceContext scmContext = activeTraceContext(exchange);
        SpanContext spanContext = scmContext == null ? spanContext(exchange) : null;
        fields.put(ScmMdcKeys.TRACE_ID, scmContext != null ? scmContext.traceId() : spanContext != null ? spanContext.getTraceId() : property(exchange, Message.TRACE_ID));
        fields.put(ScmMdcKeys.SPAN_ID, scmContext != null ? scmContext.spanId() : spanContext != null ? spanContext.getSpanId() : property(exchange, Message.SPAN_ID));
        fields.put(ScmMdcKeys.CORRELATION_ID, scmContext != null && scmContext.correlationId() != null ? scmContext.correlationId() : correlationId(exchange));
        fields.put(ScmMdcKeys.CORRELATION_TYPE, scmContext != null && scmContext.correlationType() != null ? scmContext.correlationType() : CorrelationType.OPERATION.value());
        fields.put(ScmMdcKeys.GATEWAY_NAME, gatewayName(exchange));
        fields.put(ScmMdcKeys.CHANNEL_CODE, channelCode(exchange));
        fields.put(ScmMdcKeys.SERVICE_CODE, serviceCode(exchange));
        fields.put(ScmMdcKeys.SERVICE_VERSION, property(exchange, Message.SERVICE_VERSION));
        fields.put(ScmMdcKeys.OPERATION_NAME, operationName(exchange));
        fields.put(ScmMdcKeys.ROUTE_ID, exchange.getFromRouteId());
        fields.put(ScmMdcKeys.EXCHANGE_ID, exchange.getExchangeId());
        return fields;
    }

    private TraceContext activeTraceContext(Exchange exchange) {
        if (exchange == null) {
            return null;
        }
        TraceContext operation = exchange.getProperty(
                CoreObservationTraceSupport.OPERATION_CONTEXT_PROPERTY,
                TraceContext.class
        );
        if (operation != null) {
            return operation;
        }
        TraceContext service = exchange.getProperty(
                CoreObservationTraceSupport.SERVICE_CONTEXT_PROPERTY,
                TraceContext.class
        );
        if (service != null) {
            return service;
        }
        return exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_CONTEXT_PROPERTY,
                TraceContext.class
        );
    }

    private SpanContext spanContext(Exchange exchange) {
        Span span = exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN, Span.class);
        if (span == null) {
            span = Span.current();
        }
        SpanContext spanContext = span != null ? span.getSpanContext() : null;
        return spanContext != null && spanContext.isValid() ? spanContext : null;
    }

    private String correlationId(Exchange exchange) {
        String correlationId = exchange.getMessage().getHeader(X_CORRELATION_ID, String.class);
        if (correlationId == null) {
            correlationId = exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class);
        }
        if (correlationId == null) {
            correlationId = exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class);
        }
        return correlationId != null ? correlationId : property(exchange, Message.CORRELATION_ID);
    }

    private String gatewayName(Exchange exchange) {
        GatewayChannel gatewayChannel = exchange.getProperty(Message.GATEWAY_CHANNEL, GatewayChannel.class);
        if (gatewayChannel != null) {
            return gatewayChannel.getName();
        }
        return property(exchange, Message.GATEWAY_NAME);
    }

    private String channelCode(Exchange exchange) {
        String channelCode = property(exchange, Message.CHANNEL_CODE);
        if (channelCode != null) {
            return channelCode;
        }
        ChannelServiceAccess access = exchange.getProperty(Message.CHANNEL_SERVICE_ACCESS, ChannelServiceAccess.class);
        return access != null && access.getChannel() != null ? access.getChannel().getCode() : null;
    }

    private String serviceCode(Exchange exchange) {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        return service != null ? service.getCode() : null;
    }

    private String operationName(Exchange exchange) {
        String operationName = property(exchange, Message.OPERATION_NAME);
        if (operationName != null) {
            return operationName;
        }
        ServiceOperation serviceOperation = exchange.getProperty(Message.SERVICE_OPERATION, ServiceOperation.class);
        return serviceOperation != null ? serviceOperation.getOperationName() : null;
    }

    private String property(Exchange exchange, String key) {
        Object value = exchange.getProperty(key);
        return value != null ? String.valueOf(value) : null;
    }

    public record Binding(Map<String, String> previousValues) implements AutoCloseable {
        private static Binding capture(List<String> keys) {
            Map<String, String> values = new LinkedHashMap<>();
            for (String key : keys) {
                values.put(key, MDC.get(key));
            }
            return new Binding(values);
        }

        @Override
        public void close() {
            previousValues.forEach((key, previousValue) -> {
                if (previousValue == null) {
                    MDC.remove(key);
                } else {
                    MDC.put(key, previousValue);
                }
            });
        }
    }
}
