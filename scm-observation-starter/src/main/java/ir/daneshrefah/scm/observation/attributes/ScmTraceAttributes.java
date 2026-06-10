package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmTraceAttributes {
    private ScmTraceAttributes() {
    }

    public static final ObservationAttributeKey<String> TRACE_ID = ObservationAttributeKey.stringKey("trace.id", true, "Trace identifier");
    public static final ObservationAttributeKey<String> SPAN_ID = ObservationAttributeKey.stringKey("span.id", true, "Span identifier");
    public static final ObservationAttributeKey<String> PARENT_SPAN_ID = ObservationAttributeKey.stringKey("parent.span.id", true, "Parent span identifier");
    public static final ObservationAttributeKey<String> SPAN_NAME = ObservationAttributeKey.stringKey("span.name", true, "Span name");
    public static final ObservationAttributeKey<String> SPAN_KIND = ObservationAttributeKey.stringKey("span.kind", true, "Span kind");
    public static final ObservationAttributeKey<String> SPAN_START_TIME = ObservationAttributeKey.stringKey("span.start_time", true, "Span start time");
    public static final ObservationAttributeKey<String> SPAN_END_TIME = ObservationAttributeKey.stringKey("span.end_time", true, "Span end time");
    public static final ObservationAttributeKey<Long> SPAN_DURATION_MS = ObservationAttributeKey.longKey("span.duration_ms", true, "Span duration in milliseconds");
}
