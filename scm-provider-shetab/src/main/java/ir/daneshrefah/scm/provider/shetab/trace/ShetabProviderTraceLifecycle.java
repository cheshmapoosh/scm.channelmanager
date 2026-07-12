package ir.daneshrefah.scm.provider.shetab.trace;

import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.provider.shetab.config.ShetabResolvedConfig;
import org.apache.camel.Exchange;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ShetabProviderTraceLifecycle {
    private final ShetabTraceSupport traceSupport;
    private final Exchange exchange;
    private final ShetabResolvedConfig config;
    private final ObservationScope operationScope;
    private final Map<String, Object> baseAttributes;
    private final Map<String, Object> requestAttributes;

    ShetabProviderTraceLifecycle(
            ShetabTraceSupport traceSupport,
            Exchange exchange,
            ShetabResolvedConfig config,
            ObservationScope operationScope,
            Map<String, Object> baseAttributes,
            Map<String, Object> requestAttributes
    ) {
        this.traceSupport = traceSupport;
        this.exchange = exchange;
        this.config = config;
        this.operationScope = operationScope;
        this.baseAttributes = baseAttributes == null ? Map.of() : Map.copyOf(baseAttributes);
        this.requestAttributes = requestAttributes == null ? Map.of() : Map.copyOf(requestAttributes);
    }

    public Attempt startAttempt(int attemptNumber) {
        return startAttempt(attemptNumber, Map.of());
    }

    public Attempt startAttempt(int attemptNumber, String endpoint) {
        return startAttempt(attemptNumber, traceSupport.attemptAttributes(endpoint));
    }

    public Attempt startAttempt(int attemptNumber, Map<String, Object> attemptAttributes) {
        Map<String, Object> attributes = new LinkedHashMap<>(baseAttributes);
        requestAttributes.forEach(attributes::putIfAbsent);
        if (attemptAttributes != null) {
            attemptAttributes.forEach(attributes::putIfAbsent);
        }
        put(attributes, ShetabTraceAttributes.PROVIDER_ATTEMPT.name(), Math.max(1, attemptNumber));
        put(attributes, "event.outcome", "success");
        traceSupport.addOperationEvent(operationScope, "provider.request", attributes);
        return new Attempt(this, Math.max(1, attemptNumber), System.nanoTime(), attemptAttributes);
    }

    private void finishAttempt(Attempt attempt, ShetabProviderAttemptResult result) {
        traceSupport.finishAttempt(attempt, exchange, config, result);
    }

    private void finish(
            Attempt attempt,
            ShetabProviderAttemptResult result,
            Map<String, Object> contributedAttributes
    ) {
        Map<String, Object> attributes = new LinkedHashMap<>(baseAttributes);
        if (attempt.attemptAttributes() != null) {
            attempt.attemptAttributes().forEach(attributes::putIfAbsent);
        }
        if (contributedAttributes != null) {
            contributedAttributes.forEach(attributes::putIfAbsent);
        }
        put(attributes, ShetabTraceAttributes.PROVIDER_ATTEMPT.name(), attempt.attemptNumber());
        put(attributes, ShetabTraceAttributes.PROVIDER_DURATION_MS.name(), elapsedMillis(attempt.startedAtNanos()));
        put(attributes, ShetabTraceAttributes.PROVIDER_RESPONSE_CODE.name(), clean(result.responseCode()));

        Throwable failure = result.failure();
        boolean success = failure == null && result.successfulResponse();
        put(attributes, "event.outcome", success ? "success" : "failure");
        if (!success && failure == null) {
            String errorCode = clean(result.responseCode());
            put(attributes, ShetabTraceAttributes.PROVIDER_ERROR_CODE.name(), errorCode);
            put(attributes, "error.code", errorCode);
        }
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
        private final Map<String, Object> attemptAttributes;
        private final AtomicBoolean finished = new AtomicBoolean();

        private Attempt(
                ShetabProviderTraceLifecycle lifecycle,
                int attemptNumber,
                long startedAtNanos,
                Map<String, Object> attemptAttributes
        ) {
            this.lifecycle = lifecycle;
            this.attemptNumber = attemptNumber;
            this.startedAtNanos = startedAtNanos;
            this.attemptAttributes = attemptAttributes == null ? Map.of() : Map.copyOf(attemptAttributes);
        }

        public void finish(ShetabProviderAttemptResult result) {
            lifecycle.finishAttempt(this, result);
        }

        boolean markFinished() {
            return finished.compareAndSet(false, true);
        }

        void emitFinished(ShetabProviderAttemptResult result, Map<String, Object> contributedAttributes) {
            lifecycle.finish(this, result, contributedAttributes);
        }

        private int attemptNumber() {
            return attemptNumber;
        }

        private long startedAtNanos() {
            return startedAtNanos;
        }

        private Map<String, Object> attemptAttributes() {
            return attemptAttributes;
        }
    }
}
