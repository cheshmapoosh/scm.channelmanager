package ir.daneshrefah.scm.logging.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class Slf4jLogExporterConfig implements SpanExporter {

    private static final String OPTIONS_METHOD = "OPTIONS";
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private final Logger traceLogger;


    public Slf4jLogExporterConfig(@Qualifier("logger") Logger traceLogger) {
        this.traceLogger = traceLogger;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        spans.forEach(span -> {
            if (span != null) {
                if (span.getTotalAttributeCount() == 0) {
                    return;
                }
                String method = span.getAttributes().get(AttributeKey.stringKey("http.method"));
                if (OPTIONS_METHOD.equalsIgnoreCase(method)) {
                    return;
                }
            }
            try {
                String json = objectMapper.writeValueAsString(span);
                traceLogger.info(json);
            } catch (Exception e) {
                traceLogger.error("Error logging span as JSON", e);
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
