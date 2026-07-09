package ir.daneshrefah.scm.cache.starter.connector.spring;

import org.springframework.cache.Cache;

import java.time.Duration;

public interface TtlAwareCache extends Cache {

    void put(Object key, Object value, Duration ttl);

    ValueWrapper putIfAbsent(Object key, Object value, Duration ttl);
}
