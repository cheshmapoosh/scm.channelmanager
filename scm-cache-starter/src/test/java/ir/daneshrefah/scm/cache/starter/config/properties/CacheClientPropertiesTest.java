package ir.daneshrefah.scm.cache.starter.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CacheClientPropertiesTest {

    @Test
    void remoteClusterNameBinds() {
        CacheClientProperties properties = bind("scm.cache.client.remote.cluster-name", "cache-cluster-a");

        assertEquals("cache-cluster-a", properties.getRemote().getClusterName());
    }

    @Test
    void remoteAddressesBind() {
        CacheClientProperties properties = bind(
                "scm.cache.client.remote.addresses",
                "10.0.0.1:5701,10.0.0.2:5701"
        );

        assertEquals(
                java.util.List.of("10.0.0.1:5701", "10.0.0.2:5701"),
                properties.getRemote().getAddresses()
        );
    }

    @Test
    void remoteAddressesBindFromIndexedProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.cache.client.remote.addresses[0]", "10.0.0.1:5701")
                .withProperty("scm.cache.client.remote.addresses[1]", "10.0.0.2:5701");

        CacheClientProperties properties = bind(environment);

        assertEquals(
                java.util.List.of("10.0.0.1:5701", "10.0.0.2:5701"),
                properties.getRemote().getAddresses()
        );
    }

    @Test
    void localAndRemotePropertiesDoNotExposeProvider() {
        assertFalse(fieldNames(CacheClientProperties.LocalProperties.class).contains("provider"));
        assertFalse(fieldNames(CacheClientProperties.RemoteProperties.class).contains("provider"));
    }

    private CacheClientProperties bind(String name, String value) {
        return bind(new MockEnvironment().withProperty(name, value));
    }

    private CacheClientProperties bind(MockEnvironment environment) {
        return Binder.get(environment)
                .bind("scm.cache.client", CacheClientProperties.class)
                .orElseGet(CacheClientProperties::new);
    }

    private Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}
