package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationContextHolder;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentBuilder;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.starter.ObservationIds;
import ir.daneshrefah.scm.observation.starter.ObservationRecordKind;
import ir.daneshrefah.scm.observation.starter.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.ObservationStream;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.attributes.log.CommonLogAttributes;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.composite.AbstractJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScmLogJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ScmLogJsonProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObservationSanitizer SANITIZER = new SecretScrubbingObservationSanitizer();

    private final Set<String> unknownWarnings = ConcurrentHashMap.newKeySet();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null) {
            return;
        }

        ObservationAttributeRegistry registry = ObservationAttributeRegistryHolder.getOrCommonOnly();
        ObservationRecordKind kind = recordKind(event);
        Map<String, Object> attributes = structuredAttributes(event, registry, kind);
        putMdcFields(attributes, event.getMDCPropertyMap());
        putThrowableFields(attributes, event.getThrowableProxy());

        boolean errorContext = event.getThrowableProxy() != null || kind == ObservationRecordKind.EXCEPTION;
        ObservationContext context = ObservationContextHolder.get()
                .orElseGet(() -> ObservationContext.from(null, null));
        ObservationDocumentFactory factory = new ObservationDocumentFactory(context, registry, SANITIZER);
        ObservationDocumentBuilder builder = factory.log(
                Instant.ofEpochMilli(event.getTimeStamp()),
                level(event),
                event.getLoggerName(),
                event.getThreadName(),
                event.getFormattedMessage(),
                correlationId(attributes, event),
                correlationType(attributes, event)
        );
        builder.putAll(attributes);

        Map<String, Object> document = builder.build();
        new ObservationRecordValidator(registry).validate(ObservationStream.LOG, kind, errorContext, document);
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            generator.writeObjectField(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> structuredAttributes(
            ILoggingEvent event,
            ObservationAttributeRegistry registry,
            ObservationRecordKind kind
    ) throws IOException {
        Map<String, Object> attributes = new LinkedHashMap<>();
        Object[] arguments = event.getArgumentArray();
        if (arguments == null) {
            return attributes;
        }
        for (Object argument : arguments) {
            if (argument instanceof StructuredArgument structuredArgument) {
                readStructuredArgument(structuredArgument, registry, attributes, kind);
            }
        }
        return attributes;
    }

    private void readStructuredArgument(
            StructuredArgument argument,
            ObservationAttributeRegistry registry,
            Map<String, Object> attributes,
            ObservationRecordKind kind
    ) throws IOException {
        TokenBuffer buffer = new TokenBuffer(OBJECT_MAPPER, false);
        argument.writeTo(buffer);
        try (JsonParser parser = buffer.asParser(OBJECT_MAPPER)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.currentName();
                    parser.nextToken();
                    Object value = parser.readValueAs(Object.class);
                    putStructuredField(registry, attributes, fieldName, value, kind);
                }
            }
        }
    }

    private void putStructuredField(
            ObservationAttributeRegistry registry,
            Map<String, Object> attributes,
            String fieldName,
            Object value,
            ObservationRecordKind kind
    ) {
        if (fieldName == null || fieldName.isBlank() || isEventOwnedField(fieldName)) {
            return;
        }
        String normalizedField = fieldName.trim();
        if (registry.contains(ObservationStream.LOG, normalizedField)) {
            attributes.put(normalizedField, value);
        } else if (unknownWarnings.add(normalizedField)) {
            LOG.warn(
                    "Dropping unregistered SCM log attribute '{}' for stream '{}' and record kind '{}'",
                    normalizedField,
                    ObservationStream.LOG,
                    kind
            );
        }
    }

    private void putMdcFields(Map<String, Object> attributes, Map<String, String> mdc) {
        if (mdc == null || mdc.isEmpty()) {
            return;
        }
        putIfAbsent(attributes, ScmLogFields.TRACE_ID, firstPresent(mdc, "traceId", "trace_id", "trace.id"));
        putIfAbsent(attributes, ScmLogFields.SPAN_ID, firstPresent(mdc, "spanId", "span_id", "span.id"));
        putIfAbsent(attributes, ScmLogFields.CORRELATION_ID, firstPresent(mdc, "correlationId", "correlation_id", "correlation.id"));
        putIfAbsent(attributes, ScmLogFields.CORRELATION_TYPE, firstPresent(mdc, "correlationType", "correlation_type", "correlation.type"));
    }

    private void putThrowableFields(Map<String, Object> attributes, IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return;
        }
        putIfAbsent(attributes, ScmLogFields.ERROR_TYPE, throwableProxy.getClassName());
        putIfAbsent(attributes, ScmLogFields.ERROR_MESSAGE, throwableProxy.getMessage());
        putIfAbsent(attributes, ScmLogFields.ERROR_STACK_TRACE, ThrowableProxyUtil.asString(throwableProxy));
    }

    private String correlationId(Map<String, Object> attributes, ILoggingEvent event) {
        Object value = attributes.get(ScmLogFields.CORRELATION_ID);
        String text = textOrNull(value == null ? null : String.valueOf(value));
        if (text != null) {
            return text;
        }
        ObservationRecordKind kind = recordKind(event);
        if (kind == ObservationRecordKind.CONTEXT || kind == ObservationRecordKind.EVENT) {
            text = textOrNull(ScmInitCorrelationContext.current());
        }
        return text == null ? ObservationIds.correlationId() : text;
    }

    private String correlationType(Map<String, Object> attributes, ILoggingEvent event) {
        Object value = attributes.get(ScmLogFields.CORRELATION_TYPE);
        String text = textOrNull(value == null ? null : String.valueOf(value));
        if (text != null) {
            return text;
        }
        return recordKind(event) == ObservationRecordKind.CONTEXT
                ? CorrelationType.LIFECYCLE.value()
                : CorrelationType.UNKNOWN.value();
    }

    private ObservationRecordKind recordKind(ILoggingEvent event) {
        if (hasMarker(event, "SCM_CONTEXT")) {
            return ObservationRecordKind.CONTEXT;
        }
        if (hasMarker(event, "SCM_EVENT")) {
            return ObservationRecordKind.EVENT;
        }
        if (hasMarker(event, "SCM_EXCEPTION")) {
            return ObservationRecordKind.EXCEPTION;
        }
        if (hasMarker(event, "SCM_CHANGE")) {
            return ObservationRecordKind.CHANGE;
        }
        if (event.getThrowableProxy() != null && (Level.WARN.equals(event.getLevel()) || Level.ERROR.equals(event.getLevel()))) {
            return ObservationRecordKind.EXCEPTION;
        }
        return ObservationRecordKind.PLAIN;
    }

    private boolean hasMarker(ILoggingEvent event, String markerName) {
        List<Marker> markers = event.getMarkerList();
        if (markers == null || markers.isEmpty()) {
            return false;
        }
        for (Marker marker : markers) {
            if (marker != null && (markerName.equals(marker.getName()) || marker.contains(markerName))) {
                return true;
            }
        }
        return false;
    }

    private boolean isEventOwnedField(String fieldName) {
        return CommonLogAttributes.TIMESTAMP.name().equals(fieldName)
                || CommonLogAttributes.LOG_LEVEL.name().equals(fieldName)
                || CommonLogAttributes.LOG_LOGGER.name().equals(fieldName)
                || CommonLogAttributes.PROCESS_THREAD_NAME.name().equals(fieldName)
                || CommonLogAttributes.MESSAGE.name().equals(fieldName);
    }

    private void putIfAbsent(Map<String, Object> attributes, String fieldName, Object value) {
        if (!attributes.containsKey(fieldName)) {
            String text = value instanceof String ? textOrNull((String) value) : null;
            Object selectedValue = text == null && value instanceof String ? null : value;
            if (selectedValue != null) {
                attributes.put(fieldName, selectedValue);
            }
        }
    }

    private String firstPresent(Map<String, String> values, String... aliases) {
        for (String alias : aliases) {
            String value = textOrNull(values.get(alias));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String level(ILoggingEvent event) {
        return event.getLevel() == null ? "INFO" : event.getLevel().toString();
    }

    private String textOrNull(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim()) || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
