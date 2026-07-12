package ir.daneshrefah.scm.provider.rest.trace;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEventType;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
@Slf4j
public class RestProviderTraceSupport {
    private static final String OPERATION_SCOPE_PROPERTY = "scm.observation.scope.operation";
    private static final String PROVIDER_NAME = "REST";
    private static final String PROVIDER_TYPE = "http";

    private final ObjectProvider<ScmEventPublisher> eventPublisherProvider;

    public RestProviderTraceSupport(ObjectProvider<ScmEventPublisher> eventPublisherProvider) {
        this.eventPublisherProvider = eventPublisherProvider;
    }

    public ResponseEntity<String> clientSpan(
            Exchange exchange,
            RestProviderResolvedConfig config,
            String operation,
            RestProviderRequestSpec request,
            Supplier<ResponseEntity<String>> action
    ) {
        long startedAt = System.nanoTime();
        ProviderAttempt attempt = safeStartAttempt(exchange, config, operation, request, startedAt);
        try {
            ResponseEntity<String> response = action.get();
            safeFinishAttempt(attempt, config, request, response, null, startedAt);
            return response;
        } catch (RuntimeException exception) {
            safeFinishAttempt(attempt, config, request, null, exception, startedAt);
            throw exception;
        }
    }

    public void enrichLogMdc(Exchange exchange) {
        // Trace context is owned by the host observation layer.
        // Keep this method as a compatibility hook for existing producer code.
    }

    public void customizerSpan(
            Exchange exchange,
            ProviderMessageCustomizerContext context,
            String customizerType,
            String phase,
            Runnable action
    ) {
        action.run();
    }

    public void tokenEvent(
            String eventName,
            RestProviderResolvedConfig providerConfig,
            RestAuthUrlProviderMessageCustomizerConfig authConfig,
            ProviderMessageCustomizerContext context
    ) {
        Map<String, Object> attributes = providerAttributes(providerConfig);
        put(attributes, "scm.provider.service_code", context == null ? null : context.serviceCode());
        put(attributes, "scm.provider.operation_code", context == null ? null : context.operationCode());
        put(attributes, "scm.provider.channel_code", context == null ? null : context.channelCode());
        put(attributes, "scm.provider.result", eventName);
        put(attributes, "scm.provider.auth.profile", authConfig == null || authConfig.cache() == null ? null : authConfig.cache().getAuthProfile());
        publish(ScmProviderEventType.PROVIDER_RESPONSE_RECEIVED, attributes);
    }

    private ProviderAttempt safeStartAttempt(
            Exchange exchange,
            RestProviderResolvedConfig config,
            String operation,
            RestProviderRequestSpec request,
            long startedAt
    ) {
        try {
            return startAttempt(exchange, config, operation, request, startedAt);
        } catch (RuntimeException exception) {
            log.warn("Ignoring REST provider trace start failure eventName={} failureType={}",
                    "provider.request", exception.getClass().getSimpleName());
            return new ProviderAttempt(null, Map.of());
        }
    }

    private void safeFinishAttempt(
            ProviderAttempt attempt,
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            ResponseEntity<String> response,
            RuntimeException exception,
            long startedAt
    ) {
        try {
            finishAttempt(attempt, config, request, response, exception, startedAt);
        } catch (RuntimeException traceException) {
            log.warn("Ignoring REST provider trace finish failure eventName={} failureType={}",
                    "provider.response", traceException.getClass().getSimpleName());
        }
    }

    private ProviderAttempt startAttempt(
            Exchange exchange,
            RestProviderResolvedConfig config,
            String operation,
            RestProviderRequestSpec request,
            long startedAt
    ) {
        publishProviderEvent(
                ScmProviderEventType.PROVIDER_REQUEST_SENT,
                () -> requestAttributes(config, request, null, startedAt)
        );
        ObservationScope scope = activeOperationScope(exchange);
        Map<String, Object> baseAttributes = traceBaseAttributes(config, operation, request);
        Map<String, Object> requestAttributes = new LinkedHashMap<>(baseAttributes);
        put(requestAttributes, "event.outcome", "success");
        boolean requestRecorded = addOperationEvent(scope, "provider.request", requestAttributes);
        return new ProviderAttempt(requestRecorded ? scope : null, baseAttributes);
    }

