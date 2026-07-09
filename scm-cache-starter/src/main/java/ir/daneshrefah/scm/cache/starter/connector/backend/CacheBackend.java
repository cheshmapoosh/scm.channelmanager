package ir.daneshrefah.scm.cache.starter.connector.backend;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
import ir.daneshrefah.scm.cache.starter.connector.routing.CacheRoute;

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
