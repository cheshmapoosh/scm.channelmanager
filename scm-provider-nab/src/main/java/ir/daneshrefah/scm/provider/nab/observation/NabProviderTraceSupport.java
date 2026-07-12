package ir.daneshrefah.scm.provider.nab.observation;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.provider.nab.config.NabConfigResolver;
import ir.daneshrefah.scm.provider.nab.config.NabResolvedConfig;
import ir.daneshrefah.scm.provider.nab.observation.attributes.NabTraceAttributes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class NabProviderTraceSupport {
    private static final String OPERATION_SCOPE_PROPERTY = "scm.observation.scope.operation";
    private static final String PROVIDER_NAME = "NAB";
    private static final String PROVIDER_TYPE = "tcp";

    private final List<NabProviderTraceAttributeContributor> contributors;
    private final Set<String> registeredAttributeNames;

    public NabProviderTraceSupport(ObjectProvider<NabProviderTraceAttributeContributor> contributors) {
        this.contributors = contributors.orderedStream().toList();
        this.registeredAttributeNames = registeredAttributeNames(this.contributors);
    }

    public ProviderAttempt startAttempt(
            Exchange exchange,
            NabResolvedConfig config,
            JsonNode request,
            String operation
    ) {
        ObservationScope scope = activeOperationScope(exchange);
        Map<String, Object> baseAttributes = baseAttributes(config, operation);
        Map<String, Object> requestAttributes = new LinkedHashMap<>(baseAttributes);
        Map<String, Object> contributedAttributes = new LinkedHashMap<>();
        contributeRequestAttributes(exchange, config, request, contributedAttributes);
        mergeContributed(requestAttributes, contributedAttributes);
        put(requestAttributes, "event.outcome", "success");
        addEvent(scope, "provider.request", requestAttributes);
        return new ProviderAttempt(scope, baseAttributes, System.nanoTime());
    }

    public void finishAttempt(
            ProviderAttempt attempt,
            Exchange exchange,
            NabResolvedConfig config,
            JsonNode response,
            Throwable failure
    ) {
        if (attempt == null || !attempt.markFinished()) {
            return;
        }
        Map<String, Object> attributes = new LinkedHashMap<>(attempt.baseAttributes());
        Map<String, Object> contributedAttributes = new LinkedHashMap<>();
        contributeResponseAttributes(exchange, config, response, failure, contributedAttributes);
        mergeContributed(attributes, contributedAttributes);
        put(attributes, NabTraceAttributes.PROVIDER_DURATION_MS.name(), elapsedMillis(attempt.startedAtNanos()));
        String responseCode = responseCode(response);
        put(attributes, NabTraceAttributes.PROVIDER_RESPONSE_CODE.name(), responseCode);

        boolean successfulResponse = response != null && response.path("status").path("success").asBoolean(false);
        boolean success = failure == null && successfulResponse;
        put(attributes, "event.outcome", success ? "success" : "failure");
        if (!success && failure == null) {
            put(attributes, NabTraceAttributes.PROVIDER_ERROR_CODE.name(), responseCode);
            put(attributes, "error.code", responseCode);
        }
        if (failure != null) {
            String errorCode = errorCode(failure);
            put(attributes, NabTraceAttributes.PROVIDER_ERROR_CODE.name(), errorCode);
            put(attributes, "error.type", failure.getClass().getSimpleName());
            put(attributes, "error.code", errorCode);
        }
        addEvent(attempt.scope(), "provider.response", attributes);
    }

    private Map<String, Object> baseAttributes(NabResolvedConfig config, String operation) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, NabTraceAttributes.PROVIDER_CODE.name(), config == null ? null : config.provider());
        put(attributes, NabTraceAttributes.PROVIDER_NAME.name(), PROVIDER_NAME);
        put(attributes, NabTraceAttributes.PROVIDER_TYPE.name(), PROVIDER_TYPE);
        put(attributes, NabTraceAttributes.PROVIDER_SCHEME.name(), NabConfigResolver.COMPONENT_SCHEME);
        put(attributes, NabTraceAttributes.PROVIDER_OPERATION.name(), operation);
        put(attributes, NabTraceAttributes.PROVIDER_ENDPOINT.name(), safeEndpoint(config == null ? null : config.endpoint()));
        return attributes;
    }

    private void contributeRequestAttributes(
            Exchange exchange,
            NabResolvedConfig config,
            JsonNode request,
            Map<String, Object> attributes
    ) {
        for (NabProviderTraceAttributeContributor contributor : contributors) {
            Map<String, Object> contributed = new LinkedHashMap<>();
            try {
                contributor.contributeRequestAttributes(exchange, config, request, contributed);
                attributes.putAll(normalizeContributedAttributes(contributed));
            } catch (RuntimeException exception) {
                log.warn("Ignoring NAB request trace attribute contributor failure contributor={} failureType={}",
                        contributor.getClass().getName(), exception.getClass().getSimpleName());
            }
        }
    }

    private void contributeResponseAttributes(
            Exchange exchange,
            NabResolvedConfig config,
            JsonNode response,
            Throwable failure,
            Map<String, Object> attributes
    ) {
        for (NabProviderTraceAttributeContributor contributor : contributors) {
            Map<String, Object> contributed = new LinkedHashMap<>();
            try {
                contributor.contributeResponseAttributes(exchange, config, response, failure, contributed);
                attributes.putAll(normalizeContributedAttributes(contributed));
            } catch (RuntimeException exception) {
                log.warn("Ignoring NAB response trace attribute contributor failure contributor={} failureType={}",
                        contributor.getClass().getName(), exception.getClass().getSimpleName());
            }
        }
    }

    private ObservationScope activeOperationScope(Exchange exchange) {
        return exchange == null ? null : exchange.getProperty(OPERATION_SCOPE_PROPERTY, ObservationScope.class);
    }

    private void addEvent(ObservationScope scope, String name, Map<String, Object> attributes) {
        if (scope == null) {
            return;
        }
        try {
            scope.event(name, attributes);
        } catch (RuntimeException exception) {
            log.warn("Ignoring NAB provider trace event failure eventName={} failureType={}",
                    name, exception.getClass().getSimpleName());
        }
    }

    private String responseCode(JsonNode response) {
        if (response == null) {
            return null;
        }
        return clean(response.path("status").path("code").asText(null));
    }

    private String errorCode(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current.getClass().getSimpleName().contains("Timeout")) {
                return "PROVIDER_TIMEOUT";
            }
            current = current.getCause();
        }
        return failure == null ? null : failure.getClass().getSimpleName();
    }

    private long elapsedMillis(long startedAtNanos) {
        return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, System.nanoTime() - startedAtNanos));
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

    private void mergeContributed(Map<String, Object> attributes, Map<String, Object> contributedAttributes) {
        contributedAttributes.forEach(attributes::putIfAbsent);
    }

    private void put(Map<String, Object> attributes, String name, Object value) {
        if (name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
    }

    private Map<String, Object> normalizeContributedAttributes(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> safe = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            try {
                String name = clean(entry.getKey());
                if (name == null || !registeredAttributeNames.contains(name) || entry.getValue() == null) {
                    continue;
                }
                Object value = immutableValue(entry.getValue());
                if (value != null) {
                    safe.put(name, value);
                }
            } catch (RuntimeException exception) {
                log.warn("Ignoring unsafe NAB trace attribute name={} failureType={}",
                        entry.getKey(), exception.getClass().getSimpleName());
            }
        }
        return safe;
    }

    private Object immutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> safeMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key) || key.isBlank() || entry.getValue() == null) {
                    continue;
                }
                Object nested = immutableValue(entry.getValue());
                if (nested != null) {
                    safeMap.put(key.trim(), nested);
                }
            }
            return Collections.unmodifiableMap(safeMap);
        }
        if (value instanceof Collection<?> collection) {
            List<Object> safeList = collection.stream()
                    .map(this::immutableValue)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            return List.copyOf(safeList);
        }
        if (value instanceof Object[] array) {
            List<Object> safeList = java.util.Arrays.stream(array)
                    .map(this::immutableValue)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            return List.copyOf(safeList);
        }
        return value;
    }

    private Set<String> registeredAttributeNames(List<NabProviderTraceAttributeContributor> contributors) {
        Set<String> names = new LinkedHashSet<>();
        for (var attribute : NabTraceAttributes.attributes()) {
            names.add(attribute.name());
        }
        if (contributors != null) {
            for (NabProviderTraceAttributeContributor contributor : contributors) {
                try {
                    Collection<?> attributes = contributor.attributes();
                    if (attributes == null) {
                        continue;
                    }
                    for (Object attribute : attributes) {
                        if (attribute instanceof ir.daneshrefah.scm.observation.starter.ObservationAttributeKey<?> key) {
                            names.add(key.name());
                        }
                    }
                } catch (RuntimeException exception) {
                    log.warn("Ignoring NAB trace attribute registration failure contributor={} failureType={}",
                            contributor.getClass().getName(), exception.getClass().getSimpleName());
                }
            }
        }
        return Set.copyOf(names);
    }

    public static final class ProviderAttempt {
        private final ObservationScope scope;
        private final Map<String, Object> baseAttributes;
        private final long startedAtNanos;
        private final AtomicBoolean finished = new AtomicBoolean();

        private ProviderAttempt(ObservationScope scope, Map<String, Object> baseAttributes, long startedAtNanos) {
            this.scope = scope;
            this.baseAttributes = Map.copyOf(baseAttributes);
            this.startedAtNanos = startedAtNanos;
        }

        private ObservationScope scope() {
            return scope;
        }

        private Map<String, Object> baseAttributes() {
            return baseAttributes;
        }

        private long startedAtNanos() {
            return startedAtNanos;
        }

        private boolean markFinished() {
            return finished.compareAndSet(false, true);
        }
    }
}
