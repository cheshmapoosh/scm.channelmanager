package ir.daneshrefah.scm.cache.client;

import com.hazelcast.flakeidgen.FlakeIdGenerator;

import java.util.Map;

public interface CacheTemplate {
    Object getFromCache(String mapName, String key);

    void putInCache(String mapName, String key, Object value);

    void putInCache(String mapName, String key, Object value, int timeToLiveSeconds);

    Object removeFromCache(String mapName, String key);

    Map createCacheIfNull(String mapName);

    FlakeIdGenerator createIdGeneratorIfNull(String name);
}
