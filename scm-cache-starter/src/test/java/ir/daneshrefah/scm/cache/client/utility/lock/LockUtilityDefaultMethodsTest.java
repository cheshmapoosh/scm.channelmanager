package ir.daneshrefah.scm.cache.client.utility.lock;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockUtilityDefaultMethodsTest {

    @Test
    void executeWithLockFailureCallbackReturnsFallback() {
        LockUtility utility = new FailingLockUtility();

        String result = utility.executeWithLock(
                "L1",
                Duration.ZERO,
                () -> "ok",
                exception -> "fallback"
        );

        assertEquals("fallback", result);
    }

    @Test
    void runWithLockFailureConsumerIsInvoked() {
        LockUtility utility = new FailingLockUtility();
        AtomicBoolean failureHandled = new AtomicBoolean(false);

        utility.runWithLock(
                "L1",
                Duration.ZERO,
                () -> {
                },
                exception -> failureHandled.set(true)
        );

        assertTrue(failureHandled.get());
    }

    @Test
    void executeWithLockBlockModePassesNullWaitTime() {
        CapturingLockUtility utility = new CapturingLockUtility();

        utility.executeWithLock("L2", true, () -> "ok");

        assertNull(utility.lastWaitTime);
    }

    @Test
    void executeWithLockNonBlockModePassesZeroDuration() {
        CapturingLockUtility utility = new CapturingLockUtility();

        utility.executeWithLock("L3", false, () -> "ok");

        assertSame(Duration.ZERO, utility.lastWaitTime);
    }

    private static class FailingLockUtility implements LockUtility {

        @Override
        public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
            throw new LockAcquireFailedException(lockName, waitTime);
        }
    }

    private static class CapturingLockUtility implements LockUtility {

        private Duration lastWaitTime;

        @Override
        public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
            this.lastWaitTime = waitTime;
            try {
                return job.call();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
