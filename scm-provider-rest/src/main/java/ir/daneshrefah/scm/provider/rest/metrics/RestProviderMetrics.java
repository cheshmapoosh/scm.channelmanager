package ir.daneshrefah.scm.provider.rest.metrics;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RestProviderMetrics {
    private final ConcurrentMap<String, CounterSet> counters = new ConcurrentHashMap<>();

    public CounterSet provider(String provider) {
        return counters.computeIfAbsent(provider, ignored -> new CounterSet());
    }

    public static final class CounterSet {
        private final AtomicLong submitted = new AtomicLong();
        private final AtomicLong succeeded = new AtomicLong();
        private final AtomicLong failed = new AtomicLong();
        private final AtomicLong clientErrors = new AtomicLong();
        private final AtomicLong serverErrors = new AtomicLong();
        private final AtomicLong timedOut = new AtomicLong();
        private final AtomicLong rateLimited = new AtomicLong();
        private final AtomicLong rateLimitWaits = new AtomicLong();
        private final AtomicLong totalLatencyMs = new AtomicLong();
        private final AtomicLong tokenCacheHits = new AtomicLong();
        private final AtomicLong tokenCacheMisses = new AtomicLong();
        private final AtomicLong tokenCachePuts = new AtomicLong();
        private final AtomicLong tokenLockAcquired = new AtomicLong();
        private final AtomicLong tokenLockTimeouts = new AtomicLong();
        private final AtomicLong tokenRefreshes = new AtomicLong();
        private final AtomicLong tokenRefreshFailures = new AtomicLong();
        private final AtomicLong tokenRequestLatencyMs = new AtomicLong();
        private final AtomicLong customizerExecutions = new AtomicLong();
        private final AtomicLong customizerErrors = new AtomicLong();

        public void submitted() {
            submitted.incrementAndGet();
        }

        public void succeeded() {
            succeeded.incrementAndGet();
        }

        public void failed() {
            failed.incrementAndGet();
        }

        public void clientError() {
            clientErrors.incrementAndGet();
        }

        public void serverError() {
            serverErrors.incrementAndGet();
        }

        public void timedOut() {
            timedOut.incrementAndGet();
        }

        public void rateLimited() {
            rateLimited.incrementAndGet();
        }

        public void rateLimitWait() {
            rateLimitWaits.incrementAndGet();
        }

        public void addLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                totalLatencyMs.addAndGet(elapsedMs);
            }
        }

        public void tokenCacheHit() {
            tokenCacheHits.incrementAndGet();
        }

        public void tokenCacheMiss() {
            tokenCacheMisses.incrementAndGet();
        }

        public void tokenCachePut() {
            tokenCachePuts.incrementAndGet();
        }

        public void tokenLockAcquired() {
            tokenLockAcquired.incrementAndGet();
        }

        public void tokenLockTimeout() {
            tokenLockTimeouts.incrementAndGet();
        }

        public void tokenRefresh() {
            tokenRefreshes.incrementAndGet();
        }

        public void tokenRefreshFailure() {
            tokenRefreshFailures.incrementAndGet();
        }

        public void addTokenRequestLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                tokenRequestLatencyMs.addAndGet(elapsedMs);
            }
        }

        public void customizerExecution() {
            customizerExecutions.incrementAndGet();
        }

        public void customizerError() {
            customizerErrors.incrementAndGet();
        }

        public long submittedCount() {
            return submitted.get();
        }

        public long succeededCount() {
            return succeeded.get();
        }

        public long failedCount() {
            return failed.get();
        }

        public long rateLimitedCount() {
            return rateLimited.get();
        }

        public long tokenCacheHitCount() {
            return tokenCacheHits.get();
        }

        public long tokenCacheMissCount() {
            return tokenCacheMisses.get();
        }

        public long tokenLockAcquiredCount() {
            return tokenLockAcquired.get();
        }

        public long tokenLockTimeoutCount() {
            return tokenLockTimeouts.get();
        }

        public long tokenRefreshCount() {
            return tokenRefreshes.get();
        }

        public long tokenRefreshFailureCount() {
            return tokenRefreshFailures.get();
        }

        public long customizerExecutionCount() {
            return customizerExecutions.get();
        }

        public long customizerErrorCount() {
            return customizerErrors.get();
        }
    }
}
