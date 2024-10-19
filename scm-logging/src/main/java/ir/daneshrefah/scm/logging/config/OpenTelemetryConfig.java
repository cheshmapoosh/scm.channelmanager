package ir.daneshrefah.scm.logging.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    private final Slf4jSpanExporter Slf4jSpanExporter;
    private final String applicationName;

    public OpenTelemetryConfig(Slf4jSpanExporter Slf4jSpanExporter,
                               @Value("${spring.application.name}") String applicationName) {
        this.Slf4jSpanExporter = Slf4jSpanExporter;
        this.applicationName = applicationName;
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(Slf4jSpanExporter))
                .build();
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    public Tracer tracer() {
        return openTelemetry().getTracer(this.applicationName);
    }
}