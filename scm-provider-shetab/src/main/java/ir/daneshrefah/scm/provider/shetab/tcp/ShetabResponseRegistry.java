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

    boolean beginSend(ResponseTracker tracker, long generation) {
        synchronized (lock) {
            if (!trackers.contains(tracker) || tracker.future().isDone()) {
                return false;
            }

            return tracker.tryBeginSend(generation);
        }
    }

    boolean markSent(ResponseTracker tracker) {
        return tracker != null && tracker.markSent();
    }

    MatchResult match(ShetabCorrelationKey responseKey, long generation) {
        Objects.requireNonNull(responseKey, "responseKey");

        synchronized (lock) {
            if (responseKey.hasStan() && responseKey.hasRrn()) {
                ResponseTracker tracker = exactIndex.get(responseKey.exactKey());

                if (!canMatchGeneration(tracker, generation)) {
                    return MatchResult.unmatched(responseKey);
                }

                if (!tracker.claimResponse()) {
                    removeLocked(tracker);
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
        if (!completeFailure(tracker, error)) {
            return false;
        }
        remove(tracker);
        return true;
    }

    boolean completeFailure(ResponseTracker tracker, Throwable error) {
        if (tracker == null || !tracker.claimFailure()) {
            return false;
        }
        tracker.failActiveAttempt(error);
        return tracker.future().completeExceptionally(error);
    }

    TimeoutClaim timeout(ResponseTracker tracker, Throwable error) {
        if (tracker == null) {
            return TimeoutClaim.notClaimed();
        }

        ResponseTracker.TimeoutTransition transition = tracker.claimTimeout();
        if (!transition.claimed()) {
            return TimeoutClaim.notClaimed();
        }

        remove(tracker);
        tracker.failActiveAttempt(error);
        tracker.future().completeExceptionally(error);
        return new TimeoutClaim(
                true,
                transition.deliveryStarted(),
                transition.phase(),
                transition.generation()
        );
    }

    List<ResponseTracker> removeByGeneration(long generation) {
        List<ResponseTracker> failed = new ArrayList<>();

        synchronized (lock) {
            for (ResponseTracker tracker : new ArrayList<>(trackers)) {
                if (tracker.belongsToGeneration(generation)) {
                    removeLocked(tracker);
                    failed.add(tracker);
                }
            }
        }

        return failed;
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
        if (!tracker.claimResponse()) {
            removeLocked(tracker);
            return MatchResult.unmatched(responseKey);
        }
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
                && tracker.canCompleteResponseOn(generation);
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

    record TimeoutClaim(
            boolean claimed,
            boolean deliveryStarted,
            DeliveryPhase phase,
            long generation
    ) {
        static TimeoutClaim notClaimed() {
            return new TimeoutClaim(false, false, null, -1L);
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
    private boolean terminal;

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

    synchronized DeliveryPhase deliveryPhase() {
        return deliveryPhase;
    }

    synchronized long connectionGeneration() {
        return connectionGeneration;
    }

    synchronized boolean tryBeginSend(long generation) {
        if (future.isDone() || terminal || deliveryPhase != DeliveryPhase.QUEUED) {
            return false;
        }
        connectionGeneration = generation;
        deliveryPhase = DeliveryPhase.SENDING;
        return true;
    }

    synchronized void markAdmitted(long generation) {
        if (terminal) {
            return;
        }
        connectionGeneration = generation;
        deliveryPhase = DeliveryPhase.QUEUED;
    }

    synchronized boolean markSent() {
        if (terminal || deliveryPhase != DeliveryPhase.SENDING) {
            return false;
        }
        deliveryPhase = DeliveryPhase.SENT;
        return true;
    }

    synchronized void startAttempt(String endpoint) {
        if (traceLifecycle != null && activeAttempt == null && !terminal && !future.isDone()) {
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

    synchronized boolean canCompleteResponseOn(long generation) {
        return !terminal && wasDeliveredOn(generation);
    }

    synchronized boolean wasDeliveredOn(long generation) {
        return connectionGeneration == generation
                && (deliveryPhase == DeliveryPhase.SENDING || deliveryPhase == DeliveryPhase.SENT);
    }

    synchronized boolean belongsToGeneration(long generation) {
        return connectionGeneration == generation;
    }

    synchronized boolean deliveryStarted() {
        return deliveryPhase == DeliveryPhase.SENDING || deliveryPhase == DeliveryPhase.SENT;
    }

    synchronized boolean claimResponse() {
        if (terminal || !wasDeliveredOn(connectionGeneration)) {
            return false;
        }
        terminal = true;
        return true;
    }

    synchronized boolean claimFailure() {
        if (terminal) {
            return false;
        }
        terminal = true;
        return true;
    }

    synchronized TimeoutTransition claimTimeout() {
        if (terminal) {
            return TimeoutTransition.notClaimed();
        }

        DeliveryPhase currentPhase = deliveryPhase;
        long currentGeneration = connectionGeneration;
        boolean deliveryStarted = currentPhase == DeliveryPhase.SENDING || currentPhase == DeliveryPhase.SENT;
        terminal = true;
        if (!deliveryStarted) {
            deliveryPhase = DeliveryPhase.TIMED_OUT_BEFORE_SEND;
        }

        return new TimeoutTransition(true, deliveryStarted, currentPhase, currentGeneration);
    }

    record TimeoutTransition(
            boolean claimed,
            boolean deliveryStarted,
            DeliveryPhase phase,
            long generation
    ) {
        static TimeoutTransition notClaimed() {
            return new TimeoutTransition(false, false, null, -1L);
        }
    }
}

enum DeliveryPhase {
    QUEUED,
    TIMED_OUT_BEFORE_SEND,
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

class ShetabConnectionUnavailableException extends IllegalStateException {
    ShetabConnectionUnavailableException(String message) {
        super(message);
    }
}

class ShetabRequestNoLongerPendingException extends IllegalStateException {
    ShetabRequestNoLongerPendingException(String message) {
        super(message);
    }
}
