package ir.daneshrefah.scm.observation.trace;

import ir.daneshrefah.scm.observation.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationEventSignal;
import ir.daneshrefah.scm.observation.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationLegacyTables;
import ir.daneshrefah.scm.observation.ObservationSanitizer;
import ir.daneshrefah.scm.observation.ObservationStream;
import ir.daneshrefah.scm.observation.attributes.ScmErrorAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StructuredTraceObservationSink implements TraceObservationSink {
    private final ObservationContext context;
    private final ObsTargetIndexResolver targetIndexResolver;
    private final ObservationEventDispatcher eventDispatcher;
    private final ObservationSanitizer sanitizer;
    private final Clock clock;

    public StructuredTraceObservationSink(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationEventDispatcher eventDispatcher,
            ObservationSanitizer sanitizer,
            Clock clock
    ) {
        this.context = context;
        this.targetIndexResolver = targetIndexResolver;
        this.eventDispatcher = eventDispatcher;
        this.sanitizer = sanitizer;
        this.clock = clock;
    }

    @Override
    public TraceObservationHandle start(TraceObservationSpec spec) {
        if (spec == null) {
            return TraceObservationHandle.NOOP;
        }
        return new StructuredTraceObservationHandle(spec, Instant.now(clock), System.nanoTime());
    }

    private final class StructuredTraceObservationHandle implements TraceObservationHandle {
        private final TraceObservationSpec spec;
        private final Instant startedAt;
        private final long startedNanos;
        private boolean finished;

        private StructuredTraceObservationHandle(TraceObservationSpec spec, Instant startedAt, long startedNanos) {
            this.spec = spec;
            this.startedAt = startedAt;
            this.startedNanos = startedNanos;
        }

        @Override
        public void finish(String outcome, Map<String, Object> attributes, Throwable throwable) {
            if (finished) {
                return;
            }
            finished = true;
            Instant endedAt = Instant.now(clock);
            LinkedHashMap<String, Object> document = baseDocument(endedAt, outcome, throwable);
            putAttributes(document, spec.attributes());
            putAttributes(document, attributes);
            putThrowable(document, throwable);
            eventDispatcher.write(new ir.daneshrefah.scm.observation.ObservationEvent(
                    ObservationEventSignal.TRACE,
                    spec.sourceClass(),
                    document
            ));
        }

        private LinkedHashMap<String, Object> baseDocument(Instant endedAt, String requestedOutcome, Throwable throwable) {
            LinkedHashMap<String, Object> document = new LinkedHashMap<>();
            boolean legacyEnabled = spec.legacyEnabled();
            String legacyTable = legacyEnabled ? ObservationLegacyTables.validate(spec.legacyTable()) : null;
            String outcome = textOrDefault(requestedOutcome, textOrDefault(spec.outcome(), throwable == null ? "success" : "failure"));
            document.put("@timestamp", endedAt.toString());
            document.put("event.stream", ObservationStream.TRACE.value());
            document.put("event.kind", "span");
            document.put("event.category", "trace");
            document.put("event.action", textOrDefault(spec.action(), textOrDefault(spec.spanName(), "trace.span")));
            document.put("event.outcome", outcome);
            document.put("scm.target.index", targetIndexResolver.resolve(ObservationStream.TRACE, context, endedAt));
            document.put("scm.target.legacy.enabled", legacyEnabled);
            if (legacyEnabled) {
                document.put("scm.target.legacy.table", legacyTable);
            }
            document.put("scm.platform", context.platform());
            document.put("service.name", context.appName());
            document.put("deployment.environment", context.appProfile());
            document.put("scm.app.name", context.appName());
            document.put("scm.app.profile", context.appProfile());
            document.put("scm.app.label", context.appLabel());
            document.put("scm.gateway.name", context.gatewayName());
            document.put("scm.channel.code", context.channelCode());
            document.put("scm.correlation_id", textOrDefault(spec.correlationId(), ObservationIds.correlationId()));
            document.put("trace.id", textOrDefault(spec.traceId(), ObservationIds.traceId()));
            document.put("span.id", textOrDefault(spec.spanId(), ObservationIds.spanId()));
            if (spec.parentSpanId() != null && !spec.parentSpanId().isBlank()) {
                document.put("parent.span.id", spec.parentSpanId().trim());
            }
            document.put("span.name", textOrDefault(spec.spanName(), "trace.span"));
            document.put("span.kind", textOrDefault(spec.spanKind(), "internal").toLowerCase(Locale.ROOT));
            document.put("span.start_time", startedAt.toString());
            document.put("span.end_time", endedAt.toString());
            document.put("span.duration_ms", Math.max(0L, Duration.between(startedAt, endedAt).toMillis()));
            document.put("scm.operation.duration_ms", Math.max(0L, Duration.ofNanos(System.nanoTime() - startedNanos).toMillis()));
            return document;
        }

        private void putThrowable(Map<String, Object> document, Throwable throwable) {
            if (throwable == null) {
                return;
            }
            putAttribute(document, ScmErrorAttributes.TYPE.name(), throwable.getClass().getName());
            putAttribute(document, ScmErrorAttributes.MESSAGE.name(), safeMessage(throwable));
        }

        private void putAttributes(Map<String, Object> document, Map<String, Object> attributes) {
            if (attributes == null) {
                return;
            }
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                putAttribute(document, entry.getKey(), entry.getValue());
            }
        }

        private void putAttribute(Map<String, Object> document, String fieldName, Object value) {
            if (fieldName == null || fieldName.isBlank() || value == null) {
                return;
            }
            if (!TraceAttributeSecurity.isAllowed(fieldName) && !TraceAttributeSecurity.isReservedTraceField(fieldName)) {
                return;
            }
            Object sanitized = sanitizer == null ? value : sanitizer.sanitize(fieldName.trim(), value);
            if (sanitized != null) {
                document.put(fieldName.trim(), sanitized);
            }
        }

        private String safeMessage(Throwable throwable) {
            if (throwable == null || throwable.getMessage() == null) {
                return null;
            }
            String message = throwable.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
            return message.length() > 300 ? message.substring(0, 300) : message;
        }

        private String textOrDefault(String value, String defaultValue) {
            return value == null || value.isBlank() ? defaultValue : value.trim();
        }
    }
}
