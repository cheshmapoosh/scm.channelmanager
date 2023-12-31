package ir.daneshrefah.scm.cache.client.connector;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CacheTemplateImpl implements CacheTemplate {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public Object getFromCache(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).get(key);
    }

    @Override
    public void putInCache(String mapName, String key, Object value) {
        hazelcastInstance.getMap(mapName).put(key, value);
    }

    @Override
    public void putInCache(String mapName, String key, Object value, long timeToLiveMinutes) {
        hazelcastInstance.getMap(mapName).put(key, value, timeToLiveMinutes, TimeUnit.MINUTES);
    }

    @Override
    public Object removeFromCache(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).remove(key);
    }

    @Override
    public Map createCacheIfNull(String mapName) {
        return Collections.unmodifiableMap(hazelcastInstance.getMap(mapName));
    }

    @Override
    public FlakeIdGenerator createIdGeneratorIfNull(String name) {
        return hazelcastInstance.getFlakeIdGenerator(name);
    }
}
