package ir.daneshrefah.scm.provider.rest.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.provider.rest.config.RestProviderConfigResolver;
import ir.daneshrefah.scm.provider.rest.config.RestProviderProperties;
import ir.daneshrefah.scm.provider.rest.http.RestProviderClientRegistry;
import ir.daneshrefah.scm.provider.rest.log.RestProviderLogSanitizer;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.model.RestProviderRequestSpec;
import ir.daneshrefah.scm.provider.rest.ratelimit.NoopRestProviderRateLimiter;
import ir.daneshrefah.scm.provider.rest.trace.RestProviderTraceSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.SimpleRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RestProviderProducerCustomizerTest {

    @Test
    void producerFiltersSortsAndExecutesCustomizersAroundTransportCall() throws Exception {
        List<String> events = new ArrayList<>();
        CapturingClientRegistry clientRegistry = new CapturingClientRegistry(executorProvider(), events);
        RestProviderMetrics metrics = new RestProviderMetrics();
        CountingCustomizer unsupported = new CountingCustomizer("unsupported", 1, false, events);
        CountingCustomizer macLike = new CountingCustomizer("mac", 10000, false, events);

        try (CamelContext camelContext = camelContext(clientRegistry, metrics, List.of(
                new CountingCustomizer("second", 200, true, events),
                unsupported,
                new CountingCustomizer("first", 100, true, events),
                macLike
        ))) {
            camelContext.start();
            RestProviderEndpoint endpoint = camelContext.getEndpoint("rest-provider:hps", RestProviderEndpoint.class);
            Producer producer = endpoint.createProducer();
            producer.start();
            try {
                Exchange exchange = endpoint.createExchange(ExchangePattern.InOut);
                exchange.getMessage().setBody(Map.of("body", Map.of("amount", "1")));

                producer.process(exchange);
            } finally {
                producer.stop();
            }
        }

        assertEquals(List.of("before-first", "before-second", "transport", "after-first", "after-second"), events);
        assertEquals("first,second", clientRegistry.requestSpec.headers().get("X-Customizer-Order"));
        assertEquals(0, unsupported.invocations);
        assertEquals(0, macLike.invocations);
        assertEquals(4, metrics.provider("hps").customizerExecutionCount());
        assertFalse(clientRegistry.requestSpec.skipProviderAuth());
    }

    private CamelContext camelContext(
            CapturingClientRegistry clientRegistry,
            RestProviderMetrics metrics,
            List<ProviderMessageCustomizer> customizers
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("restProviderConfigResolver", new RestProviderConfigResolver(properties()));
        registry.bind("restProviderClientRegistry", clientRegistry);
        registry.bind("restProviderMetrics", metrics);
        registry.bind("restProviderRateLimiter", new NoopRestProviderRateLimiter());
        registry.bind("restProviderTraceSupport", new RestProviderTraceSupport(
                OpenTelemetry.noop().getTracer("scm-provider-rest-test")));
        registry.bind("restProviderLogSanitizer", new RestProviderLogSanitizer(objectMapper));
        registry.bind("objectMapper", objectMapper);
        for (ProviderMessageCustomizer customizer : customizers) {
            registry.bind(customizer.getClass().getSimpleName() + customizer.order() + customizer.hashCode(), customizer);
        }

        DefaultCamelContext camelContext = new DefaultCamelContext(registry);
        camelContext.addComponent("rest-provider", new RestProviderComponent(camelContext));
        return camelContext;
    }

    private RestProviderProperties properties() {
        RestProviderProperties properties = new RestProviderProperties();
        RestProviderProperties.Instance instance = new RestProviderProperties.Instance();
        instance.setBaseUrl("https://provider.example");
        instance.getToken().setEnabled(false);
        properties.getProviders().put("hps", instance);
        return properties;
    }

    private ObjectProvider<ExecutorService> executorProvider() {
        return new DefaultListableBeanFactory().getBeanProvider(ExecutorService.class);
    }

    private static final class CapturingClientRegistry extends RestProviderClientRegistry {
        private final List<String> events;
        private RestProviderRequestSpec requestSpec;

        private CapturingClientRegistry(ObjectProvider<ExecutorService> executorProvider, List<String> events) {
            super(executorProvider);
            this.events = events;
        }

        @Override
        public ResponseEntity<String> exchange(ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig config,
                                               RestProviderRequestSpec requestSpec) {
            this.requestSpec = requestSpec;
            events.add("transport");
            return ResponseEntity.ok("{\"ok\":true}");
        }
    }

    private static final class CountingCustomizer implements ProviderMessageCustomizer {
        private final String name;
        private final int order;
        private final boolean supported;
        private final List<String> events;
        private int invocations;

        private CountingCustomizer(String name, int order, boolean supported, List<String> events) {
            this.name = name;
            this.order = order;
            this.supported = supported;
            this.events = events;
        }

        @Override
        public boolean supports(ProviderMessageCustomizerContext context) {
            return supported && "rest".equalsIgnoreCase(context.transportType());
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public void beforeSend(ProviderExchange exchange) {
            invocations++;
            events.add("before-" + name);
            String current = exchange.request().headers().get("X-Customizer-Order");
            exchange.request().putHeader("X-Customizer-Order", current == null ? name : current + "," + name);
        }

        @Override
        public void afterReceive(ProviderExchange exchange) {
            invocations++;
            events.add("after-" + name);
            if (exchange.response() == null) {
                throw new IllegalStateException("response must be available before afterReceive");
            }
        }
    }
}