    private void finishAttempt(
            ProviderAttempt attempt,
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            ResponseEntity<String> response,
            RuntimeException exception,
            long startedAt
    ) {
        if (attempt == null || !attempt.markFinished()) {
            return;
        }
        if (exception == null) {
            publishProviderEvent(
                    ScmProviderEventType.PROVIDER_RESPONSE_RECEIVED,
                    () -> responseAttributes(config, request, response, startedAt)
            );
        } else {
            publishProviderEvent(
                    isTimeout(exception) ? ScmProviderEventType.PROVIDER_TIMEOUT : ScmProviderEventType.PROVIDER_CALL_FAILED,
                    () -> failureAttributes(config, request, exception, startedAt)
            );
        }
        Map<String, Object> attributes = new LinkedHashMap<>(attempt.baseAttributes());
        ProviderBusinessOutcome outcome = providerOutcome(response, exception);
        put(attributes, RestTraceAttributes.PROVIDER_DURATION_MS.name(), durationMs(startedAt));
        put(attributes, RestTraceAttributes.PROVIDER_RESPONSE_CODE.name(), outcome.responseCode());
        put(attributes, "event.outcome", outcome.eventOutcome());
        if (!outcome.success()) {
            put(attributes, RestTraceAttributes.PROVIDER_ERROR_CODE.name(), outcome.safeErrorCode());
            put(attributes, "error.code", outcome.safeErrorCode());
            put(attributes, "error.type", outcome.errorType());
        }
        addOperationEvent(attempt.scope(), "provider.response", attributes);
    }

    private Map<String, Object> requestAttributes(
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            ResponseEntity<String> response,
            long startedAt
    ) {
        Map<String, Object> attributes = providerAttributes(config);
        putRequest(attributes, request);
        putResponse(attributes, response);
        put(attributes, "scm.provider.request_time", Instant.now().toString());
        put(attributes, "scm.provider.duration_ms", durationMs(startedAt));
        put(attributes, "scm.provider.result", "sent");
        return attributes;
    }

    private Map<String, Object> responseAttributes(
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            ResponseEntity<String> response,
            long startedAt
    ) {
        Map<String, Object> attributes = providerAttributes(config);
        putRequest(attributes, request);
        putResponse(attributes, response);
        put(attributes, "scm.provider.response_time", Instant.now().toString());
        put(attributes, "scm.provider.duration_ms", durationMs(startedAt));
        put(attributes, "scm.provider.result", isSuccessful(response) ? "success" : "failure");
        return attributes;
    }

    private Map<String, Object> failureAttributes(
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            RuntimeException exception,
            long startedAt
    ) {
        Map<String, Object> attributes = providerAttributes(config);
        putRequest(attributes, request);
        put(attributes, "scm.provider.duration_ms", durationMs(startedAt));
        put(attributes, "scm.provider.result", isTimeout(exception) ? "timeout" : "failure");
        put(attributes, "error.type", exception == null ? null : exception.getClass().getName());
        put(attributes, "error.code", exception == null ? null : exception.getClass().getSimpleName());
        put(attributes, "error.message", safeMessage(exception));
        return attributes;
    }

