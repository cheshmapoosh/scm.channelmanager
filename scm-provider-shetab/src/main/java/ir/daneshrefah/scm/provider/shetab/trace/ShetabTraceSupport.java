package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.apache.camel.Exchange;
import org.jpos.iso.ISOMsg;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
public class ShetabTraceSupport {
    private static final String OPERATION_SCOPE_PROPERTY = "scm.observation.scope.operation";

    public <T> T clientSpan(Exchange exchange, ShetabResolvedConfig config, ISOMsg request, Supplier<T> action) {
        String primaryEndpoint = primaryEndpoint(config);
        EndpointParts endpointParts = primaryEndpoint == null ? null : parseEndpoint(primaryEndpoint);
        Map<String, Object> attributes = new java.util.LinkedHashMap<>();
        put(attributes, "scm.provider.name", config == null ? null : config.provider());
        put(attributes, "scm.provider.scheme", config == null ? null : config.scheme());
        put(attributes, "scm.provider.uri", providerUri(config));
        put(attributes, "shetab.endpoint.primary", primaryEndpoint);
        put(attributes, "net.peer.name", endpointParts == null ? null : endpointParts.host());
        put(attributes, "net.peer.port", endpointParts == null ? null : endpointParts.port());
        put(attributes, "shetab.iso.mti", safeMti(request));
        put(attributes, "shetab.iso.stan", safeField(request, 11));
        put(attributes, "shetab.iso.rrn", safeField(request, 37));

        try {
            T result = action.get();
            put(attributes, "event.outcome", "success");
            addOperationEvent(exchange, "provider.shetab.call", attributes);
            return result;
        } catch (RuntimeException e) {
            put(attributes, "event.outcome", "failure");
            put(attributes, "error.type", e.getClass().getName());
            put(attributes, "error.code", e.getClass().getSimpleName());
            addOperationEvent(exchange, "provider.shetab.call", attributes);
            throw e;
        }
    }

    public void customizerSpan(
            Exchange exchange,
            ProviderMessageCustomizerContext context,
            String customizerType,
            String phase,
            Runnable action
    ) {
        Map<String, Object> attributes = new java.util.LinkedHashMap<>();
        put(attributes, "scm.provider.name", context == null ? null : context.providerCode());
        put(attributes, "scm.provider.scheme", context == null ? null : context.scheme());
        put(attributes, "scm.provider.uri", context == null ? null : context.providerUri());
        put(attributes, "scm.provider.service_code", context == null ? null : context.serviceCode());
        put(attributes, "scm.provider.operation_code", context == null ? null : context.operationCode());
        put(attributes, "scm.provider.channel_code", context == null ? null : context.channelCode());
        put(attributes, "scm.provider.customizer.type", customizerType);
        put(attributes, "scm.provider.customizer.phase", phase);

        try {
            action.run();
            put(attributes, "event.outcome", "success");
            addOperationEvent(exchange, "provider.customizer.execute", attributes);
        } catch (RuntimeException e) {
            put(attributes, "event.outcome", "failure");
            put(attributes, "error.type", e.getClass().getName());
            put(attributes, "error.code", e.getClass().getSimpleName());
            addOperationEvent(exchange, "provider.customizer.execute", attributes);
            throw e;
        }
    }

    private void addOperationEvent(Exchange exchange, String name, Map<String, Object> attributes) {
        if (exchange == null) {
            return;
        }
        ObservationScope scope = exchange.getProperty(OPERATION_SCOPE_PROPERTY, ObservationScope.class);
        if (scope != null) {
            scope.event(name, attributes);
        }
    }

    private void put(Map<String, Object> attributes, String name, Object value) {
        if (attributes != null && name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
    }

    public void enrichLogMdc(Exchange exchange) {
        // Trace context is managed by scm-observation-starter/Micrometer.
        // Keep this method as a compatibility hook for existing producer code.
    }

    public Map<String, String> currentTraceIds() {
        return Map.of(
                "traceId", value(MDC.get("traceId")),
                "spanId", value(MDC.get("spanId"))
        );
    }

    private String providerUri(ShetabResolvedConfig config) {
        if (config == null) {
            return "";
        }
        return value(config.scheme()) + ":" + value(config.provider());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String safeMti(ISOMsg msg) {
        try {
            return msg != null && msg.hasMTI() ? msg.getMTI() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String primaryEndpoint(ShetabResolvedConfig config) {
        if (config == null || config.endpoints() == null || config.endpoints().isEmpty()) {
            return null;
        }
        String endpoint = config.endpoints().get(0);
        if (endpoint == null || endpoint.isBlank()) {
            return null;
        }
        return endpoint.trim();
    }

    private EndpointParts parseEndpoint(String endpoint) {
        int separator = endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            return null;
        }
        try {
            String host = endpoint.substring(0, separator).trim();
            int port = Integer.parseInt(endpoint.substring(separator + 1).trim());
            if (host.isBlank() || port < 1) {
                return null;
            }
            return new EndpointParts(host, port);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record EndpointParts(String host, int port) {
    }
}
