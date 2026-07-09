package ir.daneshrefah.scm.cache.starter.connector.spring;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.starter.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.starter.connector.routing.CacheRoute;
import ir.daneshrefah.scm.cache.starter.connector.routing.CacheRouteResolver;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class RoutingCacheManager implements CacheManager {

    private final CacheRouteResolver routeResolver;
    private final CacheBackendRouter backendRouter;
    private final CacheClientProperties properties;
    private final ScmCacheEventSupport cacheEventSupport;
    private final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, this::createCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new LinkedHashSet<>(properties.getCaches().keySet());
        names.addAll(cacheMap.keySet());
        return Collections.unmodifiableSet(names);
    }

    private Cache createCache(String cacheName) {
        CacheRoute route = routeResolver.resolve(cacheName);
        CacheBackend backend = backendRouter.get(route.type());
        log.info("Spring cache mapped: cache='{}', backend={}", cacheName, route.type());
        return new RoutingSpringCache(
                route,
                backend,
                cacheEventSupport,
                properties.getEvents() != null && properties.getEvents().isPublishStarted()
        );
    }
}
