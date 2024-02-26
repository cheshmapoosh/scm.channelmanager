package ir.daneshrefah.scm.cache.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.cache.domain.dto.UserAuthenticationTO;
import ir.daneshrefah.scm.cache.mapper.UserAuthenticationMapper;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HazelCastServiceImpl implements HazelCastService {

    private static final String SESSION_CACHE_MAP = "sessionManagerCache";
    private final HazelcastInstance hazelcastInstance;
    private final UserAuthenticationMapper userAuthenticationMapper;

    @Override
    public Object getFromCache(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).get(key);
    }

    @Override
    public Object getEntryView(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).getEntryView(key);
    }

    @Override
    public Object putInCache(String mapName, String key, Object value) {
        hazelcastInstance.getMap(mapName).put(key, value);
        return getFromCache(mapName, key);
    }

    @Override
    public Object putInCache(String mapName, String key, Object value, int timeToLiveSeconds) {
        hazelcastInstance.getMap(mapName).put(key, value, timeToLiveSeconds, TimeUnit.SECONDS);
        return getFromCache(mapName, key);
    }

    @Override
    public Object putInCache(String mapName, String key, Object value, int timeToLiveSeconds, int maxIdle) {
        hazelcastInstance.getMap(mapName).put(key, value, timeToLiveSeconds, TimeUnit.SECONDS, maxIdle, TimeUnit.SECONDS);
        return getFromCache(mapName, key);
    }

    @Override
    public Object removeFromCache(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).remove(key);
    }

    @Override
    public Map<?, ?> createCacheIfNull(String mapName) {
        IMap<Object, Object> map = hazelcastInstance.getMap(mapName);
        hazelcastInstance.getConfig().getMapConfig(mapName).setPerEntryStatsEnabled(true);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public FlakeIdGenerator createIdGeneratorIfNull(String name) {
        return hazelcastInstance.getFlakeIdGenerator(name);
    }

    @Override
    public boolean exists(String mapName, String key) {
        return hazelcastInstance.getMap(mapName).containsKey(key);
    }

    @Override
    public UserAuthenticationTO putSession(UserAuthenticationTO userAuthenticationTo) {
        UserAuthentication userAuthentication = userAuthenticationMapper.mapToUserAuthentication(userAuthenticationTo);
        putInCache(SESSION_CACHE_MAP,
                userAuthenticationMapper.generateKey(userAuthenticationTo),
                userAuthentication,
                userAuthenticationTo.getTtl(),
                (int) userAuthentication.getDetails().getMaxIdle().toSeconds());
        return userAuthenticationTo;
    }

    @Override
    public UserAuthenticationTO getSession(String nickname, String terminalCode) {
        UserAuthentication fromCache = (UserAuthentication) getFromCache(SESSION_CACHE_MAP, userAuthenticationMapper.generateKey(nickname,terminalCode));
        if (Objects.nonNull(fromCache)) {
            return userAuthenticationMapper.mapUserAuthenticationTO(fromCache);
        }
        return null;
    }

    @Override
    public UserAuthenticationTO removeSession(String nickname, String terminalCode) {
        return userAuthenticationMapper.mapUserAuthenticationTO((UserAuthentication) removeFromCache(SESSION_CACHE_MAP, userAuthenticationMapper.generateKey(nickname,terminalCode)));
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
