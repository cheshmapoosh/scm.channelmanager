package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ShetabTraceSupport {
    private static final String OPERATION_SCOPE_PROPERTY = "scm.observation.scope.operation";
    private static final String PROVIDER_NAME = "SHETAB";
    private static final String PROVIDER_TYPE = "tcp";

    private final List<ShetabProviderTraceAttributeContributor> contributors;

    public ShetabTraceSupport(ObjectProvider<ShetabProviderTraceAttributeContributor> contributors) {
        this.contributors = contributors.orderedStream().toList();
    }

    public ShetabProviderTraceLifecycle lifecycle(
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg request,
            String operation
    ) {
        Map<String, Object> baseAttributes = baseAttributes(config, operation);
        Map<String, Object> requestAttributes = new LinkedHashMap<>();
        contributeRequestAttributes(exchange, config, request, requestAttributes);
        return new ShetabProviderTraceLifecycle(
                this, activeOperationScope(exchange), baseAttributes, requestAttributes);
    }

    public void finishAttempt(
            ShetabProviderTraceLifecycle.Attempt attempt,
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg response,
            Throwable failure
    ) {
        if (attempt == null) {
            return;
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        contributeResponseAttributes(exchange, config, response, failure, attributes);
        String responseCode = safeField(response, 39);
        attempt.finish(responseCode, response != null, failure, attributes);
    }

    void addOperationEvent(ObservationScope scope, String name, Map<String, Object> attributes) {
        if (scope == null) {
            return;
        }
        try {
            scope.event(name, attributes);
        } catch (RuntimeException exception) {
            log.warn("Ignoring Shetab provider trace event failure eventName={} failureType={}",
                    name, exception.getClass().getSimpleName());
        }
    }

    private Map<String, Object> baseAttributes(ShetabResolvedConfig config, String operation) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, ShetabTraceAttributes.PROVIDER_CODE.name(), config == null ? null : config.provider());
        put(attributes, ShetabTraceAttributes.PROVIDER_NAME.name(), PROVIDER_NAME);
        put(attributes, ShetabTraceAttributes.PROVIDER_TYPE.name(), PROVIDER_TYPE);
        put(attributes, ShetabTraceAttributes.PROVIDER_SCHEME.name(), config == null ? null : config.scheme());
        put(attributes, ShetabTraceAttributes.PROVIDER_OPERATION.name(), operation);
        put(attributes, ShetabTraceAttributes.PROVIDER_ENDPOINT.name(), safeEndpoint(primaryEndpoint(config)));
        return attributes;
    }

    private void contributeRequestAttributes(
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg request,
            Map<String, Object> attributes
    ) {
        for (ShetabProviderTraceAttributeContributor contributor : contributors) {
            try {
                contributor.contributeRequestAttributes(exchange, config, request, attributes);
            } catch (RuntimeException exception) {
                log.warn("Ignoring Shetab request trace attribute contributor failure contributor={} failureType={}",
                        contributor.getClass().getName(), exception.getClass().getSimpleName());
            }
        }
    }

    private void contributeResponseAttributes(
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg response,
            Throwable failure,
            Map<String, Object> attributes
    ) {
        for (ShetabProviderTraceAttributeContributor contributor : contributors) {
            try {
                contributor.contributeResponseAttributes(exchange, config, response, failure, attributes);
            } catch (RuntimeException exception) {
                log.warn("Ignoring Shetab response trace attribute contributor failure contributor={} failureType={}",
                        contributor.getClass().getName(), exception.getClass().getSimpleName());
            }
        }
    }

    private ObservationScope activeOperationScope(Exchange exchange) {
        return exchange == null ? null : exchange.getProperty(OPERATION_SCOPE_PROPERTY, ObservationScope.class);
    }

    private String safeField(ISOMsg message, int field) {
        try {
            return message == null ? null : clean(message.getString(field));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String primaryEndpoint(ShetabResolvedConfig config) {
        if (config == null || config.endpoints() == null || config.endpoints().isEmpty()) {
            return null;
        }
        return clean(config.endpoints().get(0));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeEndpoint(String value) {
        String endpoint = clean(value);
        if (endpoint == null || endpoint.indexOf('@') >= 0 || endpoint.indexOf('/') >= 0
                || endpoint.indexOf('?') >= 0 || endpoint.indexOf('#') >= 0) {
            return null;
        }
        int separator = endpoint.lastIndexOf(':');
        if (separator <= 0 || separator == endpoint.length() - 1) {
            return null;
        }
        try {
            int port = Integer.parseInt(endpoint.substring(separator + 1));
            return port > 0 && port <= 65_535 ? endpoint : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void put(Map<String, Object> attributes, String name, Object value) {
        if (name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
    }
}
