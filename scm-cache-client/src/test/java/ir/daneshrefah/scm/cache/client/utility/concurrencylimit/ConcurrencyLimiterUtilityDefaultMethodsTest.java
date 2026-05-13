package ir.daneshrefah.scm.cache.client.utility.concurrencylimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyLimiterUtilityDefaultMethodsTest {

    @Test
    void executeWithConcurrencyLimitFailureCallbackReturnsFallback() {
        ConcurrencyLimiterUtility utility = new FailingConcurrencyLimiterUtility();

        String result = utility.executeWithConcurrencyLimit(
                "Q1",
                2,
                Duration.ZERO,
                () -> "ok",
                exception -> "fallback"
        );

        assertEquals("fallback", result);
    }

    @Test
    void runWithConcurrencyLimitFailureConsumerIsInvoked() {
        ConcurrencyLimiterUtility utility = new FailingConcurrencyLimiterUtility();
        AtomicBoolean failureHandled = new AtomicBoolean(false);

        utility.runWithConcurrencyLimit(
                "Q1",
                2,
                Duration.ZERO,
                () -> {
                },
                exception -> failureHandled.set(true)
        );

        assertTrue(failureHandled.get());
    }

    private static class FailingConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {

        @Override
        public boolean initialize(String limitName, int maxConcurrentExecutions) {
            return false;
        }

        @Override
        public boolean tryAcquire(String limitName) {
            return false;
        }

        @Override
        public boolean tryAcquire(String limitName, int slots, Duration waitTime) {
            return false;
        }

        @Override
        public void release(String limitName) {
        }

        @Override
        public void release(String limitName, int slots) {
        }

        @Override
        public int availableSlots(String limitName) {
            return 0;
        }

        @Override
        public <T> T executeWithConcurrencyLimit(String limitName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job) {
            throw new ConcurrencyLimitAcquireTimeoutException(limitName, waitTime);
        }
    }
}
