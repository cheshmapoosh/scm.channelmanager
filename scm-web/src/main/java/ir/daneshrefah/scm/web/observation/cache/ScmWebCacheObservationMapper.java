package ir.daneshrefah.scm.web.observation.cache;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ScmWebCacheObservationMapper {
    private static final String CACHE_MISS = "cache.miss";
    private static final String CACHE_ERROR = "cache.error";
    private static final String CACHE_LOCK_FAILED = "cache.lock.failed";
    private static final String CACHE_LOCK_EXECUTION_FAILED = "cache.lock.execution.failed";
    private static final String CACHE_RATE_LIMIT_REJECTED = "cache.rate_limit.rejected";
    private static final String CACHE_CONCURRENCY_REJECTED = "cache.concurrency.rejected";
    private static final String CACHE_CONCURRENCY_ERROR = "cache.concurrency.error";
    private static final String CACHE_RESOURCE_LEASE_REJECTED = "cache.resource_lease.rejected";
    private static final String CACHE_RESOURCE_LEASE_ERROR = "cache.resource_lease.error";
    private static final String CACHE_QUEUE_ERROR = "cache.queue.error";
    private static final Set<String> TRACE_KEYS = Set.of(
            "scm.cache.name",
            "scm.cache.provider",
            "scm.cache.operation",
            "scm.cache.key_hash",
            "scm.cache.key_type",
            "scm.cache.hit",
            "scm.cache.ttl_ms",
            "scm.cache.duration_ms",
            "scm.cache.result",
            "scm.event.type",
            "scm.event.source",
            "scm.event.occurred_at",
            "error.type",
            "error.code",
            "error.message"
    );
    private static final List<String> DENIED_TRACE_KEY_PARTS = List.of(
            "key",
            "value",
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
            "mac"
    );

    public ScmWebObservationEvent map(ScmCacheEvent event) {
        String action = event == null ? "cache.event" : event.eventType();
        Map<String, Object> attributes = attributes(event);
        return new ScmWebObservationEvent(
                "cache",
                action,
                outcome(action),
                "SCM cache event",
                attributes,
                cacheTraceAttributes(attributes)
        );
    }

    private Map<String, Object> attributes(ScmCacheEvent event) {
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

    private String outcome(String action) {
        if (CACHE_MISS.equals(action)) {
            return "miss";
        }
        if (CACHE_ERROR.equals(action)
                || CACHE_LOCK_FAILED.equals(action)
                || CACHE_LOCK_EXECUTION_FAILED.equals(action)
                || CACHE_RATE_LIMIT_REJECTED.equals(action)
                || CACHE_CONCURRENCY_REJECTED.equals(action)
                || CACHE_CONCURRENCY_ERROR.equals(action)
                || CACHE_RESOURCE_LEASE_REJECTED.equals(action)
                || CACHE_RESOURCE_LEASE_ERROR.equals(action)
                || CACHE_QUEUE_ERROR.equals(action)) {
            return "failure";
        }
        return "success";
    }

    private Map<String, Object> cacheTraceAttributes(Map<String, Object> attributes) {
        Map<String, Object> traceAttributes = new LinkedHashMap<>();
        attributes.forEach((key, value) -> {
            if (isAllowedTraceKey(key)) {
                traceAttributes.put(key, value);
            }
        });
        return traceAttributes;
    }

    private boolean isAllowedTraceKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT).trim();
        if (!TRACE_KEYS.contains(normalized)) {
            return false;
        }
        if ("scm.cache.key_hash".equals(normalized) || "scm.cache.key_type".equals(normalized)) {
            return true;
        }
        for (String denied : DENIED_TRACE_KEY_PARTS) {
            if (normalized.contains(denied)) {
                return false;
            }
        }
        return true;
    }
}
