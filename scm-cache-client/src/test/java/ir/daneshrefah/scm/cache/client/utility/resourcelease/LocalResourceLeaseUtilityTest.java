package ir.daneshrefah.scm.cache.client.utility.resourcelease;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalResourceLeaseUtilityTest {

    @Test
    void acquireAndReleaseAllowsReuse() {
        LocalResourceLeaseUtility utility = new LocalResourceLeaseUtility();

        ResourceLease lease = utility.acquire("pool-a", List.of("7001"), Duration.ofSeconds(10));
        assertEquals("pool-a", lease.poolName());
        assertEquals("7001", lease.resourceName());

        assertThrows(
                ResourceLeaseAcquireException.class,
                () -> utility.acquire("pool-a", List.of("7001"), Duration.ofSeconds(10))
        );

        lease.close();

        ResourceLease secondLease = utility.acquire("pool-a", List.of("7001"), Duration.ofSeconds(10));
        assertEquals("7001", secondLease.resourceName());
        secondLease.close();
    }

    @Test
    void executeWithLeaseReleasesWhenJobIsDone() {
        LocalResourceLeaseUtility utility = new LocalResourceLeaseUtility();

        Integer selectedPort = utility.executeWithLease(
                "pool-b",
                List.of("8100"),
                Duration.ofSeconds(10),
                lease -> Integer.parseInt(lease.resourceName())
        );

        assertEquals(8100, selectedPort);

        ResourceLease lease = utility.acquire("pool-b", List.of("8100"), Duration.ofSeconds(10));
        lease.close();
    }
}
