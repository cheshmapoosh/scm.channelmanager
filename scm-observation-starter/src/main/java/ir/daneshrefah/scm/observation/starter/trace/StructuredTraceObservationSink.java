package ir.daneshrefah.scm.observation.starter.trace;

import ir.daneshrefah.scm.observation.starter.*;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StructuredTraceObservationSink implements TraceObservationSink {
    private final ObservationEventDispatcher eventDispatcher;
    private final ObservationDocumentFactory documentFactory;
    private final ObservationRecordValidator recordValidator;
    private final Clock clock;

    public StructuredTraceObservationSink(
            ObservationEventDispatcher eventDispatcher,
            ObservationDocumentFactory documentFactory,
            ObservationRecordValidator recordValidator,
            Clock clock
    ) {
        this.eventDispatcher = eventDispatcher;
        this.documentFactory = documentFactory;
        this.recordValidator = recordValidator;
        this.clock = clock;
    }

    @Override
    public TraceObservationHandle start(TraceObservationSpec spec) {
        if (spec == null) {
            return TraceObservationHandle.NOOP;
        }
        long startedAtNanos = System.nanoTime();
        return new StructuredTraceObservationHandle(spec, Instant.now(clock), startedAtNanos);
    }

    private final class StructuredTraceObservationHandle implements TraceObservationHandle {
        private final TraceObservationSpec spec;
        private final Instant startedAt;
        private final long startedAtNanos;
        private final List<TraceObservationSpanEvent> events = new ArrayList<>();
        private boolean finished;

        private StructuredTraceObservationHandle(TraceObservationSpec spec, Instant startedAt, long startedAtNanos) {
            this.spec = spec;
            this.startedAt = startedAt;
            this.startedAtNanos = startedAtNanos;
        }

        @Override
        public void finish(String outcome, Map<String, Object> attributes, Throwable throwable) {
            Instant endedAt;
            long durationMs;
            List<TraceObservationSpanEvent> recordedEvents;
            synchronized (this) {
                if (finished) {
                    return;
                }
                finished = true;
                long endedAtNanos = System.nanoTime();
                endedAt = Instant.now(clock);
                durationMs = elapsedMillis(startedAtNanos, endedAtNanos);
                recordedEvents = List.copyOf(events);
            }
            LinkedHashMap<String, Object> document = document(
                    endedAt, durationMs, recordedEvents, outcome, attributes, throwable);
            eventDispatcher.write(new ObservationEvent(
                    ObservationEventSignal.TRACE,
                    spec.sourceClass(),
                    document
            ));
        }

        @Override
        public TraceContext traceContext() {
            return new TraceContext(
                    spec.traceId(),
                    spec.spanId(),
                    spec.correlationId(),
                    spec.correlationType()
            );
        }

        @Override
        public void event(String name, Map<String, ?> attributes) {
            synchronized (this) {
                String safeName = safeEventName(name);
                if (finished || safeName == null) {
                    return;
                }
                events.add(new TraceObservationSpanEvent(
                        safeName,
                        Instant.now(clock),
                        safeEventAttributes(attributes)
                ));
            }
        }

        private LinkedHashMap<String, Object> document(
                Instant endedAt,
                long durationMs,
                List<TraceObservationSpanEvent> recordedEvents,
                String requestedOutcome,
                Map<String, Object> attributes,
                Throwable throwable
        ) {
            String outcome = textOrDefault(requestedOutcome, textOrDefault(spec.outcome(), throwable == null ? "success" : "failure"));
            ObservationDocumentBuilder builder = documentFactory.trace(
                    endedAt,
                    textOrDefault(spec.spanName(), "trace.span"),
                    spec.correlationId(),
                    spec.correlationType()
            );
            putSpanAttributes(builder, spec.attributes());
            putSpanAttributes(builder, attributes);
            builder.put(CommonTraceAttributes.TIMESTAMP, endedAt.toString());
            builder.put(CommonTraceAttributes.MESSAGE, textOrDefault(spec.spanName(), "trace.span"));
            builder.put(CommonTraceAttributes.EVENT_ACTION, textOrDefault(spec.action(), textOrDefault(spec.spanName(), "trace.span")));
            builder.put(CommonTraceAttributes.EVENT_OUTCOME, outcome);
            builder.put(CommonTraceAttributes.TRACE_ID, textOrDefault(spec.traceId(), ObservationIds.traceId()));
            builder.put(CommonTraceAttributes.SPAN_ID, textOrDefault(spec.spanId(), ObservationIds.spanId()));
            builder.put(CommonTraceAttributes.PARENT_SPAN_ID, textOrNull(spec.parentSpanId()));
            builder.put(CommonTraceAttributes.SPAN_NAME, textOrDefault(spec.spanName(), "trace.span"));
            builder.put(CommonTraceAttributes.SPAN_KIND, textOrDefault(spec.spanKind(), "internal").toLowerCase(Locale.ROOT));
            builder.put(CommonTraceAttributes.SPAN_START_TIME, startedAt.toString());
            builder.put(CommonTraceAttributes.SPAN_END_TIME, endedAt.toString());
            builder.put(CommonTraceAttributes.SPAN_DURATION_MS, durationMs);
            if (recordedEvents != null && !recordedEvents.isEmpty()) {
                builder.put(CommonTraceAttributes.SPAN_EVENTS, recordedEvents.stream()
                        .map(TraceObservationSpanEvent::toDocument)
                        .toList());
            }
            if (throwable != null) {
                builder.put(CommonTraceAttributes.ERROR_TYPE, throwable.getClass().getName());
                builder.put(CommonTraceAttributes.ERROR_MESSAGE, safeMessage(throwable));
            }
            LinkedHashMap<String, Object> document = builder.build();
            recordValidator.validate(ObservationStream.TRACE, ObservationRecordKind.EVENT, throwable != null, document);
            return document;
        }

        private void putSpanAttributes(ObservationDocumentBuilder builder, Map<String, ?> attributes) {
            if (attributes == null || attributes.isEmpty()) {
                return;
            }
            attributes.forEach((name, value) -> {
                if (value != null
                        && (isGatewaySpan() || !TraceAttributeSecurity.isGatewayOnlyJwtContextField(name))
                        && !TraceAttributeSecurity.isReservedTraceField(name)
                        && TraceAttributeSecurity.isAllowed(name)) {
                    builder.put(name, value);
                }
            });
        }

        private Map<String, Object> safeEventAttributes(Map<String, ?> attributes) {
            if (attributes == null || attributes.isEmpty()) {
                return Map.of();
            }
            ObservationDocumentBuilder eventAttributes = documentFactory.builder(ObservationStream.TRACE);
            attributes.forEach((name, value) -> {
                if (value != null && TraceAttributeSecurity.isAllowedSpanEventAttribute(name)) {
                    eventAttributes.put(name, value);
                }
            });
            return new LinkedHashMap<>(eventAttributes.snapshot());
        }

        private boolean isGatewaySpan() {
            return "gateway.receive".equals(textOrNull(spec.spanName()));
        }

        private String safeEventName(String name) {
            if (name == null || name.isBlank()) {
                return null;
            }
            String safeName = name.replace('\r', ' ').replace('\n', ' ').trim();
            if (safeName.isEmpty()) {
                return null;
            }
            return safeName.length() > 128 ? safeName.substring(0, 128) : safeName;
        }

        private long elapsedMillis(long startNanos, long endNanos) {
            return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, endNanos - startNanos));
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

        private String textOrNull(String value) {
            return value == null || value.isBlank() ? null : value.trim();
        }
    }
}
