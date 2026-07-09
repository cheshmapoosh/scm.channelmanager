package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import ir.daneshrefah.scm.observation.starter.CorrelationType;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.starter.ObservationIds;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;
import net.logstash.logback.composite.AbstractJsonProvider;

import java.io.IOException;
import java.util.Map;

public class ScmMdcJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private static final ObservationSanitizer SANITIZER = new SecretScrubbingObservationSanitizer();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null) {
            return;
        }
        ObservationAttributeRegistry registry = ObservationAttributeRegistryHolder.getOrCommonOnly();
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            writeIfPresent(generator, registry, mdc, ScmLogFields.TRACE_ID, "traceId", "trace_id", "trace.id");
            writeIfPresent(generator, registry, mdc, ScmLogFields.SPAN_ID, "spanId", "span_id", "span.id");
        }
        String correlationId = mdc == null ? null : firstPresent(mdc, "correlationId", "correlation_id", "correlation.id");
        if (correlationId == null) {
            correlationId = textOrNull(ScmInitCorrelationContext.current());
        }
        if (correlationId == null) {
            correlationId = ObservationIds.correlationId();
        }
        writeField(generator, registry, ScmLogFields.CORRELATION_ID, correlationId);

        String correlationType = mdc == null ? null : firstPresent(mdc, "correlationType", "correlation_type", "correlation.type");
        writeField(generator, registry, ScmLogFields.CORRELATION_TYPE, correlationType == null ? CorrelationType.UNKNOWN.value() : correlationType);
    }

    private void writeIfPresent(JsonGenerator generator, ObservationAttributeRegistry registry, Map<String, String> mdc, String fieldName, String... aliases)
            throws IOException {
        String value = firstPresent(mdc, aliases);
        writeField(generator, registry, fieldName, value);
    }

    private void writeField(JsonGenerator generator, ObservationAttributeRegistry registry, String fieldName, String value) throws IOException {
        Object sanitized = SANITIZER.sanitize(fieldName, value);
        Object prepared = registry.prepareValue(fieldName, sanitized);
        if (prepared != null) {
            generator.writeObjectField(fieldName, prepared);
        }
    }

    private String firstPresent(Map<String, String> mdc, String... aliases) {
        for (String alias : aliases) {
            String value = textOrNull(mdc.get(alias));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String textOrNull(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
