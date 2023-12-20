package ir.daneshrefah.scm.cache.service;

import com.hazelcast.flakeidgen.FlakeIdGenerator;

import java.util.List;
import java.util.Map;

public interface HazelCastService {

    List<String> getMapList();

    Object getMapData(String map);

    Object getFromCache(String mapName, String key);

    Object putInCache(String mapName, String key, Object value);

    Object putInCache(String mapName, String key, Object value, int timeToLiveSeconds);
    Object putInCache(String mapName, String key, Object value, int timeToLiveSeconds,int maxIdle);

    Object removeFromCache(String mapName, String key);

    Map<?,?> createCacheIfNull(String mapName);

    FlakeIdGenerator createIdGeneratorIfNull(String name);

}
