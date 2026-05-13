package ir.daneshrefah.scm.cache.client.connector.backend;

import ir.daneshrefah.scm.cache.client.config.properties.CacheType;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;

import java.time.Duration;
import java.util.Map;

public interface CacheBackend {

    CacheType type();

    Object get(CacheRoute route, String key);

    void put(CacheRoute route, String key, Object value, Duration ttl);

    void putIfAbsent(CacheRoute route, String key, Object value, Duration ttl);

    Object remove(CacheRoute route, String key);

    Map<String, Object> snapshot(CacheRoute route);

    void clear(CacheRoute route);
}
