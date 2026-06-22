package ir.daneshrefah.scm.observation.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import ir.daneshrefah.scm.observation.JwtObservationSanitizer;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.ObservationDocumentBuilder;
import ir.daneshrefah.scm.observation.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationRecordKind;
import ir.daneshrefah.scm.observation.ObservationStream;
import ir.daneshrefah.scm.observation.attributes.ScmCommonLogAttributes;
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
    private static final JwtObservationSanitizer SANITIZER = new JwtObservationSanitizer();

    private final Set<String> unknownWarnings = ConcurrentHashMap.newKeySet();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null) {
            return;
        }

        ObservationAttributeRegistry registry = ObservationAttributeRegistryHolder.getOrCommonOnly();
        Map<String, Object> attributes = structuredAttributes(event, registry);
        putMdcFields(attributes, event.getMDCPropertyMap());
        putThrowableFields(attributes, event.getThrowableProxy());

        ObservationRecordKind kind = recordKind(event);
        boolean errorContext = event.getThrowableProxy() != null || kind == ObservationRecordKind.EXCEPTION;
        ObservationDocumentFactory factory = new ObservationDocumentFactory(null, registry, SANITIZER);
        ObservationDocumentBuilder builder = factory.log(
                kind,
                errorContext,
                Instant.ofEpochMilli(event.getTimeStamp()),
                level(event),
                event.getLoggerName(),
                event.getThreadName(),
                event.getFormattedMessage(),
                correlationId(attributes, event),
                correlationType(attributes, event)
        );
        builder.putAll(attributes);

        for (Map.Entry<String, Object> entry : builder.build().entrySet()) {
            generator.writeObjectField(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, Object> structuredAttributes(ILoggingEvent event, ObservationAttributeRegistry registry) throws IOException {
        Map<String, Object> attributes = new LinkedHashMap<>();
        Object[] arguments = event.getArgumentArray();
        if (arguments == null) {
            return attributes;
        }
        for (Object argument : arguments) {
            if (argument instanceof StructuredArgument structuredArgument) {
                readStructuredArgument(structuredArgument, registry, attributes);
            }
        }
        return attributes;
    }

    private void readStructuredArgument(
            StructuredArgument argument,
            ObservationAttributeRegistry registry,
            Map<String, Object> attributes
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
                    putStructuredField(registry, attributes, fieldName, value);
                }
            }
        }
    }

    private void putStructuredField(
            ObservationAttributeRegistry registry,
            Map<String, Object> attributes,
            String fieldName,
            Object value
    ) {
        if (fieldName == null || fieldName.isBlank() || isEventOwnedField(fieldName)) {
            return;
        }
        String normalizedField = fieldName.trim();
        if (registry.contains(ObservationStream.LOG, normalizedField)) {
            attributes.put(normalizedField, value);
        } else if (unknownWarnings.add(normalizedField)) {
            LOG.warn("Dropping unregistered SCM log attribute '{}'", normalizedField);
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
        return recordKind(event) == ObservationRecordKind.CONTEXT ? "lifecycle" : "unknown";
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
        return ScmCommonLogAttributes.TIMESTAMP.name().equals(fieldName)
                || ScmCommonLogAttributes.LOG_LEVEL.name().equals(fieldName)
                || ScmCommonLogAttributes.LOG_LOGGER.name().equals(fieldName)
                || ScmCommonLogAttributes.PROCESS_THREAD_NAME.name().equals(fieldName)
                || ScmCommonLogAttributes.MESSAGE.name().equals(fieldName);
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
