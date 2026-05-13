package ir.daneshrefah.scm.cache.client.connector;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRoute;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRouteResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class CacheTemplateImpl implements CacheTemplate {

    private final CacheRouteResolver routeResolver;
    private final CacheBackendRouter backendRouter;
    private final Optional<HazelcastInstance> hazelcastInstance;

    @Override
    public Object getFromCache(String mapName, String key) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache get: cache='{}', key='{}', backend={}", mapName, key, route.type());
        return backend(route).get(route, key);
    }

    @Override
    public void putInCache(String mapName, String key, Object value) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache put: cache='{}', key='{}', backend={}", mapName, key, route.type());
        backend(route).put(route, key, value, route.ttl());
    }

    @Override
    public void putInCache(String mapName, String key, Object value, long timeToLiveMinutes) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache put with ttl: cache='{}', key='{}', ttlMinutes={}, backend={}",
                mapName, key, timeToLiveMinutes, route.type());
        backend(route).put(route, key, value, Duration.ofMinutes(timeToLiveMinutes));
    }

    @Override
    public void putInCacheIfAbsent(String mapName, String key, Object value) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache putIfAbsent: cache='{}', key='{}', backend={}", mapName, key, route.type());
        backend(route).putIfAbsent(route, key, value, route.ttl());
    }

    @Override
    public void putInCacheIfAbsent(String mapName, String key, Object value, long timeToLiveMinutes) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache putIfAbsent with ttl: cache='{}', key='{}', ttlMinutes={}, backend={}",
                mapName, key, timeToLiveMinutes, route.type());
        backend(route).putIfAbsent(route, key, value, Duration.ofMinutes(timeToLiveMinutes));
    }

    @Override
    public Object removeFromCache(String mapName, String key) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache remove: cache='{}', key='{}', backend={}", mapName, key, route.type());
        return backend(route).remove(route, key);
    }

    @Override
    public Map createCacheIfNull(String mapName) {
        CacheRoute route = routeResolver.resolve(mapName);
        log.debug("Cache snapshot requested: cache='{}', backend={}", mapName, route.type());
        return backend(route).snapshot(route);
    }

    @Override
    public FlakeIdGenerator createIdGeneratorIfNull(String name) {
        log.debug("FlakeId generator requested: name='{}'", name);
        return hazelcastInstance
                .map(instance -> instance.getFlakeIdGenerator(name))
                .orElseThrow(() -> new IllegalStateException("Hazelcast instance is required for flake id generation"));
    }

    private CacheBackend backend(CacheRoute route) {
        return backendRouter.get(route.type());
    }
}
