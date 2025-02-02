//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ir.daneshrefah.scm.logging.config;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.apache.camel.CamelContext;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.apache.camel.opentelemetry.OpenTelemetryTracingStrategy;
import org.apache.camel.opentelemetry.starter.OpenTelemetryConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(
    proxyBeanMethods = false
)
@EnableConfigurationProperties({OpenTelemetryConfigurationProperties.class})
@ConditionalOnProperty(
    value = {"camel.opentelemetry.enabled"},
    matchIfMissing = true
)

public class OpenTelemetryAutoConfiguration {
    @Autowired(
        required = false
    )
    private Tracer tracer;
    @Autowired(
        required = false
    )
    private ContextPropagators contextPropagators;

    public OpenTelemetryAutoConfiguration() {
    }

    @Bean(
        initMethod = "",
        destroyMethod = ""
    )
    @ConditionalOnMissingBean({OpenTelemetryTracer.class})
    OpenTelemetryTracer openTelemetryEventNotifier(CamelContext camelContext, OpenTelemetryConfigurationProperties config) {
        OpenTelemetryTracer ottracer = new OpenTelemetryTracerImpl();
        if (this.tracer != null) {
            ottracer.setTracer(this.tracer);
        }

        if (this.contextPropagators != null) {
            ottracer.setContextPropagators(this.contextPropagators);
        }

        if (config.getExcludePatterns() != null) {
            ottracer.setExcludePatterns(config.getExcludePatterns());
        }

        if (config.getEncoding() != null) {
            ottracer.setEncoding(config.getEncoding());
        }

        if (config.getTraceProcessors() != null && config.getTraceProcessors()) {
            OpenTelemetryTracingStrategy tracingStrategy = new OpenTelemetryTracingStrategy(ottracer);
            tracingStrategy.setPropagateContext(true);
            ottracer.setTracingStrategy(tracingStrategy);
        }

        ottracer.init(camelContext);
        return ottracer;
    }
}
