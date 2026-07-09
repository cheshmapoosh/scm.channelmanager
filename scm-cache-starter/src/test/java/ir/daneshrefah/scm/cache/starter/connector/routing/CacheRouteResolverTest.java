package ir.daneshrefah.scm.cache.starter.connector.routing;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheRouteResolverTest {

    @Test
    void localMaximumSizeUsesLocalDefault() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.setDefaultType(CacheType.LOCAL);
        properties.getLocal().setMaximumSize(512L);

        CacheRoute route = new CacheRouteResolver(properties).resolve("local-cache");

        assertEquals(CacheType.LOCAL, route.type());
        assertEquals(512L, route.maximumSize());
    }

    @Test
    void localTtlUsesLocalDefault() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.setDefaultType(CacheType.LOCAL);
        properties.getLocal().setTtl(Duration.ofMinutes(5));

        CacheRoute route = new CacheRouteResolver(properties).resolve("local-cache");

        assertEquals(Duration.ofMinutes(5), route.ttl());
    }

    @Test
    void perCacheMaximumSizeOverridesLocalDefault() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.setDefaultType(CacheType.LOCAL);
        properties.getLocal().setMaximumSize(512L);

        CacheClientProperties.CacheDefinition definition = new CacheClientProperties.CacheDefinition();
        definition.setType(CacheType.LOCAL);
        definition.setMaximumSize(64L);
        properties.getCaches().put("local-cache", definition);

        CacheRoute route = new CacheRouteResolver(properties).resolve("local-cache");

        assertEquals(64L, route.maximumSize());
    }

    @Test
    void perCacheMaximumSizeOverridesNearDefault() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.setDefaultType(CacheType.NEAR);
        properties.getNear().setMaximumSize(1_024L);

        CacheClientProperties.CacheDefinition definition = new CacheClientProperties.CacheDefinition();
        definition.setType(CacheType.NEAR);
        definition.setMaximumSize(128L);
        properties.getCaches().put("near-cache", definition);

        CacheRoute route = new CacheRouteResolver(properties).resolve("near-cache");

        assertEquals(CacheType.NEAR, route.type());
        assertEquals(128L, route.maximumSize());
    }
}
