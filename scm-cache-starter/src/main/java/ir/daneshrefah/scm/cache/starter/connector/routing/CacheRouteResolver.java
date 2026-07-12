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

        CacheType type = effectiveType(definition);

        CacheRoute route = switch (type) {
            case LOCAL -> localRoute(cacheName, definition);
            case REMOTE -> remoteRoute(cacheName, definition);
            case NEAR -> nearRoute(cacheName, definition);
        };

        log.info("Cache route resolved: cache='{}', target='{}', type={}, {}",
                route.cacheName(), route.targetName(), route.type(), describePolicy(route));
        return route;
    }

    private CacheRoute localRoute(String cacheName, CacheClientProperties.CacheDefinition definition) {
        Duration ttl = properties.getLocal().getTtl();
        long maximumSize = properties.getLocal().getMaximumSize();

        if (definition != null) {
            if (definition.getTtl() != null) {
                ttl = definition.getTtl();
            }
            if (definition.getMaximumSize() != null) {
                maximumSize = definition.getMaximumSize();
            }
        }

        return new CacheRoute(
                cacheName,
                cacheName,
                CacheType.LOCAL,
                ttl,
                maximumSize,
                CacheRoute.TtlOwnership.HOST,
                CacheRoute.SizeOwnership.LOCAL
        );
    }

    private CacheRoute remoteRoute(String cacheName, CacheClientProperties.CacheDefinition definition) {
        return new CacheRoute(
                cacheName,
                targetName(cacheName, definition),
                CacheType.REMOTE,
                null,
                0L,
                CacheRoute.TtlOwnership.SERVER_MANAGED,
                CacheRoute.SizeOwnership.NOT_APPLICABLE
        );
    }

    private CacheRoute nearRoute(String cacheName, CacheClientProperties.CacheDefinition definition) {
        long maximumSize = definition != null && definition.getMaximumSize() != null
                ? definition.getMaximumSize()
                : properties.getNear().getMaximumSize();
        return new CacheRoute(
                cacheName,
                targetName(cacheName, definition),
                CacheType.NEAR,
                null,
                maximumSize,
                CacheRoute.TtlOwnership.SERVER_MANAGED,
                CacheRoute.SizeOwnership.NEAR_CLIENT
        );
    }

    private CacheType effectiveType(CacheClientProperties.CacheDefinition definition) {
        if (definition != null && definition.getType() != null) {
            return definition.getType();
        }
        return properties.getDefaultType() == null ? CacheType.REMOTE : properties.getDefaultType();
    }

    private String targetName(String cacheName, CacheClientProperties.CacheDefinition definition) {
        if (definition != null && StringUtils.hasText(definition.getRemoteName())) {
            return definition.getRemoteName().trim();
        }
        return cacheName;
    }

    private String describePolicy(CacheRoute route) {
        if (route.type() == CacheType.LOCAL) {
            return "ttl=%s, maximumSize=%d".formatted(route.ttl(), route.maximumSize());
        }
        if (route.type() == CacheType.NEAR) {
            return "ttl=server-managed, nearMaximumSize=%d".formatted(route.maximumSize());
        }
        return "ttl=server-managed, maximumSize=not-applicable";
    }
}
