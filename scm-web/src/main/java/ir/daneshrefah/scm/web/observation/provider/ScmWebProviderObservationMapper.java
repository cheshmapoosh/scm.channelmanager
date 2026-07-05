package ir.daneshrefah.scm.web.observation.provider;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ScmWebProviderObservationMapper {
    private static final String PROVIDER_CALL_FAILED = "provider.call.failed";
    private static final String PROVIDER_TIMEOUT = "provider.timeout";
    private static final Set<String> TRACE_METADATA_KEYS = Set.of(
            "scm.provider.code",
            "scm.provider.type",
            "scm.provider.address",
            "scm.provider.endpoint",
            "scm.provider.request_time",
            "scm.provider.response_time",
            "scm.provider.duration_ms",
            "scm.provider.result",
            "scm.provider.response_code",
            "http.method",
            "http.status_code",
            "http.status_code.value",
            "http.response.status_code",
            "url.path",
            "endpoint.path",
            "duration",
            "duration.ms",
            "duration_ms",
            "scm.event.type",
            "scm.event.source",
            "scm.event.occurred_at",
            "error.type",
            "error.code",
            "error.message"
    );
    private static final List<String> DENIED_TRACE_KEY_FRAGMENTS = List.of(
            "body",
            "payload",
            "raw",
            "iso",
            "authorization",
            "cookie",
            "token",
            "password",
            "secret",
            "pin",
            "cvv",
            "cvv2",
            "pan",
            "card",
            "mac",
            "key"
    );

    public ScmWebObservationEvent map(ScmProviderEvent event) {
        String action = event == null ? "provider.event" : event.eventType();
        Map<String, Object> logAttributes = attributes(event);
        return new ScmWebObservationEvent(
                "provider",
                action,
                outcome(action),
                "SCM provider event",
                logAttributes,
                providerTraceAttributes(logAttributes)
        );
    }

    private Map<String, Object> attributes(ScmProviderEvent event) {
        Map<String, Object> attributes = event == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(event.attributes()));
        if (event != null) {
            attributes.put("scm.event.type", event.eventType());
            attributes.put("scm.event.source", event.metadata().source());
            attributes.put("scm.event.occurred_at", event.occurredAt().toString());
        }
        return attributes;
    }

    private Map<String, Object> providerTraceAttributes(Map<String, Object> attributes) {
        Map<String, Object> traceAttributes = new LinkedHashMap<>();
        attributes.forEach((key, value) -> {
            if (isProviderTraceMetadata(key)) {
                traceAttributes.put(key, value);
            }
        });
        return traceAttributes;
    }

    private boolean isProviderTraceMetadata(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        for (String denied : DENIED_TRACE_KEY_FRAGMENTS) {
            if (normalized.contains(denied)) {
                return false;
            }
        }
        return TRACE_METADATA_KEYS.contains(normalized);
    }

    private String outcome(String action) {
        if (PROVIDER_TIMEOUT.equals(action)) {
            return "timeout";
        }
        if (PROVIDER_CALL_FAILED.equals(action)) {
            return "failure";
        }
        return "success";
    }
}
