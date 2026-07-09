package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.composite.AbstractJsonProvider;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ScmObservationEventJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null || event.getFormattedMessage() == null || event.getFormattedMessage().isBlank()) {
            return;
        }
        JsonNode payload = OBJECT_MAPPER.readTree(event.getFormattedMessage());
        if (!payload.isObject()) {
            generator.writeStringField("message", event.getFormattedMessage());
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            generator.writeFieldName(field.getKey());
            generator.writeTree(field.getValue());
        }
    }
}
