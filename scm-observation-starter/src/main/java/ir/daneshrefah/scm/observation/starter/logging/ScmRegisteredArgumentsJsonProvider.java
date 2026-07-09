package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.composite.AbstractJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScmRegisteredArgumentsJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ScmRegisteredArgumentsJsonProvider.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObservationSanitizer SANITIZER = new SecretScrubbingObservationSanitizer();

    private final Set<String> unknownWarnings = ConcurrentHashMap.newKeySet();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null || event.getArgumentArray() == null) {
            return;
        }
        ObservationAttributeRegistry registry = ObservationAttributeRegistryHolder.getOrCommonOnly();
        for (Object argument : event.getArgumentArray()) {
            if (argument instanceof StructuredArgument structuredArgument) {
                writeStructuredArgument(generator, registry, structuredArgument);
            }
        }
    }

    private void writeStructuredArgument(JsonGenerator generator, ObservationAttributeRegistry registry, StructuredArgument argument) throws IOException {
        TokenBuffer buffer = new TokenBuffer(OBJECT_MAPPER, false);
        argument.writeTo(buffer);
        try (JsonParser parser = buffer.asParser(OBJECT_MAPPER)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.currentName();
                    parser.nextToken();
                    Object value = parser.readValueAs(Object.class);
                    writeRegisteredField(generator, registry, fieldName, value);
                }
            }
        }
    }

    private void writeRegisteredField(JsonGenerator generator, ObservationAttributeRegistry registry, String fieldName, Object value) throws IOException {
        if (isProviderOwnedField(fieldName)) {
            return;
        }
        Object sanitized = SANITIZER.sanitize(fieldName, value);
        Object prepared = registry.prepareValue(fieldName, sanitized);
        if (prepared != null) {
            generator.writeObjectField(fieldName, prepared);
        } else if (fieldName != null && !registry.containsLogAttribute(fieldName) && unknownWarnings.add(fieldName)) {
            LOG.warn("Dropping unregistered SCM log attribute '{}'", fieldName);
        }
    }

    private boolean isProviderOwnedField(String fieldName) {
        return ScmLogFields.TRACE_ID.equals(fieldName)
                || ScmLogFields.SPAN_ID.equals(fieldName)
                || ScmLogFields.CORRELATION_ID.equals(fieldName)
                || ScmLogFields.CORRELATION_TYPE.equals(fieldName)
                || ScmLogFields.ERROR_TYPE.equals(fieldName)
                || ScmLogFields.ERROR_MESSAGE.equals(fieldName)
                || ScmLogFields.ERROR_STACK_TRACE.equals(fieldName);
    }
}
