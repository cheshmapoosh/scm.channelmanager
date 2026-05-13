package ir.daneshrefah.scm.cache.client.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HazelcastLockUtilityTest {

    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private CPSubsystem cpSubsystem;
    @Mock
    private FencedLock lock;

    @Test
    void blocksWhenWaitTimeIsNull() {
        when(hazelcastInstance.getCPSubsystem()).thenReturn(cpSubsystem);
        when(cpSubsystem.getLock("lock-A")).thenReturn(lock);

        HazelcastLockUtility utility = new HazelcastLockUtility(hazelcastInstance);

        String result = utility.executeWithLock("lock-A", null, () -> "done");

        assertEquals("done", result);
        verify(lock).lock();
        verify(lock, never()).tryLock();
        verify(lock).unlock();
    }

    @Test
    void throwsWhenImmediateTryLockFails() {
        when(hazelcastInstance.getCPSubsystem()).thenReturn(cpSubsystem);
        when(cpSubsystem.getLock("lock-B")).thenReturn(lock);
        when(lock.tryLock()).thenReturn(false);

        HazelcastLockUtility utility = new HazelcastLockUtility(hazelcastInstance);

        assertThrows(
                LockAcquireFailedException.class,
                () -> utility.executeWithLock("lock-B", Duration.ZERO, () -> "done")
        );
        verify(lock, never()).unlock();
    }
}
