package ir.daneshrefah.scm.cache.client.utility.concurrencylimit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.ISemaphore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazelcastConcurrencyLimiterUtilityTest {

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private CPSubsystem cpSubsystem;
    @Mock
    private ISemaphore semaphore;

    @Test
    void executeWithConcurrencyLimitBlocksOnAcquireAndReleasesPermit() throws Exception {
        when(hazelcastInstance.getCPSubsystem()).thenReturn(cpSubsystem);
        when(cpSubsystem.getSemaphore("queue-A")).thenReturn(semaphore);
        when(semaphore.init(2)).thenReturn(true);

        HazelcastConcurrencyLimiterUtility utility = new HazelcastConcurrencyLimiterUtility(hazelcastInstance);

        String result = utility.executeWithConcurrencyLimit("queue-A", 2, null, () -> "done");

        assertEquals("done", result);
        verify(semaphore).acquire();
        verify(semaphore).release();
    }

    @Test
    void executeWithConcurrencyLimitThrowsTimeoutWhenPermitIsNotAvailable() {
        when(hazelcastInstance.getCPSubsystem()).thenReturn(cpSubsystem);
        when(cpSubsystem.getSemaphore("queue-B")).thenReturn(semaphore);
        when(semaphore.init(1)).thenReturn(false);
        when(semaphore.tryAcquire()).thenReturn(false);

        HazelcastConcurrencyLimiterUtility utility = new HazelcastConcurrencyLimiterUtility(hazelcastInstance);

        assertThrows(
                ConcurrencyLimitAcquireTimeoutException.class,
                () -> utility.executeWithConcurrencyLimit("queue-B", 1, java.time.Duration.ZERO, () -> "done")
        );
        verify(semaphore, never()).release();
    }
}
