package ir.daneshrefah.scm.observation.starter.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import ir.daneshrefah.scm.observation.starter.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentBuilder;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.starter.ObservationIds;
import ir.daneshrefah.scm.observation.starter.ObservationRecordKind;
import ir.daneshrefah.scm.observation.starter.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ScmSpanDataMapper {
    private static final String EVENT_ACTION = "event.action";
    private static final String EVENT_OUTCOME = "event.outcome";
    private static final String CORRELATION_ID = "correlation.id";
    private static final String CORRELATION_TYPE = "correlation.type";
    private static final String SPAN_DURATION_MS = "span.duration_ms";

    private final ObservationDocumentFactory documentFactory;
    private final ObservationRecordValidator recordValidator;
    private final ObservationSanitizer sanitizer;
    private final ObservationAttributeRegistry attributeRegistry;

    /**
     * @deprecated Prefer constructor injection of the application-wide registry.
     */
    @Deprecated(forRemoval = false)
    public ScmSpanDataMapper(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSanitizer sanitizer
    ) {
        this(context, targetIndexResolver, sanitizer, requiredApplicationRegistry());
    }

    public ScmSpanDataMapper(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSanitizer sanitizer,
            ObservationAttributeRegistry attributeRegistry
    ) {
        this.sanitizer = sanitizer;
        this.attributeRegistry = Objects.requireNonNull(
                attributeRegistry, "The application-wide observation attribute registry is required");
        this.documentFactory = new ObservationDocumentFactory(
                context, attributeRegistry, sanitizer, targetIndexResolver);
        this.recordValidator = new ObservationRecordValidator(attributeRegistry);
    }

    private static ObservationAttributeRegistry requiredApplicationRegistry() {
        return ObservationAttributeRegistryHolder.get().orElseThrow(() -> new IllegalStateException(
                "The application-wide observation attribute registry must be initialized before the span mapper"));
    }

    public Map<String, Object> map(SpanData spanData) {
        Instant startTime = instant(spanData.getStartEpochNanos());
        Instant endTime = instant(spanData.getEndEpochNanos());
        String eventAction = stringAttribute(spanData, EVENT_ACTION, textOrDefault(spanData.getName(), "trace.span"));
        String eventOutcome = stringAttribute(spanData, EVENT_OUTCOME, statusOutcome(spanData));
        String spanName = textOrDefault(spanData.getName(), "trace.span");
        ObservationDocumentBuilder builder = documentFactory.trace(
                endTime,
                spanName,
                stringAttribute(spanData, CORRELATION_ID, ObservationIds.correlationId()),
                stringAttribute(spanData, CORRELATION_TYPE, CorrelationType.OPERATION.value())
        );
        builder.put(CommonTraceAttributes.EVENT_ACTION, textOrDefault(eventAction, spanName));
        builder.put(CommonTraceAttributes.EVENT_OUTCOME, textOrDefault(eventOutcome, statusOutcome(spanData)));
        builder.put(CommonTraceAttributes.TRACE_ID, spanData.getTraceId());
        builder.put(CommonTraceAttributes.SPAN_ID, spanData.getSpanId());
        putParentSpanId(builder, spanData.getParentSpanContext());
        builder.put(CommonTraceAttributes.SPAN_NAME, spanName);
        builder.put(CommonTraceAttributes.SPAN_KIND, spanData.getKind().name().toLowerCase(Locale.ROOT));
        builder.put(CommonTraceAttributes.SPAN_START_TIME, startTime.toString());
        builder.put(CommonTraceAttributes.SPAN_END_TIME, endTime.toString());
        builder.put(CommonTraceAttributes.SPAN_DURATION_MS, longAttribute(
                spanData,
                SPAN_DURATION_MS,
                elapsedMillis(spanData.getStartEpochNanos(), spanData.getEndEpochNanos())
        ));
        putSpanAttributes(builder, spanData, spanName);
        putSpanEvents(builder, spanData);
        Map<String, Object> document = builder.build();
        recordValidator.validate(ObservationStream.TRACE, ObservationRecordKind.EVENT, false, document);
        return document;
    }

    private void putParentSpanId(ObservationDocumentBuilder builder, SpanContext parentSpanContext) {
        if (parentSpanContext != null && parentSpanContext.isValid()) {
            builder.put(CommonTraceAttributes.PARENT_SPAN_ID, parentSpanContext.getSpanId());
        }
    }

    private void putSpanAttributes(ObservationDocumentBuilder builder, SpanData spanData, String spanName) {
        spanData.getAttributes().forEach(
                (attributeKey, value) -> putAttribute(builder, attributeKey, value, spanName));
    }

    private void putAttribute(
            ObservationDocumentBuilder builder,
            AttributeKey<?> attributeKey,
            Object value,
            String spanName
    ) {
        if (attributeKey == null || value == null) {
            return;
        }
        String fieldName = attributeKey.getKey();
        if (TraceAttributeSecurity.isReservedTraceField(fieldName)
                || !TraceAttributeSecurity.isAllowed(fieldName)
                || (!"gateway.receive".equals(spanName)
                && TraceAttributeSecurity.isGatewayOnlyJwtContextField(fieldName))) {
            return;
        }
        builder.put(fieldName, value);
    }

    private void putSpanEvents(ObservationDocumentBuilder builder, SpanData spanData) {
        List<Map<String, Object>> events = new ArrayList<>();
        List<Map<String, Object>> enrichedEvents = TraceObservationSpanEventRegistry.drain(
                spanData.getTraceId(), spanData.getSpanId());
        boolean[] consumedEnrichedEvents = new boolean[enrichedEvents.size()];
        for (EventData eventData : spanData.getEvents()) {
            String eventName = safeEventName(eventData.getName());
            Map<String, Object> enrichedEvent = consumeEnrichedEvent(
                    enrichedEvents, consumedEnrichedEvents, eventName);
            if (enrichedEvent != null) {
                events.add(safeEnrichedEvent(enrichedEvent));
                continue;
            }
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("name", eventName);
            event.put("timestamp", instant(eventData.getEpochNanos()).toString());
            Map<String, Object> attributes = new LinkedHashMap<>();
            eventData.getAttributes().forEach((attributeKey, value) -> {
                if (attributeKey != null
                        && value != null
                        && TraceAttributeSecurity.isAllowedSpanEventAttribute(attributeKey.getKey())) {
                    Object prepared = safeEventAttribute(attributeKey.getKey(), value);
                    if (prepared != null) {
                        attributes.put(attributeKey.getKey(), prepared);
                    }
                }
            });
            event.put("attributes", attributes);
            events.add(event);
        }
        for (int index = 0; index < enrichedEvents.size(); index++) {
            if (!consumedEnrichedEvents[index]) {
                events.add(safeEnrichedEvent(enrichedEvents.get(index)));
            }
        }
        if (!events.isEmpty()) {
            builder.put(CommonTraceAttributes.SPAN_EVENTS, events);
        }
    }

    private Map<String, Object> consumeEnrichedEvent(
            List<Map<String, Object>> events,
            boolean[] consumed,
            String name
    ) {
        for (int index = 0; index < events.size(); index++) {
            if (!consumed[index]
                    && name.equals(String.valueOf(events.get(index).get("name")))) {
                consumed[index] = true;
                return events.get(index);
            }
        }
        return null;
    }

    private Map<String, Object> safeEnrichedEvent(Map<String, Object> source) {
        if (source == null) {
            return Map.of();
        }
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("name", safeEventName(source.get("name") == null ? null : String.valueOf(source.get("name"))));
        event.put("timestamp", eventTimestamp(source).toString());
        Map<String, Object> attributes = new LinkedHashMap<>();
        Object sourceAttributes = source.get("attributes");
        if (sourceAttributes instanceof Map<?, ?> values) {
            values.forEach((key, value) -> {
                if (key != null) {
                    Object prepared = safeEventAttribute(String.valueOf(key), value);
                    if (prepared != null) {
                        attributes.put(String.valueOf(key).trim(), prepared);
                    }
                }
            });
        }
        event.put("attributes", attributes);
        return event;
    }

    private Instant eventTimestamp(Map<String, Object> event) {
        try {
            return Instant.parse(String.valueOf(event.get("timestamp")));
        } catch (RuntimeException ignored) {
            return Instant.MAX;
        }
    }

    private Object safeEventAttribute(String fieldName, Object value) {
        if (fieldName == null
                || fieldName.isBlank()
                || value == null
                || !TraceAttributeSecurity.isAllowedSpanEventAttribute(fieldName)) {
            return null;
        }
        String normalizedField = fieldName.trim();
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(normalizedField, value);
        return attributeRegistry.prepareValue(ObservationStream.TRACE, normalizedField, sanitized);
    }

    private String safeEventName(String name) {
        if (name == null || name.isBlank()) {
            return "span.event";
        }
        String normalized = name.replace('\r', ' ').replace('\n', ' ').trim();
        if (normalized.isEmpty()) {
            return "span.event";
        }
        return normalized.length() > 128 ? normalized.substring(0, 128) : normalized;
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
        Object prepared = attributeRegistry.prepareValue(ObservationStream.TRACE, fieldName, sanitized);
        return prepared == null ? fallback : String.valueOf(prepared);
    }

    private String statusOutcome(SpanData spanData) {
        if (spanData.getStatus() != null && StatusCode.ERROR.equals(spanData.getStatus().getStatusCode())) {
            return "failure";
        }
        return "success";
    }

    private long longAttribute(SpanData spanData, String fieldName, long fallback) {
        Long value = spanData.getAttributes().get(AttributeKey.longKey(fieldName));
        return value == null ? Math.max(0L, fallback) : Math.max(0L, value);
    }

    private Instant instant(long epochNanos) {
        long seconds = Math.floorDiv(epochNanos, 1_000_000_000L);
        long nanos = Math.floorMod(epochNanos, 1_000_000_000L);
        return Instant.ofEpochSecond(seconds, nanos);
    }

    private long elapsedMillis(long startNanos, long endNanos) {
        return TimeUnit.NANOSECONDS.toMillis(Math.max(0L, endNanos - startNanos));
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
