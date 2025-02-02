package ir.daneshrefah.scm.logging.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.apache.camel.opentelemetry.starter.CamelOpenTelemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@CamelOpenTelemetry
public class OpenTelemetryConfig {

    private final SpanExporter spanExporter;
    private final String applicationName;

    public OpenTelemetryConfig(SpanExporter spanExporter,
                               @Value("${spring.application.name}") String applicationName) {
        this.spanExporter = spanExporter;
        this.applicationName = applicationName;
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build();
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    public Tracer tracer() {
        return openTelemetry().getTracer(this.applicationName);
    }
}