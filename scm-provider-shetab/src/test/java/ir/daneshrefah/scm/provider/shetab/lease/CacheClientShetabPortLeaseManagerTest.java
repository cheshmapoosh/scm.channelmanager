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

class CacheClientShetabPortLeaseManagerTest {

    @Test
    void acquiresPortUsingResourceLeaseUtility() {
        FakeResourceLeaseUtility utility = new FakeResourceLeaseUtility("5002");
        CacheClientShetabPortLeaseManager manager = new CacheClientShetabPortLeaseManager(utility);

        ShetabPortLease lease = manager.acquire(config(true, List.of(5001, 5002), "10.10.10.10"));

        assertEquals(5002, lease.port());
        assertEquals("shetab-local-port::10.10.10.10", utility.poolName);
        assertEquals(List.of("5001", "5002"), utility.candidates);
        assertEquals(Duration.ofMillis(30_000L), utility.ttl);

        lease.close();
        assertTrue(utility.closed);
    }

    @Test
    void returnsNoneWhenPortLeaseIsDisabled() {
        FakeResourceLeaseUtility utility = new FakeResourceLeaseUtility("5001");
        CacheClientShetabPortLeaseManager manager = new CacheClientShetabPortLeaseManager(utility);

        ShetabPortLease lease = manager.acquire(config(false, List.of(5001, 5002), null));

        assertEquals(0, lease.port());
    }

    private static ShetabResolvedConfig config(boolean enabled, List<Integer> localPorts, String localAddress) {
        return new ShetabResolvedConfig(
                "poya",
                "127.0.0.1",
                9000,
                localAddress,
                localPorts,
                "ASCII",
                4,
                null,
                null,
                3000,
                1000,
                6000,
                1000,
                1000,
                1000,
                new ShetabResolvedConfig.RateLimit(false, "unused", "provider"),
                new ShetabResolvedConfig.PortLease(enabled, 30_000L)
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
