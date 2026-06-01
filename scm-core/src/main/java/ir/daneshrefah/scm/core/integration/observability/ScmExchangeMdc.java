package ir.daneshrefah.scm.core.integration.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.utils.constant.Constants;
import org.apache.camel.Exchange;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ScmExchangeMdc {

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

    public Map<String, String> fields(Exchange exchange) {
        Map<String, String> fields = new LinkedHashMap<>();
        SpanContext spanContext = spanContext(exchange);
        fields.put("traceId", spanContext != null ? spanContext.getTraceId() : property(exchange, Message.TRACE_ID));
        fields.put("spanId", spanContext != null ? spanContext.getSpanId() : property(exchange, Message.SPAN_ID));
        fields.put("correlationId", correlationId(exchange));
        fields.put("gatewayName", gatewayName(exchange));
        fields.put("channelCode", channelCode(exchange));
        fields.put("serviceCode", serviceCode(exchange));
        fields.put("operationName", operationName(exchange));
        fields.put("routeId", exchange.getFromRouteId());
        fields.put("exchangeId", exchange.getExchangeId());
        return fields;
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
        String correlationId = exchange.getMessage().getHeader(Constants.SCM_PARAMETER_CORRELATION_ID, String.class);
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
}
