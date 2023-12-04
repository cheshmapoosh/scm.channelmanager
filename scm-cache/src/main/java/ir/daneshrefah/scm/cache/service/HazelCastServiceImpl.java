package ir.daneshrefah.scm.cache.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HazelCastServiceImpl implements HazelCastService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public Object getFromCache(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).getEntryView(key);
    }

    @Override
    public void putInCache(String mapName, String key, Object value) {
        hazelcastInstance.getMap(mapName).put(key, value);
    }

    @Override
    public void putInCache(String mapName, String key, Object value, int timeToLiveSeconds) {
        hazelcastInstance.getMap(mapName).put(key, value, timeToLiveSeconds, TimeUnit.SECONDS);
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

    @Override
    public List<String> getMapList() {
        return hazelcastInstance
                .getDistributedObjects()
                .stream()
                .filter(distributedObject -> distributedObject instanceof IMap<?, ?>)
                .map(distributedObject -> hazelcastInstance.getMap(distributedObject.getName()))
                .map(IMap::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Object getMapData(String map) {
        return hazelcastInstance.getMap(map);
    }
}
