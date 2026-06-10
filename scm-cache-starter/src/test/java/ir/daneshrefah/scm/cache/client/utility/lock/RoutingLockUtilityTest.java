package ir.daneshrefah.scm.cache.client.utility.lock;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingLockUtilityTest {

    @Test
    void routesByBaseNameBeforeKeyPart() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setLock(CacheClientProperties.UtilityBackendType.REMOTE);
        utilities.getLockNames().put("user-update", CacheClientProperties.UtilityBackendType.LOCAL);

        RecordingLockUtility localUtility = new RecordingLockUtility();
        RecordingLockUtility remoteUtility = new RecordingLockUtility();
        RoutingLockUtility routingLockUtility = new RoutingLockUtility(
                localUtility,
                remoteUtility,
                utilities
        );

        String result = routingLockUtility.executeWithLock(
                "user-update::uid::42",
                Duration.ZERO,
                () -> "done"
        );

        assertEquals("done", result);
        assertEquals(1, localUtility.executionCount());
        assertEquals(0, remoteUtility.executionCount());
    }

    private static final class RecordingLockUtility implements LockUtility {
        private final AtomicInteger executionCount = new AtomicInteger();

        private RecordingLockUtility() {
        }

        @Override
        public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
            try {
                executionCount.incrementAndGet();
                return job.call();
            } catch (Exception exception) {
                throw new LockExecutionException(lockName, exception);
            }
        }

        private int executionCount() {
            return executionCount.get();
        }
    }
}
