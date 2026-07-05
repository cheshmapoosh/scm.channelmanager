package ir.daneshrefah.scm.cache.client.connector.spring;

import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;
import ir.daneshrefah.scm.cache.client.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class RoutingSpringCache implements TtlAwareCache {

    private final CacheRoute route;
    private final CacheBackend backend;
    private final ScmCacheEventSupport cacheEventSupport;
    private final boolean publishStartedEvents;

    @Override
    public String getName() {
        return route.cacheName();
    }

    @Override
    public Object getNativeCache() {
        return backend;
    }

    @Override
    public ValueWrapper get(Object key) {
        long startedAt = System.nanoTime();
        publishGetStarted(key, startedAt);
        try {
            Object value = backend.get(route, toKey(key));
            publishGetResult(key, startedAt, value);
            return value == null ? null : () -> value;
        } catch (RuntimeException exception) {
            publishError("get", key, startedAt, exception);
            throw exception;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        long startedAt = System.nanoTime();
        publishGetStarted(key, startedAt);
        try {
            Object value = backend.get(route, toKey(key));
            publishGetResult(key, startedAt, value);
            if (value == null) {
                return null;
            }
            if (type != null && !type.isInstance(value)) {
                throw new IllegalStateException("Cached value is not of required type: " + type.getName());
            }
            return (T) value;
        } catch (RuntimeException exception) {
            publishError("get", key, startedAt, exception);
            throw exception;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        long startedAt = System.nanoTime();
        String cacheKey = toKey(key);
        publishGetStarted(key, startedAt);
        try {
            Object value = backend.get(route, cacheKey);
            if (value != null) {
                publishGetResult(key, startedAt, value);
                return (T) value;
            }
            publishGetResult(key, startedAt, null);
            T loadedValue = loadValue(key, valueLoader, startedAt);
            if (loadedValue != null) {
                backend.putIfAbsent(route, cacheKey, loadedValue, route.ttl());
                cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_PUT, route, "put_if_absent", key, route.ttl(), startedAt, "success", null, null);
            }
            return loadedValue;
        } catch (ValueRetrievalException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            publishError("get", key, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public void put(Object key, Object value) {
        put(key, value, route.ttl());
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        long startedAt = System.nanoTime();
        try {
            backend.put(route, toKey(key), value, ttl);
            cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_PUT, route, "put", key, ttl, startedAt, "success", null, null);
        } catch (RuntimeException exception) {
            publishError("put", key, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return putIfAbsent(key, value, route.ttl());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value, Duration ttl) {
        long startedAt = System.nanoTime();
        String cacheKey = toKey(key);
        try {
            Object currentValue = backend.get(route, cacheKey);
            if (currentValue != null) {
                cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_HIT, route, "put_if_absent", key, ttl, startedAt, "exists", true, null);
                return () -> currentValue;
            }
            backend.putIfAbsent(route, cacheKey, value, ttl);
            cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_PUT, route, "put_if_absent", key, ttl, startedAt, "success", null, null);
            Object valueAfterPut = backend.get(route, cacheKey);
            if (valueAfterPut == null || Objects.equals(valueAfterPut, value)) {
                return null;
            }
            return () -> valueAfterPut;
        } catch (RuntimeException exception) {
            publishError("put_if_absent", key, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public void evict(Object key) {
        long startedAt = System.nanoTime();
        try {
            backend.remove(route, toKey(key));
            cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_EVICT, route, "evict", key, null, startedAt, "success", null, null);
        } catch (RuntimeException exception) {
            publishError("evict", key, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public void clear() {
        long startedAt = System.nanoTime();
        try {
            backend.clear(route);
            cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_CLEAR, route, "clear", null, null, startedAt, "success", null, null);
        } catch (RuntimeException exception) {
            publishError("clear", null, startedAt, exception);
            throw exception;
        }
    }

    private String toKey(Object key) {
        return String.valueOf(key);
    }

    private <T> T loadValue(Object key, Callable<T> valueLoader, long startedAt) {
        try {
            return valueLoader.call();
        } catch (Exception exception) {
            publishError("get", key, startedAt, exception);
            throw new ValueRetrievalException(key, valueLoader, exception);
        }
    }

    private void publishGetStarted(Object key, long startedAt) {
        if (publishStartedEvents) {
            cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_GET, route, "get", key, null, startedAt, "started", null, null);
        }
    }

    private void publishGetResult(Object key, long startedAt, Object value) {
        boolean hit = value != null;
        cacheEventSupport.cacheEvent(
                hit ? ScmCacheEventType.CACHE_HIT : ScmCacheEventType.CACHE_MISS,
                route,
                "get",
                key,
                null,
                startedAt,
                hit ? "hit" : "miss",
                hit,
                null
        );
    }

    private void publishError(String operation, Object key, long startedAt, Throwable exception) {
        cacheEventSupport.cacheEvent(ScmCacheEventType.CACHE_ERROR, route, operation, key, null, startedAt, "failure", null, exception);
    }
}
