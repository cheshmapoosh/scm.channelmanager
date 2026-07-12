package ir.daneshrefah.scm.provider.shetab.tcp;

import ir.daneshrefah.scm.observation.starter.provider.ProviderBusinessOutcome;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderAttemptResult;
import ir.daneshrefah.scm.provider.shetab.trace.ShetabProviderTraceLifecycle;
import org.jpos.iso.ISOMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

final class ShetabResponseRegistry {
    private final String provider;
    private final Object lock = new Object();
    private final Set<ResponseTracker> trackers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, ResponseTracker> exactIndex = new HashMap<>();
    private final Map<String, Set<ResponseTracker>> stanIndex = new HashMap<>();
    private final Map<String, Set<ResponseTracker>> rrnIndex = new HashMap<>();

    ShetabResponseRegistry(String provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    ResponseTracker register(
            ShetabCorrelationKey correlationKey,
            Deadline deadline,
            ShetabProviderTraceLifecycle traceLifecycle
    ) {
        Objects.requireNonNull(correlationKey, "correlationKey");
        Objects.requireNonNull(deadline, "deadline");

        if (!correlationKey.hasAnyValue()) {
            throw new IllegalArgumentException(
                    "Shetab request must contain field 11 (STAN) or field 37 (RRN) provider=" + provider
            );
        }

        ResponseTracker tracker = new ResponseTracker(
                correlationKey,
                deadline,
                System.nanoTime(),
                traceLifecycle
        );

        synchronized (lock) {
            String exactKey = correlationKey.exactKey();

            if (exactKey != null && exactIndex.containsKey(exactKey)) {
                throw new DuplicateShetabCorrelationException(
                        "Duplicate Shetab request correlation provider=" + provider
                                + " stan=" + correlationKey.stan()
                                + " rrn=" + correlationKey.rrn()
                );
            }

            trackers.add(tracker);

            if (exactKey != null) {
                exactIndex.put(exactKey, tracker);
            }

            addToIndex(stanIndex, correlationKey.stan(), tracker);
            addToIndex(rrnIndex, correlationKey.rrn(), tracker);
        }

        return tracker;
    }

    boolean contains(ResponseTracker tracker) {
        if (tracker == null || tracker.future().isDone()) {
            return false;
        }

        synchronized (lock) {
            return trackers.contains(tracker);
        }
    }

    boolean markSending(ResponseTracker tracker, long generation) {
        synchronized (lock) {
            if (!trackers.contains(tracker) || tracker.future().isDone()) {
                return false;
            }

            tracker.markSending(generation);
            return true;
        }
    }

    boolean markSent(ResponseTracker tracker) {
        synchronized (lock) {
            if (!trackers.contains(tracker) || tracker.future().isDone()) {
                return false;
            }

            tracker.markSent();
            return true;
        }
    }

    MatchResult match(ShetabCorrelationKey responseKey, long generation) {
        Objects.requireNonNull(responseKey, "responseKey");

        synchronized (lock) {
            if (responseKey.hasStan() && responseKey.hasRrn()) {
                ResponseTracker tracker = exactIndex.get(responseKey.exactKey());

                if (!canMatchGeneration(tracker, generation)) {
                    return MatchResult.unmatched(responseKey);
                }

                removeLocked(tracker);
                return MatchResult.matched(responseKey, tracker);
            }

            if (responseKey.hasStan()) {
                return matchFallbackLocked(responseKey, stanIndex.get(responseKey.stan()), generation, "STAN");
            }

            if (responseKey.hasRrn()) {
                return matchFallbackLocked(responseKey, rrnIndex.get(responseKey.rrn()), generation, "RRN");
            }

            return MatchResult.unmatched(responseKey);
        }
    }

    boolean remove(ResponseTracker tracker) {
        if (tracker == null) {
            return false;
        }

        synchronized (lock) {
            return removeLocked(tracker);
        }
    }

    boolean failIfActive(ResponseTracker tracker, Throwable error) {
        boolean removed = remove(tracker);
        return removed && tracker.future().completeExceptionally(error);
    }

    List<ResponseTracker> removeDeliveredByGeneration(long generation) {
        List<ResponseTracker> failed = new ArrayList<>();

        synchronized (lock) {
            for (ResponseTracker tracker : new ArrayList<>(trackers)) {
                if (tracker.wasDeliveredOn(generation)) {
                    removeLocked(tracker);
                    failed.add(tracker);
                }
            }
        }

        return failed;
    }

    List<ResponseTracker> removeExpired() {
        List<ResponseTracker> expired = new ArrayList<>();

        synchronized (lock) {
            for (ResponseTracker tracker : new ArrayList<>(trackers)) {
                if (tracker.deadline().isExpired()) {
                    removeLocked(tracker);
                    expired.add(tracker);
                }
            }
        }

        return expired;
    }

    List<ResponseTracker> removeAll() {
        List<ResponseTracker> all;

        synchronized (lock) {
            all = new ArrayList<>(trackers);
            trackers.clear();
            exactIndex.clear();
            stanIndex.clear();
            rrnIndex.clear();
        }

        return all;
    }

    int pendingCount() {
        synchronized (lock) {
            return trackers.size();
        }
    }

    private MatchResult matchFallbackLocked(
            ShetabCorrelationKey responseKey,
            Set<ResponseTracker> rawCandidates,
            long generation,
            String fallbackName
    ) {
        List<ResponseTracker> candidates = candidatesForGeneration(rawCandidates, generation);

        if (candidates.isEmpty()) {
            return MatchResult.unmatched(responseKey);
        }

        if (candidates.size() > 1) {
            return MatchResult.ambiguous(
                    responseKey,
                    new AmbiguousShetabResponseCorrelationException(
                            "Ambiguous Shetab response correlation provider=" + provider
                                    + " fallback=" + fallbackName
                                    + " candidates=" + candidates.size()
                                    + " stan=" + responseKey.stan()
                                    + " rrn=" + responseKey.rrn()
                    )
            );
        }

        ResponseTracker tracker = candidates.getFirst();
        removeLocked(tracker);
        return MatchResult.matched(responseKey, tracker);
    }

    private List<ResponseTracker> candidatesForGeneration(Set<ResponseTracker> rawCandidates, long generation) {
        if (rawCandidates == null || rawCandidates.isEmpty()) {
            return List.of();
        }

        List<ResponseTracker> candidates = new ArrayList<>();

        for (ResponseTracker candidate : rawCandidates) {
            if (canMatchGeneration(candidate, generation)) {
                candidates.add(candidate);
            }
        }

        return candidates;
    }

    private boolean canMatchGeneration(ResponseTracker tracker, long generation) {
        return tracker != null
                && trackers.contains(tracker)
                && !tracker.future().isDone()
                && tracker.wasDeliveredOn(generation);
    }

    private boolean removeLocked(ResponseTracker tracker) {
        if (!trackers.remove(tracker)) {
            return false;
        }

        ShetabCorrelationKey correlationKey = tracker.correlationKey();
        String exactKey = correlationKey.exactKey();

        if (exactKey != null) {
            exactIndex.remove(exactKey, tracker);
        }

        removeFromIndex(stanIndex, correlationKey.stan(), tracker);
        removeFromIndex(rrnIndex, correlationKey.rrn(), tracker);

        return true;
    }

    private void addToIndex(Map<String, Set<ResponseTracker>> index, String value, ResponseTracker tracker) {
        if (value == null) {
            return;
        }

        index.computeIfAbsent(value, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(tracker);
    }

    private void removeFromIndex(Map<String, Set<ResponseTracker>> index, String value, ResponseTracker tracker) {
        if (value == null) {
            return;
        }

        Set<ResponseTracker> indexedTrackers = index.get(value);

        if (indexedTrackers == null) {
            return;
        }

        indexedTrackers.remove(tracker);

        if (indexedTrackers.isEmpty()) {
            index.remove(value);
        }
    }

    record MatchResult(
            ShetabCorrelationKey responseKey,
            ResponseTracker tracker,
            AmbiguousShetabResponseCorrelationException ambiguity
    ) {
        static MatchResult matched(ShetabCorrelationKey responseKey, ResponseTracker tracker) {
            return new MatchResult(responseKey, tracker, null);
        }

        static MatchResult unmatched(ShetabCorrelationKey responseKey) {
            return new MatchResult(responseKey, null, null);
        }

        static MatchResult ambiguous(
                ShetabCorrelationKey responseKey,
                AmbiguousShetabResponseCorrelationException ambiguity
        ) {
            return new MatchResult(responseKey, null, ambiguity);
        }

        boolean matched() {
            return tracker != null;
        }

        boolean ambiguous() {
            return ambiguity != null;
        }
    }
}

final class ResponseTracker {
    private final ShetabCorrelationKey correlationKey;
    private final Deadline deadline;
    private final long startedNanos;
    private final CompletableFuture<ISOMsg> future = new CompletableFuture<>();
    private final ShetabProviderTraceLifecycle traceLifecycle;
    private ShetabProviderTraceLifecycle.Attempt activeAttempt;
    private volatile DeliveryPhase deliveryPhase = DeliveryPhase.QUEUED;
    private volatile long connectionGeneration = -1L;

    ResponseTracker(
            ShetabCorrelationKey correlationKey,
            Deadline deadline,
            long startedNanos,
            ShetabProviderTraceLifecycle traceLifecycle
    ) {
        this.correlationKey = Objects.requireNonNull(correlationKey, "correlationKey");
        this.deadline = Objects.requireNonNull(deadline, "deadline");
        this.startedNanos = startedNanos;
        this.traceLifecycle = traceLifecycle;
    }

    ShetabCorrelationKey correlationKey() {
        return correlationKey;
    }

    Deadline deadline() {
        return deadline;
    }

    long startedNanos() {
        return startedNanos;
    }

    CompletableFuture<ISOMsg> future() {
        return future;
    }

    DeliveryPhase deliveryPhase() {
        return deliveryPhase;
    }

    long connectionGeneration() {
        return connectionGeneration;
    }

    void markSending(long generation) {
        connectionGeneration = generation;
        deliveryPhase = DeliveryPhase.SENDING;
    }

    void markSent() {
        deliveryPhase = DeliveryPhase.SENT;
    }

    synchronized void startAttempt(String endpoint) {
        if (traceLifecycle != null && activeAttempt == null && !future.isDone()) {
            activeAttempt = traceLifecycle.startAttempt(1, endpoint);
        }
    }

    synchronized void failActiveAttempt(Throwable failure) {
        ShetabProviderTraceLifecycle.Attempt attempt = activeAttempt;
        activeAttempt = null;

        if (attempt != null) {
            attempt.finish(new ShetabProviderAttemptResult(
                    null,
                    ProviderBusinessOutcome.technicalFailure(null, failure),
                    failure
            ));
        }
    }

    synchronized ShetabProviderTraceLifecycle.Attempt releaseActiveAttempt() {
        ShetabProviderTraceLifecycle.Attempt attempt = activeAttempt;
        activeAttempt = null;
        return attempt;
    }

    boolean wasDeliveredOn(long generation) {
        return connectionGeneration == generation
                && (deliveryPhase == DeliveryPhase.SENDING || deliveryPhase == DeliveryPhase.SENT);
    }
}

enum DeliveryPhase {
    QUEUED,
    SENDING,
    SENT
}

record ShetabCorrelationKey(String stan, String rrn) {
    ShetabCorrelationKey {
        stan = clean(stan);
        rrn = clean(rrn);
    }

    static ShetabCorrelationKey from(ISOMsg msg) {
        return new ShetabCorrelationKey(safeField(msg, 11), safeField(msg, 37));
    }

    boolean hasStan() {
        return stan != null;
    }

    boolean hasRrn() {
        return rrn != null;
    }

    boolean hasAnyValue() {
        return hasStan() || hasRrn();
    }

    String exactKey() {
        if (!hasStan() || !hasRrn()) {
            return null;
        }

        return stan + "|" + rrn;
    }

    List<String> displayKeys() {
        List<String> keys = new ArrayList<>(3);
        String exactKey = exactKey();

        if (exactKey != null) {
            keys.add(exactKey);
        }

        if (stan != null) {
            keys.add(stan);
        }

        if (rrn != null) {
            keys.add("rrn:" + rrn);
        }

        return List.copyOf(keys);
    }

    String display() {
        String exactKey = exactKey();

        if (exactKey != null) {
            return exactKey;
        }

        if (stan != null) {
            return stan;
        }

        if (rrn != null) {
            return "rrn:" + rrn;
        }

        return "";
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static String safeField(ISOMsg msg, int field) {
        try {
            return msg != null ? msg.getString(field) : null;
        } catch (Exception e) {
            return null;
        }
    }
}

final class Deadline {
    private final long expiresAtNanos;

    private Deadline(long expiresAtNanos) {
        this.expiresAtNanos = expiresAtNanos;
    }

    static Deadline afterMillis(long timeoutMs) {
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(1L, timeoutMs));
        return new Deadline(System.nanoTime() + timeoutNanos);
    }

    boolean isExpired() {
        return remainingNanos() <= 0L;
    }

    long remainingNanos() {
        return expiresAtNanos - System.nanoTime();
    }

    long remainingMillisCeiling() {
        long remainingNanos = remainingNanos();

        if (remainingNanos <= 0L) {
            return 0L;
        }

        return Math.max(1L, TimeUnit.NANOSECONDS.toMillis(remainingNanos + 999_999L));
    }
}

class DuplicateShetabCorrelationException extends IllegalStateException {
    DuplicateShetabCorrelationException(String message) {
        super(message);
    }
}

class AmbiguousShetabResponseCorrelationException extends IllegalStateException {
    AmbiguousShetabResponseCorrelationException(String message) {
        super(message);
    }
}

class ShetabAmbiguousProviderDeliveryException extends IllegalStateException {
    ShetabAmbiguousProviderDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ShetabRequestDeadlineExceededException extends IllegalStateException {
    ShetabRequestDeadlineExceededException(String message) {
        super(message);
    }

    ShetabRequestDeadlineExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ShetabConnectionLostAfterSendException extends IllegalStateException {
    ShetabConnectionLostAfterSendException(String message, Throwable cause) {
        super(message, cause);
    }
}

class ShetabRequestNoLongerPendingException extends IllegalStateException {
    ShetabRequestNoLongerPendingException(String message) {
        super(message);
    }
}
