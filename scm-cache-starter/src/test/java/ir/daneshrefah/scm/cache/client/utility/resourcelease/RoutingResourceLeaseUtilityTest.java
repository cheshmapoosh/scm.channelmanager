package ir.daneshrefah.scm.cache.client.utility.resourcelease;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingResourceLeaseUtilityTest {

    @Test
    void routesByBaseNameBeforeKeyPart() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setResourceLease(CacheClientProperties.UtilityBackendType.REMOTE);
        utilities.getResourceLeaseNames().put("shetab-local-port", CacheClientProperties.UtilityBackendType.LOCAL);
        RecordingResourceLeaseUtility localUtility = new RecordingResourceLeaseUtility("local-port");
        RecordingResourceLeaseUtility remoteUtility = new RecordingResourceLeaseUtility("remote-port");
        RoutingResourceLeaseUtility routingUtility = new RoutingResourceLeaseUtility(
                localUtility,
                remoteUtility,
                utilities
        );

        ResourceLease lease = routingUtility.acquire(
                "shetab-local-port::0.0.0.0",
                List.of("40001"),
                Duration.ofSeconds(30)
        );

        assertEquals("local-port", lease.resourceName());
        assertEquals(1, localUtility.acquireCount());
        assertEquals(0, remoteUtility.acquireCount());
    }

    private static final class RecordingResourceLeaseUtility implements ResourceLeaseUtility {
        private final String resourceName;
        private final AtomicInteger acquireCount = new AtomicInteger();

        private RecordingResourceLeaseUtility(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        public ResourceLease acquire(String poolName, java.util.Collection<String> candidates, Duration ttl) {
            acquireCount.incrementAndGet();
            return new RecordingResourceLease(poolName, resourceName);
        }

        private int acquireCount() {
            return acquireCount.get();
        }
    }

    private record RecordingResourceLease(String poolName, String resourceName) implements ResourceLease {
        @Override
        public void close() {
        }
    }
}
