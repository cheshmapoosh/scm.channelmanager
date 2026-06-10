package ir.daneshrefah.scm.observation.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import net.logstash.logback.composite.AbstractJsonProvider;

import java.io.IOException;
import java.util.Map;

public class ScmMdcJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private final ObservationAttributeRegistry registry = ObservationAttributeRegistry.effectiveLogRegistry();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null) {
            return;
        }
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            writeIfPresent(generator, mdc, ScmLogFields.TRACE_ID, "traceId", "trace_id", "trace.id");
            writeIfPresent(generator, mdc, ScmLogFields.SPAN_ID, "spanId", "span_id", "span.id");
        }
        String correlationId = mdc == null ? null : firstPresent(mdc, "correlationId", "correlation_id", "correlation.id");
        if (correlationId == null) {
            correlationId = textOrNull(ScmInitCorrelationContext.current());
        }
        if (correlationId == null) {
            correlationId = ScmInitCorrelationContext.ensure();
        }
        writeField(generator, ScmLogFields.CORRELATION_ID, correlationId);

        String correlationType = mdc == null ? null : firstPresent(mdc, "correlationType", "correlation_type", "correlation.type");
        writeField(generator, ScmLogFields.CORRELATION_TYPE, correlationType == null ? "lifecycle" : correlationType);
    }

    private void writeIfPresent(JsonGenerator generator, Map<String, String> mdc, String fieldName, String... aliases)
            throws IOException {
        String value = firstPresent(mdc, aliases);
        writeField(generator, fieldName, value);
    }

    private void writeField(JsonGenerator generator, String fieldName, String value) throws IOException {
        Object prepared = registry.prepareValue(fieldName, value);
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
