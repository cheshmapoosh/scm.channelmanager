package ir.daneshrefah.scm.logging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class Slf4jSpanExporter implements SpanExporter {

    private final Logger logger;
    @Autowired
    private ObjectMapper objectMapper;

    public Slf4jSpanExporter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        spans.forEach(span -> {
            try {
                String json = objectMapper.writeValueAsString(span);
                logger.info(json);
            } catch (Exception e) {
                logger.error("Error logging span as JSON", e);
            }
        });
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }
}