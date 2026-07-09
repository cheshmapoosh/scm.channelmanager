package ir.daneshrefah.scm.cache.starter.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheClientAutoConfigurationTest {

    @Test
    void clientConfigUsesRemoteProperties() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.getRemote().setClusterName("remote-cluster");
        properties.getRemote().setAddresses(java.util.List.of("10.0.0.1:5701", "10.0.0.2:5701"));

        ClientConfig clientConfig = new CacheClientAutoConfiguration().clientConfig(properties);

        assertEquals("remote-cluster", clientConfig.getClusterName());
        assertEquals(java.util.List.of("10.0.0.1:5701", "10.0.0.2:5701"),
                clientConfig.getNetworkConfig().getAddresses());
    }

    @Test
    void nearMaximumSizeIsUsedWhenBuildingNearCacheConfig() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.getNear().setMaximumSize(256L);
        properties.getNear().setInvalidateOnChange(false);
        properties.getNear().setInMemoryFormat("BINARY");

        CacheClientProperties.CacheDefinition definition = new CacheClientProperties.CacheDefinition();
        definition.setType(CacheType.NEAR);
        definition.setRemoteName("remote-orders");
        properties.getCaches().put("orders", definition);

        ClientConfig clientConfig = new CacheClientAutoConfiguration().clientConfig(properties);
        NearCacheConfig nearCacheConfig = clientConfig.getNearCacheConfigMap().get("remote-orders");

        assertNotNull(nearCacheConfig);
        assertEquals(256, nearCacheConfig.getEvictionConfig().getSize());
        assertEquals(InMemoryFormat.BINARY, nearCacheConfig.getInMemoryFormat());
        assertEquals(false, nearCacheConfig.isInvalidateOnChange());
    }

    @Test
    void perCacheMaximumSizeOverridesNearDefaultWhenBuildingNearCacheConfig() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.getNear().setMaximumSize(256L);

        CacheClientProperties.CacheDefinition definition = new CacheClientProperties.CacheDefinition();
        definition.setType(CacheType.NEAR);
        definition.setMaximumSize(32L);
        properties.getCaches().put("orders", definition);

        ClientConfig clientConfig = new CacheClientAutoConfiguration().clientConfig(properties);
        NearCacheConfig nearCacheConfig = clientConfig.getNearCacheConfigMap().get("orders");

        assertNotNull(nearCacheConfig);
        assertEquals(32, nearCacheConfig.getEvictionConfig().getSize());
    }

    @Test
    void nearCacheFailsWhenNearDefaultsAreDisabled() {
        CacheClientProperties properties = new CacheClientProperties();
        properties.getNear().setEnabled(false);

        CacheClientProperties.CacheDefinition definition = new CacheClientProperties.CacheDefinition();
        definition.setType(CacheType.NEAR);
        properties.getCaches().put("orders", definition);

        assertThrows(IllegalStateException.class, () -> new CacheClientAutoConfiguration().clientConfig(properties));
    }

    @Test
    void nearCacheFailsWhenDistributedClientIsDisabled() {
        CacheClientProperties properties = new CacheClientProperties();
        CacheClientProperties.CacheDefinition definition = new CacheClientProperties.CacheDefinition();
        definition.setType(CacheType.NEAR);
        properties.getCaches().put("orders", definition);

        assertThrows(IllegalStateException.class, () -> new CacheClientAutoConfiguration().hazelcastEmbed(properties));
    }
}
