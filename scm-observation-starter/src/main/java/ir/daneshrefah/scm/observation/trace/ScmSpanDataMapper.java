package ir.daneshrefah.scm.observation.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import ir.daneshrefah.scm.observation.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.ObservationDocumentBuilder;
import ir.daneshrefah.scm.observation.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationRecordKind;
import ir.daneshrefah.scm.observation.ObservationSanitizer;
import ir.daneshrefah.scm.observation.attributes.ScmObservationDocumentAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmTraceAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

public class ScmSpanDataMapper {
    private static final String EVENT_ACTION = "event.action";
    private static final String EVENT_OUTCOME = "event.outcome";
    private static final String CORRELATION_ID = "correlation.id";

    private final ObservationDocumentFactory documentFactory;
    private final ObservationSanitizer sanitizer;

    public ScmSpanDataMapper(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSanitizer sanitizer
    ) {
        this.sanitizer = sanitizer;
        this.documentFactory = new ObservationDocumentFactory(context, ObservationAttributeRegistry.commonOnly(), sanitizer);
    }

    public Map<String, Object> map(SpanData spanData) {
        Instant startTime = instant(spanData.getStartEpochNanos());
        Instant endTime = instant(spanData.getEndEpochNanos());
        String eventAction = stringAttribute(spanData, EVENT_ACTION, textOrDefault(spanData.getName(), "trace.span"));
        String eventOutcome = stringAttribute(spanData, EVENT_OUTCOME, statusOutcome(spanData));
        ObservationDocumentBuilder builder = documentFactory.trace(
                ObservationRecordKind.EVENT,
                false,
                endTime,
                textOrDefault(spanData.getName(), "trace.span"),
                stringAttribute(spanData, CORRELATION_ID, ObservationIds.correlationId()),
                "operation"
        );
        builder.put(ScmObservationDocumentAttributes.EVENT_CATEGORY, "trace");
        builder.put(ScmObservationDocumentAttributes.EVENT_ACTION, textOrDefault(eventAction, textOrDefault(spanData.getName(), "trace.span")));
        builder.put(ScmObservationDocumentAttributes.EVENT_OUTCOME, textOrDefault(eventOutcome, statusOutcome(spanData)));
        builder.put(ScmTraceAttributes.TRACE_ID, spanData.getTraceId());
        builder.put(ScmTraceAttributes.SPAN_ID, spanData.getSpanId());
        putParentSpanId(builder, spanData.getParentSpanContext());
        builder.put(ScmTraceAttributes.SPAN_NAME, textOrDefault(spanData.getName(), "trace.span"));
        builder.put(ScmTraceAttributes.SPAN_KIND, spanData.getKind().name().toLowerCase(Locale.ROOT));
        builder.put(ScmTraceAttributes.SPAN_START_TIME, startTime.toString());
        builder.put(ScmTraceAttributes.SPAN_END_TIME, endTime.toString());
        builder.put(ScmTraceAttributes.SPAN_DURATION_MS, Math.max(0L, Duration.between(startTime, endTime).toMillis()));
        putSpanAttributes(builder, spanData);
        return builder.build();
    }

    private void putParentSpanId(ObservationDocumentBuilder builder, SpanContext parentSpanContext) {
        if (parentSpanContext != null && parentSpanContext.isValid()) {
            builder.put(ScmTraceAttributes.PARENT_SPAN_ID, parentSpanContext.getSpanId());
        }
    }

    private void putSpanAttributes(ObservationDocumentBuilder builder, SpanData spanData) {
        spanData.getAttributes().forEach((attributeKey, value) -> putAttribute(builder, attributeKey, value));
    }

    private void putAttribute(ObservationDocumentBuilder builder, AttributeKey<?> attributeKey, Object value) {
        if (attributeKey == null || value == null) {
            return;
        }
        String fieldName = attributeKey.getKey();
        if (TraceAttributeSecurity.isReservedTraceField(fieldName) || !TraceAttributeSecurity.isAllowed(fieldName)) {
            return;
        }
        builder.put(fieldName, value);
    }

    private String stringAttribute(SpanData spanData, String fieldName, String fallback) {
        Object value = spanData.getAttributes().asMap().get(AttributeKey.stringKey(fieldName));
        if (value == null) {
            value = spanData.getAttributes().asMap().get(AttributeKey.booleanKey(fieldName));
        }
        if (value == null) {
            value = spanData.getAttributes().asMap().get(AttributeKey.longKey(fieldName));
        }
        if (value == null) {
            value = spanData.getAttributes().asMap().get(AttributeKey.doubleKey(fieldName));
        }
        if (value == null) {
            return fallback;
        }
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(fieldName, value);
        return sanitized == null ? fallback : String.valueOf(sanitized);
    }

    private String statusOutcome(SpanData spanData) {
        if (spanData.getStatus() != null && StatusCode.ERROR.equals(spanData.getStatus().getStatusCode())) {
            return "failure";
        }
        return "success";
    }

    private Instant instant(long epochNanos) {
        long seconds = Math.floorDiv(epochNanos, 1_000_000_000L);
        long nanos = Math.floorMod(epochNanos, 1_000_000_000L);
        return Instant.ofEpochSecond(seconds, nanos);
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
