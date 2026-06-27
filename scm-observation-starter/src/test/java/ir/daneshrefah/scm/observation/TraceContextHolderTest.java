package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.trace.StructuredTraceObservationSink;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TraceContextHolderTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clearContext() {
        TraceContextHolder.clear();
    }

    @Test
    void rootAndChildTraceContextPropagatesAndRestores() {
        List<ObservationEvent> events = new ArrayList<>();
        ScmObservation observation = observation(events);

        ObservationScope root = observation.trace()
                .span("root")
                .correlationId("correlation-1")
                .start();
        TraceContext rootContext = TraceContextHolder.current();
        assertNotNull(rootContext);

        ObservationScope child = observation.trace()
                .span("child")
                .start();
        TraceContext childContext = TraceContextHolder.current();
        assertNotNull(childContext);
        assertEquals(rootContext.traceId(), childContext.traceId());
        assertNotEquals(rootContext.spanId(), childContext.spanId());

        child.success().close();
        assertEquals(rootContext, TraceContextHolder.current());

        root.success().close();
        assertNull(TraceContextHolder.current());

        Map<String, Object> rootDocument = document(events, "root");
        Map<String, Object> childDocument = document(events, "child");

        assertFalse(rootDocument.containsKey("parent.span.id"));
        assertEquals(rootDocument.get("trace.id"), childDocument.get("trace.id"));
        assertEquals(rootDocument.get("span.id"), childDocument.get("parent.span.id"));
    }

    private ScmObservation observation(List<ObservationEvent> events) {
        ObservationSignalPolicy signalPolicy = signal -> signal == ObservationSignal.TRACE;
        ObservationEventSink sink = new ObservationEventSink() {
            @Override
            public ObservationEventSignal signal() {
                return ObservationEventSignal.TRACE;
            }

            @Override
            public void write(ObservationEvent event) {
                events.add(event);
            }
        };
        ObservationEventDispatcher dispatcher = new ObservationEventDispatcher(signalPolicy, List.of(sink));
        ObservationAttributeRegistry registry = ObservationAttributeRegistry.commonOnly();
        ObservationSanitizer sanitizer = (fieldName, value) -> value;
        ObservationDocumentFactory documentFactory = new ObservationDocumentFactory(null, registry, sanitizer);
        ObservationRecordValidator validator = new ObservationRecordValidator(registry);
        return new ScmObservation(
                null,
                new ObsTargetIndexResolver(),
                signalPolicy,
                dispatcher,
                null,
                new StructuredTraceObservationSink(dispatcher, documentFactory, validator, CLOCK),
                sanitizer,
                documentFactory,
                validator,
                CLOCK
        );
    }

    private Map<String, Object> document(List<ObservationEvent> events, String spanName) {
        return events.stream()
                .map(ObservationEvent::document)
                .filter(document -> spanName.equals(document.get("span.name")))
                .findFirst()
                .orElseThrow();
    }
}
