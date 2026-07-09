package ir.daneshrefah.scm.observation.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.observation.starter.logging.ScmObservationEventJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScmObservationEventJsonProviderTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void encodesObservationPayloadAsOneCompactJsonObjectLine() throws Exception {
        LoggerContext context = new LoggerContext();
        LoggingEventJsonProviders providers = new LoggingEventJsonProviders();
        providers.addProvider(new ScmObservationEventJsonProvider());
        LoggingEventCompositeJsonEncoder encoder = new LoggingEventCompositeJsonEncoder();
        encoder.setContext(context);
        encoder.setProviders(providers);
        encoder.start();
        try {
            LoggingEvent event = new LoggingEvent();
            event.setLoggerContext(context);
            event.setLoggerName("trace");
            event.setMessage("""
                    {"trace.id":"t1","span.id":"s1","span.name":"uaa.http.request","span.kind":"server","span.duration_ms":12,"correlation.id":"c1","correlation.type":"request","error.stack_trace":"line1\\nline2"}
                    """.trim());

            String line = new String(encoder.encode(event), StandardCharsets.UTF_8);

            assertTrue(line.endsWith("\n"));
            assertFalse(line.contains("["));
            assertFalse(line.contains("\n  "));
            assertEquals(1, line.lines().count());
            JsonNode json = OBJECT_MAPPER.readTree(line);
            assertTrue(json.isObject());
            assertEquals("t1", json.get("trace.id").asText());
            assertEquals("line1\nline2", json.get("error.stack_trace").asText());
        } finally {
            encoder.stop();
            context.stop();
        }
    }
}
