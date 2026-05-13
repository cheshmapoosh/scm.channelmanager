package ir.daneshrefah.scm.cache.client.connector.backend;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.cache.client.config.properties.CacheType;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class HazelcastCacheBackend implements CacheBackend {

    private final HazelcastInstance hazelcastInstance;
    private final CacheType type;

    @Override
    public CacheType type() {
        return type;
    }

    @Override
    public Object get(CacheRoute route, String key) {
        return map(route).get(key);
    }

    @Override
    public void put(CacheRoute route, String key, Object value, Duration ttl) {
        if (hasTtl(ttl)) {
            map(route).put(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
            return;
        }
        map(route).put(key, value);
    }

    @Override
    public void putIfAbsent(CacheRoute route, String key, Object value, Duration ttl) {
        if (hasTtl(ttl)) {
            map(route).putIfAbsent(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
            return;
        }
        map(route).putIfAbsent(key, value);
    }

    @Override
    public Object remove(CacheRoute route, String key) {
        return map(route).remove(key);
    }

    @Override
    public Map<String, Object> snapshot(CacheRoute route) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(map(route)));
    }

    @Override
    public void clear(CacheRoute route) {
        map(route).clear();
    }

    private IMap<String, Object> map(CacheRoute route) {
        return hazelcastInstance.getMap(route.targetName());
    }

    private boolean hasTtl(Duration ttl) {
        return ttl != null && !ttl.isZero() && !ttl.isNegative();
    }
}
