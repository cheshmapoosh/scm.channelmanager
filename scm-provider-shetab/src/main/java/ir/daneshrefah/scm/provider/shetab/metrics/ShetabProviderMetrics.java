package ir.daneshrefah.scm.provider.shetab.metrics;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ShetabProviderMetrics {
    private final ConcurrentMap<String, CounterSet> counters = new ConcurrentHashMap<>();

    public CounterSet provider(String provider) {
        return counters.computeIfAbsent(provider, ignored -> new CounterSet());
    }

    public static final class CounterSet {
        private final AtomicLong submitted = new AtomicLong();
        private final AtomicLong rateLimited = new AtomicLong();
        private final AtomicLong rateLimitWaits = new AtomicLong();
        private final AtomicLong queueRejected = new AtomicLong();
        private final AtomicLong succeeded = new AtomicLong();
        private final AtomicLong sent = new AtomicLong();
        private final AtomicLong received = new AtomicLong();
        private final AtomicLong failed = new AtomicLong();
        private final AtomicLong timedOut = new AtomicLong();
        private final AtomicLong totalLatencyMs = new AtomicLong();

        public void submitted() {
            submitted.incrementAndGet();
        }

        public void rateLimited() {
            rateLimited.incrementAndGet();
        }

        public void rateLimitWait() {
            rateLimitWaits.incrementAndGet();
        }

        public void queueRejected() {
            queueRejected.incrementAndGet();
        }

        public void succeeded() {
            succeeded.incrementAndGet();
        }

        public void sent() {
            sent.incrementAndGet();
        }

        public void received() {
            received.incrementAndGet();
        }

        public void failed() {
            failed.incrementAndGet();
        }

        public void timedOut() {
            timedOut.incrementAndGet();
        }

        public void addLatency(long elapsedMs) {
            if (elapsedMs > 0) {
                totalLatencyMs.addAndGet(elapsedMs);
            }
        }

        public long submittedCount() {
            return submitted.get();
        }

        public long rateLimitedCount() {
            return rateLimited.get();
        }

        public long sentCount() {
            return sent.get();
        }

        public long receivedCount() {
            return received.get();
        }

        public long failedCount() {
            return failed.get();
        }

        public long succeededCount() {
            return succeeded.get();
        }
    }
}
