package ir.daneshrefah.scm.cache.client.event;

import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimitResult;
import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEvent;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ScmCacheEventSupport {
    private final ObjectProvider<ScmEventPublisher> eventPublisherProvider;
    private final CacheKeyHasher keyHasher;

    public ScmCacheEventSupport(ObjectProvider<ScmEventPublisher> eventPublisherProvider, CacheKeyHasher keyHasher) {
        this.eventPublisherProvider = eventPublisherProvider;
        this.keyHasher = keyHasher == null ? new DefaultCacheKeyHasher() : keyHasher;
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
        Map<String, Object> attributes = baseAttributes(
                route == null ? null : route.cacheName(),
                route == null || route.type() == null ? null : route.type().name().toLowerCase(),
                operation,
                key,
                startedAtNanos
        );
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

    public void lockEvent(ScmCacheEventType type, String lockName, String provider, long startedAtNanos, String result, Throwable error) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.cache.name", "lock");
        put(attributes, "scm.cache.provider", provider);
        put(attributes, "scm.cache.operation", "lock");
        put(attributes, "scm.cache.key_hash", keyHash(lockName));
        put(attributes, "scm.cache.key_type", keyType(lockName));
        put(attributes, "scm.cache.duration_ms", durationMs(startedAtNanos));
        put(attributes, "scm.cache.result", result);
        putError(attributes, error);
        publish(type, attributes);
    }

    public void concurrencyEvent(ScmCacheEventType type, String limitName, String provider, long startedAtNanos, String result, Throwable error) {
        utilityEvent(type, "concurrency_limit", provider, "concurrency", limitName, null, startedAtNanos, result, null, error);
    }

    public void resourceLeaseEvent(
            ScmCacheEventType type,
            String poolName,
            String provider,
            Object leaseKey,
            Duration ttl,
            long startedAtNanos,
            String result,
            Throwable error
    ) {
        utilityEvent(type, poolName, provider, "resource_lease", leaseKey, ttl, startedAtNanos, result, null, error);
    }

    public void queueEvent(ScmCacheEventType type, String queueName, long startedAtNanos, String result, Boolean hit, Throwable error) {
        utilityEvent(type, "queue", "queue", "queue", queueName, null, startedAtNanos, result, hit, error);
    }

    public void cacheError(String cacheName, String provider, String operation, Object key, long startedAtNanos, Throwable error) {
        utilityEvent(ScmCacheEventType.CACHE_ERROR, cacheName, provider, operation, key, null, startedAtNanos, "failure", null, error);
    }

    public String keyHash(Object key) {
        return keyHasher.hash(key);
    }

    private void utilityEvent(
            ScmCacheEventType type,
            String cacheName,
            String provider,
            String operation,
            Object key,
            Duration ttl,
            long startedAtNanos,
            String result,
            Boolean hit,
            Throwable error
    ) {
        Map<String, Object> attributes = baseAttributes(cacheName, provider, operation, key, startedAtNanos);
        put(attributes, "scm.cache.ttl_ms", ttl == null ? null : ttl.toMillis());
        put(attributes, "scm.cache.result", result);
        put(attributes, "scm.cache.hit", hit);
        putError(attributes, error);
        publish(type, attributes);
    }

    private Map<String, Object> baseAttributes(String cacheName, String provider, String operation, Object key, long startedAtNanos) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        put(attributes, "scm.cache.name", cacheName);
        put(attributes, "scm.cache.provider", provider);
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
        put(attributes, "error.message", "Cache operation failed: " + error.getClass().getSimpleName());
    }

    private String keyType(Object key) {
        return keyHasher.type(key);
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
