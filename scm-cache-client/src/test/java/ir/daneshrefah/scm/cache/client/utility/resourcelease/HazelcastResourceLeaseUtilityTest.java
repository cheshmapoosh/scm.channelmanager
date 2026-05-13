package ir.daneshrefah.scm.cache.client.utility.resourcelease;

import com.hazelcast.map.IMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HazelcastResourceLeaseUtilityTest {

    @SuppressWarnings("unchecked")
    @Test
    void acquiresAndReleasesRemoteLease() {
        IMap<String, String> leaseMap = Mockito.mock(IMap.class);
        when(leaseMap.putIfAbsent(eq("pool-c::9001"), eq("owner-1"), eq(10_000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(null);
        when(leaseMap.get("pool-c::9001")).thenReturn("owner-1");

        HazelcastResourceLeaseUtility utility = new HazelcastResourceLeaseUtility(leaseMap, "owner-1");

        ResourceLease lease = utility.acquire("pool-c", List.of("9001"), Duration.ofSeconds(10));
        assertEquals("pool-c", lease.poolName());
        assertEquals("9001", lease.resourceName());
        lease.close();

        verify(leaseMap).remove("pool-c::9001");
    }
}
