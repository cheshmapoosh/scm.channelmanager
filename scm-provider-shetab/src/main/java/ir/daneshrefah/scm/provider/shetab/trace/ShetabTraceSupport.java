package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import ir.daneshrefah.scm.provider.shetab.iso.util.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class ShetabTraceSupport {
    private static final String OPERATION_SCOPE_PROPERTY = "scm.observation.scope.operation";
    private static final String PROVIDER_NAME = "SHETAB";
    private static final String PROVIDER_TYPE = "tcp";
    private static final String DEFAULT_SUCCESS_RESPONSE_CODE = ResponseCode.APPROVED.getCode();

    private final List<ShetabProviderTraceAttributeContributor> contributors;
    private final Set<String> registeredAttributeNames;

    public ShetabTraceSupport(ObjectProvider<ShetabProviderTraceAttributeContributor> contributors) {
        this.contributors = contributors.orderedStream().toList();
        this.registeredAttributeNames = registeredAttributeNames(this.contributors);
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
                this, exchange, config, activeOperationScope(exchange), baseAttributes, requestAttributes);
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
        String responseCode = safeField(response, 39);
        finishAttempt(attempt, exchange, config, new ShetabProviderAttemptResult(
                response,
                failure == null
                        ? providerOutcome(config, responseCode)
                        : ProviderBusinessOutcome.technicalFailure(responseCode, failure),
                failure
        ));
    }

    public void finishAttempt(
            ShetabProviderTraceLifecycle.Attempt attempt,
            Exchange exchange,
            ShetabResolvedConfig config,
            ShetabProviderAttemptResult result
    ) {
        if (attempt == null || !attempt.markFinished()) {
            return;
        }
        ShetabProviderAttemptResult safeResult = result == null
                ? new ShetabProviderAttemptResult(null, ProviderBusinessOutcome.businessFailure(null, null), null)
                : result;
        Map<String, Object> attributes = new LinkedHashMap<>();
        contributeResponseAttributes(exchange, config, safeResult.response(), safeResult.failure(), attributes);
        attempt.emitFinished(safeResult, attributes);
    }

    public boolean isSuccessfulResponse(ShetabResolvedConfig config, String responseCode) {
        String configuredSuccessCode = configuredSuccessCode(config);
        String normalizedCode = clean(responseCode);
        return normalizedCode != null && normalizedCode.equals(configuredSuccessCode);
    }

    public ProviderBusinessOutcome providerOutcome(ShetabResolvedConfig config, String responseCode) {
        return isSuccessfulResponse(config, responseCode)
                ? ProviderBusinessOutcome.success(responseCode)
                : ProviderBusinessOutcome.businessFailure(responseCode, responseCode);
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
        return attributes;
    }

    Map<String, Object> attemptAttributes(String endpoint) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, ShetabTraceAttributes.PROVIDER_ENDPOINT.name(), safeEndpoint(endpoint));
        return attributes;
    }

    private void contributeRequestAttributes(
            Exchange exchange,
            ShetabResolvedConfig config,
            ISOMsg request,
            Map<String, Object> attributes
    ) {
        for (ShetabProviderTraceAttributeContributor contributor : contributors) {
            Map<String, Object> contributed = new LinkedHashMap<>();
            try {
                contributor.contributeRequestAttributes(exchange, config, request, contributed);
                attributes.putAll(normalizeContributedAttributes(contributed));
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
            Map<String, Object> contributed = new LinkedHashMap<>();
            try {
                contributor.contributeResponseAttributes(exchange, config, response, failure, contributed);
                attributes.putAll(normalizeContributedAttributes(contributed));
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

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String configuredSuccessCode(ShetabResolvedConfig config) {
        Object value = config == null || config.providerConfig() == null
                ? null
                : firstPresent(config.providerConfig(),
                "successCode",
                "success-code",
                "successfulResponseCode",
                "successful-response-code",
                "responseSuccessCode",
                "response-success-code");
        String configured = clean(value == null ? null : String.valueOf(value));
        return configured == null ? DEFAULT_SUCCESS_RESPONSE_CODE : configured;
    }

    private Object firstPresent(Map<String, Object> values, String... names) {
        for (String name : names) {
            if (values.containsKey(name)) {
                return values.get(name);
            }
        }
        return null;
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
                log.warn("Ignoring unsafe Shetab trace attribute name={} failureType={}",
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

    private Set<String> registeredAttributeNames(List<ShetabProviderTraceAttributeContributor> contributors) {
        Set<String> names = new LinkedHashSet<>();
        for (var attribute : ShetabTraceAttributes.attributes()) {
            names.add(attribute.name());
        }
        if (contributors != null) {
            for (ShetabProviderTraceAttributeContributor contributor : contributors) {
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
                    log.warn("Ignoring Shetab trace attribute registration failure contributor={} failureType={}",
                            contributor.getClass().getName(), exception.getClass().getSimpleName());
                }
            }
        }
        return Set.copyOf(names);
    }
}
