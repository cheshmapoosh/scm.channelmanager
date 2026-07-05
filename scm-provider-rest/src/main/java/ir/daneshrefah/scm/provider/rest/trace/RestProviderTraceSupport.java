package ir.daneshrefah.scm.provider.rest.trace;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEventType;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
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
import java.util.function.Supplier;

@Component
@Slf4j
public class RestProviderTraceSupport {
    private final ObjectProvider<ScmEventPublisher> eventPublisherProvider;

    public RestProviderTraceSupport(ObjectProvider<ScmEventPublisher> eventPublisherProvider) {
        this.eventPublisherProvider = eventPublisherProvider;
    }

    public ResponseEntity<String> clientSpan(
            Exchange exchange,
            RestProviderResolvedConfig config,
            RestProviderRequestSpec request,
            Supplier<ResponseEntity<String>> action
    ) {
        long startedAt = System.nanoTime();
        publish(ScmProviderEventType.PROVIDER_REQUEST_SENT, requestAttributes(config, request, null, startedAt));
        try {
            ResponseEntity<String> response = action.get();
            publish(ScmProviderEventType.PROVIDER_RESPONSE_RECEIVED, responseAttributes(config, request, response, startedAt));
            return response;
        } catch (RuntimeException exception) {
            publish(isTimeout(exception) ? ScmProviderEventType.PROVIDER_TIMEOUT : ScmProviderEventType.PROVIDER_CALL_FAILED,
                    failureAttributes(config, request, exception, startedAt));
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
            log.warn("event=SCM_PROVIDER_EVENT_PUBLISH_FAILED outcome=ignored providerEventType={} failureType={} failureMessage={}",
                    type.code(),
                    exception.getClass().getSimpleName(),
                    safeMessage(exception));
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
}
