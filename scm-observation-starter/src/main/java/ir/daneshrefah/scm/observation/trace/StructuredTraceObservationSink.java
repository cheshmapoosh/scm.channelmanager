package ir.daneshrefah.scm.observation.trace;

import ir.daneshrefah.scm.observation.ObservationDocumentBuilder;
import ir.daneshrefah.scm.observation.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.ObservationEventSignal;
import ir.daneshrefah.scm.observation.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationRecordKind;
import ir.daneshrefah.scm.observation.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.ObservationStream;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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
        return new StructuredTraceObservationHandle(spec, Instant.now(clock));
    }

    private final class StructuredTraceObservationHandle implements TraceObservationHandle {
        private final TraceObservationSpec spec;
        private final Instant startedAt;
        private boolean finished;

        private StructuredTraceObservationHandle(TraceObservationSpec spec, Instant startedAt) {
            this.spec = spec;
            this.startedAt = startedAt;
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
                    endedAt,
                    textOrDefault(spec.spanName(), "trace.span"),
                    spec.correlationId(),
                    spec.correlationType()
            );
            builder.put(CommonTraceAttributes.EVENT_CATEGORY, "trace");
            builder.put(CommonTraceAttributes.EVENT_ACTION, textOrDefault(spec.action(), textOrDefault(spec.spanName(), "trace.span")));
            builder.put(CommonTraceAttributes.EVENT_OUTCOME, outcome);
            builder.put(CommonTraceAttributes.TRACE_ID, textOrDefault(spec.traceId(), ObservationIds.traceId()));
            builder.put(CommonTraceAttributes.SPAN_ID, textOrDefault(spec.spanId(), ObservationIds.spanId()));
            builder.put(CommonTraceAttributes.PARENT_SPAN_ID, textOrNull(spec.parentSpanId()));
            builder.put(CommonTraceAttributes.SPAN_NAME, textOrDefault(spec.spanName(), "trace.span"));
            builder.put(CommonTraceAttributes.SPAN_KIND, textOrDefault(spec.spanKind(), "internal").toLowerCase(Locale.ROOT));
            builder.put(CommonTraceAttributes.SPAN_START_TIME, startedAt.toString());
            builder.put(CommonTraceAttributes.SPAN_END_TIME, endedAt.toString());
            builder.put(CommonTraceAttributes.SPAN_DURATION_MS, Math.max(0L, Duration.between(startedAt, endedAt).toMillis()));
            builder.putAll(spec.attributes());
            builder.putAll(attributes);
            if (throwable != null) {
                builder.put(CommonTraceAttributes.ERROR_TYPE, throwable.getClass().getName());
                builder.put(CommonTraceAttributes.ERROR_MESSAGE, safeMessage(throwable));
            }
            LinkedHashMap<String, Object> document = builder.build();
            recordValidator.validate(ObservationStream.TRACE, ObservationRecordKind.EVENT, throwable != null, document);
            return document;
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
