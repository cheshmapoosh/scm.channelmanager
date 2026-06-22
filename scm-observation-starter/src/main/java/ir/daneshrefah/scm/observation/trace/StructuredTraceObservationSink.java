package ir.daneshrefah.scm.observation.trace;

import ir.daneshrefah.scm.observation.ObservationDocumentBuilder;
import ir.daneshrefah.scm.observation.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.ObservationEventSignal;
import ir.daneshrefah.scm.observation.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationRecordKind;
import ir.daneshrefah.scm.observation.attributes.ScmErrorAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmObservationDocumentAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmOperationAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmTraceAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StructuredTraceObservationSink implements TraceObservationSink {
    private final ObservationEventDispatcher eventDispatcher;
    private final ObservationDocumentFactory documentFactory;
    private final Clock clock;

    public StructuredTraceObservationSink(
            ObservationEventDispatcher eventDispatcher,
            ObservationDocumentFactory documentFactory,
            Clock clock
    ) {
        this.eventDispatcher = eventDispatcher;
        this.documentFactory = documentFactory;
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
            LinkedHashMap<String, Object> document = document(endedAt, outcome, attributes, throwable);
            eventDispatcher.write(new ir.daneshrefah.scm.observation.ObservationEvent(
                    ObservationEventSignal.TRACE,
                    spec.sourceClass(),
                    document
            ));
        }

        private LinkedHashMap<String, Object> document(
                Instant endedAt,
                String requestedOutcome,
                Map<String, Object> attributes,
                Throwable throwable
        ) {
            String outcome = textOrDefault(requestedOutcome, textOrDefault(spec.outcome(), throwable == null ? "success" : "failure"));
            ObservationDocumentBuilder builder = documentFactory.trace(
                    ObservationRecordKind.EVENT,
                    throwable != null,
                    endedAt,
                    textOrDefault(spec.spanName(), "trace.span"),
                    spec.correlationId(),
                    "operation"
            );
            builder.put(ScmObservationDocumentAttributes.EVENT_CATEGORY, "trace");
            builder.put(ScmObservationDocumentAttributes.EVENT_ACTION, textOrDefault(spec.action(), textOrDefault(spec.spanName(), "trace.span")));
            builder.put(ScmObservationDocumentAttributes.EVENT_OUTCOME, outcome);
            builder.put(ScmTraceAttributes.TRACE_ID, textOrDefault(spec.traceId(), ObservationIds.traceId()));
            builder.put(ScmTraceAttributes.SPAN_ID, textOrDefault(spec.spanId(), ObservationIds.spanId()));
            builder.put(ScmTraceAttributes.PARENT_SPAN_ID, spec.parentSpanId());
            builder.put(ScmTraceAttributes.SPAN_NAME, textOrDefault(spec.spanName(), "trace.span"));
            builder.put(ScmTraceAttributes.SPAN_KIND, textOrDefault(spec.spanKind(), "internal").toLowerCase(Locale.ROOT));
            builder.put(ScmTraceAttributes.SPAN_START_TIME, startedAt.toString());
            builder.put(ScmTraceAttributes.SPAN_END_TIME, endedAt.toString());
            builder.put(ScmTraceAttributes.SPAN_DURATION_MS, Math.max(0L, Duration.between(startedAt, endedAt).toMillis()));
            builder.put(ScmOperationAttributes.DURATION_MS, Math.max(0L, Duration.ofNanos(System.nanoTime() - startedNanos).toMillis()));
            builder.putAll(spec.attributes());
            builder.putAll(attributes);
            if (throwable != null) {
                builder.put(ScmErrorAttributes.TYPE, throwable.getClass().getName());
                builder.put(ScmErrorAttributes.MESSAGE, safeMessage(throwable));
            }
            return builder.build();
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
