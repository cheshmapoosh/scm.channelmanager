package ir.daneshrefah.scm.logging.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class OpenTelemetryConfig {

    @Value("${spring.application.name}")
    private String applicationName;
    private final Slf4jLogExporterConfig slf4jLogExporterConfig;

    @Bean
    @Primary
    public SpanExporter SpanExporter(){
        return slf4jLogExporterConfig;
    }


    @Bean
    public OpenTelemetry openTelemetry() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setSpanLimits(
                        SpanLimits.builder()
                        .setMaxNumberOfAttributes(128)
                        .build()
                )
                .setSampler(Sampler.alwaysOn())
                .addSpanProcessor(SimpleSpanProcessor.create(SpanExporter()))
                .build();
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    public Tracer tracer() {
        return openTelemetry().getTracer(this.applicationName);
    }
}