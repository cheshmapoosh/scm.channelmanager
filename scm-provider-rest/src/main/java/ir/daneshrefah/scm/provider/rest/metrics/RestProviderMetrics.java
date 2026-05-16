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
        private final AtomicLong totalLatencyMs = new AtomicLong();
        private final AtomicLong tokenCacheHits = new AtomicLong();
        private final AtomicLong tokenRefreshes = new AtomicLong();
        private final AtomicLong tokenRefreshFailures = new AtomicLong();

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

        public void addLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                totalLatencyMs.addAndGet(elapsedMs);
            }
        }

        public void tokenCacheHit() {
            tokenCacheHits.incrementAndGet();
        }

        public void tokenRefresh() {
            tokenRefreshes.incrementAndGet();
        }

        public void tokenRefreshFailure() {
            tokenRefreshFailures.incrementAndGet();
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
    }
}
