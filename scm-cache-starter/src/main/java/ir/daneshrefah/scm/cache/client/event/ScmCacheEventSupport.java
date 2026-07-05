package ir.daneshrefah.scm.cache.client.event;

import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEvent;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ScmCacheEventSupport {
    private final ObjectProvider<ScmEventPublisher> eventPublisherProvider;

    public ScmCacheEventSupport(ObjectProvider<ScmEventPublisher> eventPublisherProvider) {
        this.eventPublisherProvider = eventPublisherProvider;
    }

    public void cacheEvent(
            ScmCacheEventType type,
            CacheRoute route,
            String operation,
            Object key,
            Duration ttl,
            long startedAtNanos,
            String result,
            Boolean hit,
            Throwable error
    ) {
        Map<String, Object> attributes = baseAttributes(route, operation, key, startedAtNanos);
        put(attributes, "scm.cache.ttl_ms", ttl == null ? null : ttl.toMillis());
        put(attributes, "scm.cache.result", result);
        put(attributes, "scm.cache.hit", hit);
        putError(attributes, error);
        publish(type, attributes);
    }

    public void rateLimitEvent(RateLimitResult result, long startedAtNanos) {
        if (result == null) {
            return;
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.cache.name", result.bucketName());
        put(attributes, "scm.cache.provider", "rate_limit");
        put(attributes, "scm.cache.operation", "rate_limit");
        put(attributes, "scm.cache.key_hash", keyHash(result.key()));
        put(attributes, "scm.cache.key_type", keyType(result.key()));
        put(attributes, "scm.cache.duration_ms", durationMs(startedAtNanos));
        put(attributes, "scm.cache.result", result.allowed() ? "consumed" : "rejected");
        publish(result.allowed()
                ? ScmCacheEventType.CACHE_RATE_LIMIT_CONSUMED
                : ScmCacheEventType.CACHE_RATE_LIMIT_REJECTED, attributes);
    }

    public void lockEvent(ScmCacheEventType type, String lockName, long startedAtNanos, String result, Throwable error) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.cache.name", lockName);
        put(attributes, "scm.cache.provider", "lock");
        put(attributes, "scm.cache.operation", "lock");
        put(attributes, "scm.cache.key_hash", keyHash(lockName));
        put(attributes, "scm.cache.key_type", keyType(lockName));
        put(attributes, "scm.cache.duration_ms", durationMs(startedAtNanos));
        put(attributes, "scm.cache.result", result);
        putError(attributes, error);
        publish(type, attributes);
    }

    public void cacheError(String cacheName, String provider, String operation, Object key, long startedAtNanos, Throwable error) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.cache.name", cacheName);
        put(attributes, "scm.cache.provider", provider);
        put(attributes, "scm.cache.operation", operation);
        put(attributes, "scm.cache.key_hash", keyHash(key));
        put(attributes, "scm.cache.key_type", keyType(key));
        put(attributes, "scm.cache.duration_ms", durationMs(startedAtNanos));
        put(attributes, "scm.cache.result", "failure");
        putError(attributes, error);
        publish(ScmCacheEventType.CACHE_ERROR, attributes);
    }

    public String keyHash(Object key) {
        if (key == null) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(key).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ignored) {
            return Integer.toHexString(String.valueOf(key).hashCode());
        }
    }

    private Map<String, Object> baseAttributes(CacheRoute route, String operation, Object key, long startedAtNanos) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.cache.name", route == null ? null : route.cacheName());
        put(attributes, "scm.cache.provider", route == null || route.type() == null ? null : route.type().name().toLowerCase());
        put(attributes, "scm.cache.operation", operation);
        put(attributes, "scm.cache.key_hash", keyHash(key));
        put(attributes, "scm.cache.key_type", keyType(key));
        put(attributes, "scm.cache.duration_ms", durationMs(startedAtNanos));
        return attributes;
    }

    private void publish(ScmCacheEventType type, Map<String, ?> attributes) {
        ScmEventPublisher eventPublisher = eventPublisherProvider == null ? null : eventPublisherProvider.getIfAvailable();
        if (eventPublisher == null || type == null) {
            return;
        }
        try {
            eventPublisher.publish(ScmCacheEvent.of(type, attributes));
        } catch (RuntimeException exception) {
            log.warn("event=SCM_CACHE_EVENT_PUBLISH_FAILED outcome=ignored cacheEventType={} failureType={} failureMessage={}",
                    type.code(),
                    exception.getClass().getSimpleName(),
                    ScmSafeEventAttributes.sanitizeMessage(exception.getMessage()));
        }
    }

    private void putError(Map<String, Object> attributes, Throwable error) {
        if (error == null) {
            return;
        }
        put(attributes, "error.type", error.getClass().getName());
        put(attributes, "error.code", error.getClass().getSimpleName());
        put(attributes, "error.message", ScmSafeEventAttributes.sanitizeMessage(error.getMessage()));
    }

    private String keyType(Object key) {
        return key == null ? null : key.getClass().getSimpleName();
    }

    private long durationMs(long startedAtNanos) {
        return Duration.ofNanos(Math.max(0L, System.nanoTime() - startedAtNanos)).toMillis();
    }

    private void put(Map<String, Object> attributes, String key, Object value) {
        if (key != null && !key.isBlank() && value != null) {
            attributes.put(key, value);
        }
    }
}
