package ir.daneshrefah.scm.provider.shetab.lease;

import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLease;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheClientShetabEndpointLeaseManagerTest {

    @Test
    void acquiresEndpointUsingResourceLeaseUtility() {
        FakeResourceLeaseUtility utility = new FakeResourceLeaseUtility("10.10.10.11:5002");
        CacheClientShetabEndpointLeaseManager manager = new CacheClientShetabEndpointLeaseManager(utility);

        ShetabEndpointLease lease = manager.acquire(config(true, List.of("10.10.10.10:5001", "10.10.10.11:5002")));

        assertEquals("10.10.10.11:5002", lease.endpoint());
        assertEquals("10.10.10.11", lease.remoteHost());
        assertEquals(5002, lease.remotePort());
        assertEquals("shetab-hps-endpoint::hps", utility.poolName);
        assertEquals(List.of("10.10.10.10:5001", "10.10.10.11:5002"), utility.candidates);
        assertEquals(Duration.ofMillis(30_000L), utility.ttl);

        lease.close();
        assertTrue(utility.closed);
    }

    @Test
    void returnsFirstEndpointWhenLeaseIsDisabled() {
        FakeResourceLeaseUtility utility = new FakeResourceLeaseUtility("10.10.10.10:5001");
        CacheClientShetabEndpointLeaseManager manager = new CacheClientShetabEndpointLeaseManager(utility);

        ShetabEndpointLease lease = manager.acquire(config(false, List.of("10.10.10.10:5001", "10.10.10.11:5002")));

        assertEquals("10.10.10.10:5001", lease.endpoint());
        assertEquals("10.10.10.10", lease.remoteHost());
        assertEquals(5001, lease.remotePort());
    }

    private static ShetabResolvedConfig config(boolean enabled, List<String> endpoints) {
        return new ShetabResolvedConfig(
                "hps",
                endpoints,
                null,
                null,
                3000,
                1000,
                6000,
                1000,
                1000,
                3,
                1000,
                new ShetabResolvedConfig.RateLimit(false, "unused", "provider"),
                new ShetabResolvedConfig.EndpointLease(enabled, 30_000L),
                new ShetabResolvedConfig.Security(
                        new ShetabResolvedConfig.Pin(false, null, 52, 2),
                        new ShetabResolvedConfig.Mac(false, null, 128, false, "AAAAAAAAAAAAAAAA", 16)
                )
        );
    }

    private static final class FakeResourceLeaseUtility implements ResourceLeaseUtility {
        private final String selectedResource;
        private String poolName;
        private List<String> candidates;
        private Duration ttl;
        private boolean closed;

        private FakeResourceLeaseUtility(String selectedResource) {
            this.selectedResource = selectedResource;
        }

        @Override
        public ResourceLease acquire(String poolName, Collection<String> candidates, Duration ttl) {
            this.poolName = poolName;
            this.candidates = List.copyOf(candidates);
            this.ttl = ttl;
            return new ResourceLease() {
                @Override
                public String poolName() {
                    return poolName;
                }

                @Override
                public String resourceName() {
                    return selectedResource;
                }

                @Override
                public void close() {
                    closed = true;
                }
            };
        }
    }
}
