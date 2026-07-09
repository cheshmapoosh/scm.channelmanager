package ir.daneshrefah.scm.cache.starter.connector.routing;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
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

        if (definition != null) {
            if (definition.getType() != null) {
                type = definition.getType();
            }
            if (StringUtils.hasText(definition.getRemoteName())) {
                targetName = definition.getRemoteName();
            }
        }

        Duration ttl = defaultTtl(type);
        long maximumSize = defaultMaximumSize(type);

        if (definition != null) {
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

    private Duration defaultTtl(CacheType type) {
        if (type == CacheType.LOCAL) {
            return properties.getLocal().getTtl();
        }
        return Duration.ZERO;
    }

    private long defaultMaximumSize(CacheType type) {
        if (type == CacheType.NEAR) {
            return properties.getNear().getMaximumSize();
        }
        return properties.getLocal().getMaximumSize();
    }
}
