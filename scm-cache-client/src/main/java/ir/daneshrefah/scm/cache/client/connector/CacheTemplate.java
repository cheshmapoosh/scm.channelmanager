package ir.daneshrefah.scm.cache.client.connector;

import com.hazelcast.flakeidgen.FlakeIdGenerator;

import java.util.Map;

public interface CacheTemplate {
    Object getFromCache(String mapName, String key);

    void putInCache(String mapName, String key, Object value);

    void putInCache(String mapName, String key, Object value, long timeToLiveMinutes);

    void putInCacheIfAbsent(String mapName, String key, Object value);

    void putInCacheIfAbsent(String mapName, String key, Object value, long timeToLiveMinutes);

    Object removeFromCache(String mapName, String key);

    Map createCacheIfNull(String mapName);

    FlakeIdGenerator createIdGeneratorIfNull(String name);
}
