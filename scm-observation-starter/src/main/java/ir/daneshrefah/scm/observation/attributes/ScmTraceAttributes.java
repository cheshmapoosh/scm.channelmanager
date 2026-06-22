package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.util.List;

public final class ScmTraceAttributes {
    private ScmTraceAttributes() {
    }

    public static final ObservationAttributeKey<String> TRACE_ID = ObservationAttributeKey.stringKey(
            "trace.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Trace identifier", ObservationStream.TRACE);
    public static final ObservationAttributeKey<String> SPAN_ID = ObservationAttributeKey.stringKey(
            "span.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Span identifier", ObservationStream.TRACE);
    public static final ObservationAttributeKey<String> PARENT_SPAN_ID = ObservationAttributeKey.stringKey(
            "parent.span.id", ObservationAttributePresence.EVENT_OPTIONAL, "Parent span identifier", ObservationStream.TRACE);
    public static final ObservationAttributeKey<String> SPAN_NAME = ObservationAttributeKey.stringKey(
            "span.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Span name", ObservationStream.TRACE);
    public static final ObservationAttributeKey<String> SPAN_KIND = ObservationAttributeKey.stringKey(
            "span.kind", ObservationAttributePresence.ALWAYS_REQUIRED, "Span kind", ObservationStream.TRACE);
    public static final ObservationAttributeKey<String> SPAN_START_TIME = ObservationAttributeKey.stringKey(
            "span.start_time", ObservationAttributePresence.ALWAYS_REQUIRED, "Span start time", ObservationStream.TRACE);
    public static final ObservationAttributeKey<String> SPAN_END_TIME = ObservationAttributeKey.stringKey(
            "span.end_time", ObservationAttributePresence.ALWAYS_REQUIRED, "Span end time", ObservationStream.TRACE);
    public static final ObservationAttributeKey<Long> SPAN_DURATION_MS = ObservationAttributeKey.longKey(
            "span.duration_ms", ObservationAttributePresence.ALWAYS_REQUIRED, "Span duration in milliseconds", ObservationStream.TRACE);

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                TRACE_ID,
                SPAN_ID,
                PARENT_SPAN_ID,
                SPAN_NAME,
                SPAN_KIND,
                SPAN_START_TIME,
                SPAN_END_TIME,
                SPAN_DURATION_MS
        );
    }
}