    private Map<String, Object> providerAttributes(RestProviderResolvedConfig config) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.provider.code", config == null ? null : config.provider());
        put(attributes, "scm.provider.type", config == null ? null : config.scheme());
        put(attributes, "scm.provider.endpoint", providerUri(config));
        return attributes;
    }

    private Map<String, Object> traceBaseAttributes(
            RestProviderResolvedConfig config,
            String operation,
            RestProviderRequestSpec request
    ) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, RestTraceAttributes.PROVIDER_CODE.name(), config == null ? null : config.provider());
        put(attributes, RestTraceAttributes.PROVIDER_NAME.name(), PROVIDER_NAME);
        put(attributes, RestTraceAttributes.PROVIDER_TYPE.name(), PROVIDER_TYPE);
        put(attributes, RestTraceAttributes.PROVIDER_SCHEME.name(), config == null ? null : config.scheme());
        put(attributes, RestTraceAttributes.PROVIDER_OPERATION.name(), clean(operation));
        put(attributes, RestTraceAttributes.PROVIDER_ENDPOINT.name(), safeEndpoint(request == null ? null : request.uri()));
        put(attributes, "http.method", request == null || request.method() == null ? null : request.method().name());
        return attributes;
    }

    private void putRequest(Map<String, Object> attributes, RestProviderRequestSpec request) {
        URI uri = request == null ? null : request.uri();
        put(attributes, "http.method", request == null || request.method() == null ? null : request.method().name());
        put(attributes, "url.path", uri == null ? null : uri.getPath());
        put(attributes, "scm.provider.address", uri == null ? null : uri.getHost());
    }

    private void putResponse(Map<String, Object> attributes, ResponseEntity<String> response) {
        Integer status = response == null ? null : response.getStatusCode().value();
        put(attributes, "http.status_code", status);
        put(attributes, "http.response.status_code", status);
        put(attributes, "scm.provider.response_code", status);
    }

    private void publish(ScmProviderEventType type, Map<String, ?> attributes) {
        ScmEventPublisher eventPublisher = eventPublisherProvider.getIfAvailable();
        if (eventPublisher == null || type == null) {
            return;
        }
        try {
            eventPublisher.publish(ScmProviderEvent.of(type, attributes));
        } catch (RuntimeException exception) {
            log.warn("event=SCM_PROVIDER_EVENT_PUBLISH_FAILED outcome=ignored providerEventType={} failureType={}",
                    type.code(),
                    exception.getClass().getSimpleName());
        }
    }

    private void publishProviderEvent(ScmProviderEventType type, Supplier<Map<String, ?>> attributesSupplier) {
        try {
            publish(type, attributesSupplier == null ? Map.of() : attributesSupplier.get());
        } catch (RuntimeException exception) {
            log.warn("event=SCM_PROVIDER_EVENT_BUILD_FAILED outcome=ignored providerEventType={} failureType={}",
                    type == null ? null : type.code(),
                    exception.getClass().getSimpleName());
        }
    }

    private ObservationScope activeOperationScope(Exchange exchange) {
        try {
            return exchange == null ? null : exchange.getProperty(OPERATION_SCOPE_PROPERTY, ObservationScope.class);
        } catch (RuntimeException exception) {
            log.warn("Ignoring REST provider trace scope lookup failure eventName={} failureType={}",
                    "provider", exception.getClass().getSimpleName());
            return null;
        }
    }

    private boolean addOperationEvent(ObservationScope scope, String name, Map<String, Object> attributes) {
        if (scope == null) {
            return false;
        }
        try {
            scope.event(name, attributes);
            return true;
        } catch (RuntimeException exception) {
            log.warn("Ignoring REST provider trace event failure eventName={} failureType={}",
                    name, exception.getClass().getSimpleName());
            return false;
        }
    }

    private String providerUri(RestProviderResolvedConfig config) {
        if (config == null) {
            return null;
        }
        return value(config.scheme()) + ":" + value(config.provider());
    }

    private boolean isSuccessful(ResponseEntity<String> response) {
        return response != null && response.getStatusCode().is2xxSuccessful();
    }

    private ProviderBusinessOutcome providerOutcome(ResponseEntity<String> response, RuntimeException exception) {
        String responseCode = responseCode(response);
        if (exception != null) {
            return ProviderBusinessOutcome.technicalFailure(responseCode, exception);
        }
        return isSuccessful(response)
                ? ProviderBusinessOutcome.success(responseCode)
                : ProviderBusinessOutcome.businessFailure(responseCode, responseCode);
    }

    private String responseCode(ResponseEntity<String> response) {
        Integer status = response == null ? null : response.getStatusCode().value();
        return status == null ? null : String.valueOf(status);
    }

    private long durationMs(long startedAt) {
        return Duration.ofNanos(Math.max(0L, System.nanoTime() - startedAt)).toMillis();
    }

    private boolean isTimeout(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String className = current.getClass().getName();
            if (className.contains("Timeout")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String safeEndpoint(URI uri) {
        if (uri == null || uri.getRawUserInfo() != null) {
            return null;
        }
        String host = clean(uri.getHost());
        if (host == null) {
            return null;
        }
        int port = uri.getPort();
        return port > 0 ? host + ":" + port : host;
    }

    private void put(Map<String, Object> attributes, String key, Object value) {
        if (key != null && !key.isBlank() && value != null) {
            attributes.put(key, value);
        }
    }

    private String safeMessage(Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            return null;
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static final class ProviderAttempt {
        private final ObservationScope scope;
        private final Map<String, Object> baseAttributes;
        private final AtomicBoolean finished = new AtomicBoolean();

        private ProviderAttempt(ObservationScope scope, Map<String, Object> baseAttributes) {
            this.scope = scope;
            this.baseAttributes = baseAttributes == null ? Map.of() : Map.copyOf(baseAttributes);
        }

        private ObservationScope scope() {
            return scope;
        }

        private Map<String, Object> baseAttributes() {
            return baseAttributes;
        }

        private boolean markFinished() {
            return finished.compareAndSet(false, true);
        }
    }
}
