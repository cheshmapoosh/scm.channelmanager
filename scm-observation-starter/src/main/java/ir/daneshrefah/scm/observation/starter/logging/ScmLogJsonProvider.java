package ir.daneshrefah.scm.observation.starter.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;

import java.io.IOException;
import java.util.Map;

public class ScmLogJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private final ScmLogDocumentFactory documentFactory = new ScmLogDocumentFactory();

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        if (event == null) {
            return;
        }

        Map<String, Object> document = documentFactory.create(event);
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            generator.writeObjectField(entry.getKey(), entry.getValue());
        }
    }
}
