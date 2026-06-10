package ir.daneshrefah.scm.cache.client.utility.concurrencylimit;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutingConcurrencyLimiterUtilityTest {

    @Test
    void routesByBaseNameBeforeKeyPart() {
        CacheClientProperties.UtilityBackends utilities = new CacheClientProperties.UtilityBackends();
        utilities.setConcurrencyLimit(CacheClientProperties.UtilityBackendType.REMOTE);
        utilities.getConcurrencyLimitNames().put("otp-send", CacheClientProperties.UtilityBackendType.LOCAL);
        RecordingConcurrencyLimiterUtility localUtility = new RecordingConcurrencyLimiterUtility();
        RecordingConcurrencyLimiterUtility remoteUtility = new RecordingConcurrencyLimiterUtility();
        RoutingConcurrencyLimiterUtility routingUtility = new RoutingConcurrencyLimiterUtility(
                localUtility,
                remoteUtility,
                utilities
        );

        String result = routingUtility.executeWithConcurrencyLimit(
                "otp-send::uid::42",
                3,
                Duration.ZERO,
                () -> "done"
        );

        assertEquals("done", result);
        assertEquals(1, localUtility.executionCount());
        assertEquals(0, remoteUtility.executionCount());
    }

    private static final class RecordingConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {
        private final AtomicInteger executionCount = new AtomicInteger();

        @Override
        public boolean initialize(String limitName, int maxConcurrentExecutions) {
            return true;
        }

        @Override
        public boolean tryAcquire(String limitName) {
            return true;
        }

        @Override
        public boolean tryAcquire(String limitName, int slots, Duration waitTime) {
            return true;
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
        public <T> T executeWithConcurrencyLimit(String limitName,
                                                 int maxConcurrentExecutions,
                                                 Duration waitTime,
                                                 Callable<T> job) {
            try {
                executionCount.incrementAndGet();
                return job.call();
            } catch (Exception exception) {
                throw new ConcurrencyLimitOperationException("Concurrency limit execution failed: " + limitName, exception);
            }
        }

        private int executionCount() {
            return executionCount.get();
        }
    }
}
