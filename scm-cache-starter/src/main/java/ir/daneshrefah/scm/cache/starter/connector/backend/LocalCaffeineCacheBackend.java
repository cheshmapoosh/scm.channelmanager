package ir.daneshrefah.scm.cache.starter.connector.backend;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
import ir.daneshrefah.scm.cache.starter.connector.routing.CacheRoute;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LocalCaffeineCacheBackend implements CacheBackend {

    private static final long NO_EXPIRY = Long.MAX_VALUE;
    private final Map<String, Cache<String, CacheValue>> caches = new ConcurrentHashMap<>();

    @Override
    public CacheType type() {
        return CacheType.LOCAL;
    }

    @Override
    public Object get(CacheRoute route, String key) {
        CacheValue cacheValue = resolveCache(route).getIfPresent(key);
        return cacheValue == null ? null : cacheValue.value();
    }

    @Override
    public void put(CacheRoute route, String key, Object value, Duration ttl) {
        resolveCache(route).put(key, new CacheValue(value, ttlToNanos(ttl)));
    }

    @Override
    public void putIfAbsent(CacheRoute route, String key, Object value, Duration ttl) {
        resolveCache(route)
                .asMap()
                .putIfAbsent(key, new CacheValue(value, ttlToNanos(ttl)));
    }

    @Override
    public Object remove(CacheRoute route, String key) {
        Cache<String, CacheValue> cache = resolveCache(route);
        CacheValue oldValue = cache.getIfPresent(key);
        cache.invalidate(key);
        return oldValue == null ? null : oldValue.value();
    }

    @Override
    public Map<String, Object> snapshot(CacheRoute route) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        resolveCache(route).asMap().forEach((key, value) -> snapshot.put(key, value.value()));
        return Collections.unmodifiableMap(snapshot);
    }

    @Override
    public void clear(CacheRoute route) {
        resolveCache(route).invalidateAll();
    }

    private Cache<String, CacheValue> resolveCache(CacheRoute route) {
        return caches.computeIfAbsent(route.cacheName(), cacheName -> createCache(route));
    }

    private Cache<String, CacheValue> createCache(CacheRoute route) {
        long maximumSize = route.maximumSize() > 0 ? route.maximumSize() : 10_000L;
        log.info("Creating local caffeine cache: cache='{}', maxSize={}", route.cacheName(), maximumSize);
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfter(new Expiry<String, CacheValue>() {
                    @Override
                    public long expireAfterCreate(String key, CacheValue value, long currentTime) {
                        return value.ttlNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheValue value, long currentTime, long currentDuration) {
                        return value.ttlNanos();
                    }

                    @Override
                    public long expireAfterRead(String key, CacheValue value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    private long ttlToNanos(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return NO_EXPIRY;
        }
        try {
            long nanos = ttl.toNanos();
            return nanos > 0 ? nanos : NO_EXPIRY;
        } catch (ArithmeticException ex) {
            return NO_EXPIRY;
        }
    }

    private record CacheValue(Object value, long ttlNanos) {
    }
}
