package ir.daneshrefah.scm.cache.service;

import com.hazelcast.flakeidgen.FlakeIdGenerator;
import ir.daneshrefah.scm.cache.domain.dto.UserAuthenticationTO;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;

import java.util.List;
import java.util.Map;

public interface HazelCastService {

    List<String> getMapList();

    Object getMapData(String map);

    Object getFromCache(String mapName, String key);

    Object getEntryView(String mapName, String key);

    Object putInCache(String mapName, String key, Object value);

    Object putInCache(String mapName, String key, Object value, int timeToLiveSeconds);

    Object putInCache(String mapName, String key, Object value, int timeToLiveSeconds, int maxIdle);

    Object removeFromCache(String mapName, String key);

    Map<?, ?> createCacheIfNull(String mapName);

    FlakeIdGenerator createIdGeneratorIfNull(String name);

    boolean exists(String mapName, String key);

    UserAuthenticationTO putSession(UserAuthenticationTO userAuthentication);
    UserAuthenticationTO getSession(String nickname,String terminalCode);
    UserAuthenticationTO removeSession(String nickname,String terminalCode);
}
