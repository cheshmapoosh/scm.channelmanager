package ir.daneshrefah.scm.web.observation.provider;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.provider.ScmProviderEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class ScmWebProviderObservationMapper {
    private static final String PROVIDER_CALL_FAILED = "provider.call.failed";
    private static final String PROVIDER_TIMEOUT = "provider.timeout";

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
        return normalized.contains("provider")
                || normalized.equals("scm.event.type")
                || normalized.equals("http.method")
                || normalized.equals("method")
                || normalized.equals("url.path")
                || normalized.equals("endpoint.path")
                || normalized.equals("http.status.code")
                || normalized.equals("http.status_code")
                || normalized.equals("http.response.status_code")
                || normalized.endsWith(".duration.ms")
                || normalized.endsWith(".duration_ms")
                || normalized.equals("duration")
                || normalized.equals("duration.ms")
                || normalized.equals("duration_ms");
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
