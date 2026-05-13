package ir.daneshrefah.scm.cache.client.connector.routing;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.config.properties.CacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class CacheRouteResolver {

    private final CacheClientProperties properties;
    private final Map<String, CacheRoute> routes = new ConcurrentHashMap<>();

    public CacheRoute resolve(String cacheName) {
        return routes.computeIfAbsent(cacheName, this::buildRoute);
    }

    private CacheRoute buildRoute(String cacheName) {
        CacheClientProperties.CacheDefinition definition = properties.getCaches().get(cacheName);

        CacheType type = properties.getDefaultType();
        String targetName = cacheName;
        Duration ttl = properties.getDefaultTtl();
        long maximumSize = properties.getDefaultMaximumSize();

        if (definition != null) {
            if (definition.getType() != null) {
                type = definition.getType();
            }
            if (StringUtils.hasText(definition.getRemoteName())) {
                targetName = definition.getRemoteName();
            }
            if (definition.getTtl() != null) {
                ttl = definition.getTtl();
            }
            if (definition.getMaximumSize() != null && definition.getMaximumSize() > 0) {
                maximumSize = definition.getMaximumSize();
            }
        }

        CacheRoute route = new CacheRoute(cacheName, targetName, type, ttl, maximumSize);
        log.info("Cache route resolved: cache='{}', target='{}', type={}, ttl={}, maxSize={}",
                route.cacheName(), route.targetName(), route.type(), route.ttl(), route.maximumSize());
        return route;
    }
}
