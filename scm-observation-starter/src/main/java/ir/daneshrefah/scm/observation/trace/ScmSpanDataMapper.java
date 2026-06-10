package ir.daneshrefah.scm.observation.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import ir.daneshrefah.scm.observation.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationLegacyTables;
import ir.daneshrefah.scm.observation.ObservationSanitizer;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ScmSpanDataMapper {
    private static final String EVENT_ACTION = "event.action";
    private static final String EVENT_OUTCOME = "event.outcome";
    private static final String LEGACY_ENABLED = "scm.target.legacy.enabled";
    private static final String LEGACY_TABLE = "scm.target.legacy.table";
    private static final String CORRELATION_ID = "scm.correlation_id";

    private final ObservationContext context;
    private final ObsTargetIndexResolver targetIndexResolver;
    private final ObservationSanitizer sanitizer;

    public ScmSpanDataMapper(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSanitizer sanitizer
    ) {
        this.context = context;
        this.targetIndexResolver = targetIndexResolver;
        this.sanitizer = sanitizer;
    }

    public Map<String, Object> map(SpanData spanData) {
        Instant startTime = instant(spanData.getStartEpochNanos());
        Instant endTime = instant(spanData.getEndEpochNanos());
        String eventAction = stringAttribute(spanData, EVENT_ACTION, textOrDefault(spanData.getName(), "trace.span"));
        String eventOutcome = stringAttribute(spanData, EVENT_OUTCOME, statusOutcome(spanData));
        boolean legacyEnabled = booleanAttribute(spanData, LEGACY_ENABLED, false);
        String legacyTable = stringAttribute(spanData, LEGACY_TABLE, null);
        if (legacyEnabled) {
            legacyTable = ObservationLegacyTables.validate(legacyTable);
        }

        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put("@timestamp", endTime.toString());
        document.put("event.stream", ObservationStream.TRACE.value());
        document.put("event.kind", "span");
        document.put("event.category", "trace");
        document.put(EVENT_ACTION, textOrDefault(eventAction, textOrDefault(spanData.getName(), "trace.span")));
        document.put(EVENT_OUTCOME, textOrDefault(eventOutcome, statusOutcome(spanData)));
        document.put("scm.target.index", targetIndexResolver.resolve(ObservationStream.TRACE, context, endTime));
        document.put(LEGACY_ENABLED, legacyEnabled);
        if (legacyEnabled) {
            document.put(LEGACY_TABLE, legacyTable);
        }
        document.put("scm.platform", context.platform());
        document.put("service.name", context.appName());
        document.put("deployment.environment", context.appProfile());
        document.put("scm.app.name", context.appName());
        document.put("scm.app.profile", context.appProfile());
        document.put("scm.app.label", context.appLabel());
        document.put("scm.gateway.name", context.gatewayName());
        document.put("scm.channel.code", context.channelCode());
        document.put(CORRELATION_ID, stringAttribute(spanData, CORRELATION_ID, ObservationIds.correlationId()));
        document.put("trace.id", spanData.getTraceId());
        document.put("span.id", spanData.getSpanId());
        putParentSpanId(document, spanData.getParentSpanContext());
        document.put("span.name", textOrDefault(spanData.getName(), "trace.span"));
        document.put("span.kind", spanData.getKind().name().toLowerCase(Locale.ROOT));
        document.put("span.start_time", startTime.toString());
        document.put("span.end_time", endTime.toString());
        document.put("span.duration_ms", Math.max(0L, Duration.between(startTime, endTime).toMillis()));
        putSpanAttributes(document, spanData);
        return document;
    }

    private void putParentSpanId(Map<String, Object> document, SpanContext parentSpanContext) {
        if (parentSpanContext != null && parentSpanContext.isValid()) {
            document.put("parent.span.id", parentSpanContext.getSpanId());
        }
    }

    private void putSpanAttributes(Map<String, Object> document, SpanData spanData) {
        spanData.getAttributes().forEach((attributeKey, value) -> putAttribute(document, attributeKey, value));
    }

    private void putAttribute(Map<String, Object> document, AttributeKey<?> attributeKey, Object value) {
        if (attributeKey == null || value == null) {
            return;
        }
        String fieldName = attributeKey.getKey();
        if (TraceAttributeSecurity.isReservedTraceField(fieldName) || !TraceAttributeSecurity.isAllowed(fieldName)) {
            return;
        }
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(fieldName, value);
        if (sanitized != null) {
            document.put(fieldName, sanitized);
        }
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

    private boolean booleanAttribute(SpanData spanData, String fieldName, boolean fallback) {
        Object value = spanData.getAttributes().asMap().get(AttributeKey.booleanKey(fieldName));
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        Object textValue = spanData.getAttributes().asMap().get(AttributeKey.stringKey(fieldName));
        if (textValue instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text.trim());
        }
        return fallback;
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
