package ir.daneshrefah.scm.cache.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class cacheTemplateImpl implements CacheTemplate {

    private HazelcastInstance hazelcastInstance;

    public cacheTemplateImpl(String serverHost) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNetworkConfig(new ClientNetworkConfig()
                .addAddress(serverHost));
        this.hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
    }

    @Override
    public Object getFromCache(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).get(key);
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
}
