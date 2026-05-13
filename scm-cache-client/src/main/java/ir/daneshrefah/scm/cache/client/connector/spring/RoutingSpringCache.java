package ir.daneshrefah.scm.cache.client.connector.spring;

import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;

import java.util.Objects;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class RoutingSpringCache implements Cache {

    private final CacheRoute route;
    private final CacheBackend backend;

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
        Object value = backend.get(route, toKey(key));
        return value == null ? null : () -> value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Object value = backend.get(route, toKey(key));
        if (value == null) {
            return null;
        }
        if (type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type: " + type.getName());
        }
        return (T) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        String cacheKey = toKey(key);
        Object value = backend.get(route, cacheKey);
        if (value != null) {
            return (T) value;
        }
        try {
            T loadedValue = valueLoader.call();
            if (loadedValue != null) {
                backend.putIfAbsent(route, cacheKey, loadedValue, route.ttl());
            }
            return loadedValue;
        } catch (Exception exception) {
            throw new ValueRetrievalException(key, valueLoader, exception);
        }
    }

    @Override
    public void put(Object key, Object value) {
        backend.put(route, toKey(key), value, route.ttl());
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String cacheKey = toKey(key);
        Object currentValue = backend.get(route, cacheKey);
        if (currentValue != null) {
            return () -> currentValue;
        }
        backend.putIfAbsent(route, cacheKey, value, route.ttl());
        Object valueAfterPut = backend.get(route, cacheKey);
        if (valueAfterPut == null || Objects.equals(valueAfterPut, value)) {
            return null;
        }
        return () -> valueAfterPut;
    }

    @Override
    public void evict(Object key) {
        backend.remove(route, toKey(key));
    }

    @Override
    public void clear() {
        backend.clear(route);
    }

    private String toKey(Object key) {
        return String.valueOf(key);
    }
}
