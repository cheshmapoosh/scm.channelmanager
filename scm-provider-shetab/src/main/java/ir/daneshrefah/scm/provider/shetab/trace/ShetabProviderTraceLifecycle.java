package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.ObservationScope;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ShetabProviderTraceLifecycle {
    private final ShetabTraceSupport traceSupport;
    private final ObservationScope operationScope;
    private final Map<String, Object> baseAttributes;
    private final Map<String, Object> requestAttributes;

    ShetabProviderTraceLifecycle(
            ShetabTraceSupport traceSupport,
            ObservationScope operationScope,
            Map<String, Object> baseAttributes,
            Map<String, Object> requestAttributes
    ) {
        this.traceSupport = traceSupport;
        this.operationScope = operationScope;
        this.baseAttributes = Map.copyOf(baseAttributes);
        this.requestAttributes = Map.copyOf(requestAttributes);
    }

    public Attempt startAttempt(int attemptNumber) {
        Map<String, Object> attributes = new LinkedHashMap<>(baseAttributes);
        requestAttributes.forEach(attributes::putIfAbsent);
        put(attributes, ShetabTraceAttributes.PROVIDER_ATTEMPT.name(), Math.max(1, attemptNumber));
        put(attributes, "event.outcome", "success");
        traceSupport.addOperationEvent(operationScope, "provider.request", attributes);
        return new Attempt(this, Math.max(1, attemptNumber), System.nanoTime());
    }

    private void finish(
            Attempt attempt,
            String responseCode,
            boolean successfulResponse,
            Throwable failure,
            Map<String, Object> contributedAttributes
    ) {
        Map<String, Object> attributes = new LinkedHashMap<>(baseAttributes);
        if (contributedAttributes != null) {
            contributedAttributes.forEach(attributes::putIfAbsent);
        }
        put(attributes, ShetabTraceAttributes.PROVIDER_ATTEMPT.name(), attempt.attemptNumber());
        put(attributes, ShetabTraceAttributes.PROVIDER_DURATION_MS.name(), elapsedMillis(attempt.startedAtNanos()));
        put(attributes, ShetabTraceAttributes.PROVIDER_RESPONSE_CODE.name(), clean(responseCode));

        boolean success = failure == null && successfulResponse;
        put(attributes, "event.outcome", success ? "success" : "failure");
        if (failure != null) {
            String errorCode = errorCode(failure);
            put(attributes, ShetabTraceAttributes.PROVIDER_ERROR_CODE.name(), errorCode);
            put(attributes, "error.type", failure.getClass().getSimpleName());
            put(attributes, "error.code", errorCode);
        }
        traceSupport.addOperationEvent(operationScope, "provider.response", attributes);
    }

    private static long elapsedMillis(long startedAtNanos) {
        return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, System.nanoTime() - startedAtNanos));
    }

    private static String errorCode(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current.getClass().getSimpleName().contains("Timeout")) {
                return "PROVIDER_TIMEOUT";
            }
            current = current.getCause();
        }
        return failure == null ? null : failure.getClass().getSimpleName();
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void put(Map<String, Object> attributes, String name, Object value) {
        if (name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
    }

    public static final class Attempt {
        private final ShetabProviderTraceLifecycle lifecycle;
        private final int attemptNumber;
        private final long startedAtNanos;
        private final AtomicBoolean finished = new AtomicBoolean();

        private Attempt(ShetabProviderTraceLifecycle lifecycle, int attemptNumber, long startedAtNanos) {
            this.lifecycle = lifecycle;
            this.attemptNumber = attemptNumber;
            this.startedAtNanos = startedAtNanos;
        }

        public void finish(
                String responseCode,
                boolean successfulResponse,
                Throwable failure,
                Map<String, Object> contributedAttributes
        ) {
            if (finished.compareAndSet(false, true)) {
                lifecycle.finish(this, responseCode, successfulResponse, failure, contributedAttributes);
            }
        }

        public void transportFailure(Throwable failure) {
            finish(null, false, failure, Map.of());
        }

        private int attemptNumber() {
            return attemptNumber;
        }

        private long startedAtNanos() {
            return startedAtNanos;
        }
    }
}
