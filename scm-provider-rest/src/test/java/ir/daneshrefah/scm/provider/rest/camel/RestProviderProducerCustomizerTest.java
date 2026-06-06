package ir.daneshrefah.scm.provider.rest.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
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
    void producerExecutesOnlyConfiguredProviderInstanceCustomizers() throws Exception {
        List<String> events = new ArrayList<>();
        CapturingClientRegistry clientRegistry = new CapturingClientRegistry(executorProvider(), events);
        RestProviderMetrics metrics = new RestProviderMetrics();

        try (CamelContext camelContext = camelContext(clientRegistry, metrics, events)) {
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
        assertEquals(4, metrics.provider("hps").customizerExecutionCount());
        assertFalse(clientRegistry.requestSpec.skipProviderAuth());
    }

    @Test
    void missingMessageCustomizersExecutesNoCustomizers() throws Exception {
        List<String> events = new ArrayList<>();
        CapturingClientRegistry clientRegistry = new CapturingClientRegistry(executorProvider(), events);
        RestProviderMetrics metrics = new RestProviderMetrics();
        ProviderRegistryProperties registryProperties = properties(false);

        try (CamelContext camelContext = camelContext(clientRegistry, metrics, events, registryProperties)) {
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

        assertEquals(List.of("transport"), events);
        assertEquals(0, metrics.provider("hps").customizerExecutionCount());
    }

    private CamelContext camelContext(
            CapturingClientRegistry clientRegistry,
            RestProviderMetrics metrics,
            List<String> events
    ) {
        return camelContext(clientRegistry, metrics, events, properties(true));
    }

    private CamelContext camelContext(
            CapturingClientRegistry clientRegistry,
            RestProviderMetrics metrics,
            List<String> events,
            ProviderRegistryProperties providerRegistryProperties
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("restProviderConfigResolver", new RestProviderConfigResolver(providerRegistryProperties, new RestProviderProperties()));
        registry.bind("restProviderClientRegistry", clientRegistry);
        registry.bind("restProviderMetrics", metrics);
        registry.bind("restProviderRateLimiter", new NoopRestProviderRateLimiter());
        registry.bind("restProviderTraceSupport", new RestProviderTraceSupport(OpenTelemetry.noop().getTracer("scm-provider-rest-test")));
        registry.bind("restProviderLogSanitizer", new RestProviderLogSanitizer(objectMapper));
        registry.bind("objectMapper", objectMapper);
        List<ProviderMessageCustomizerFactory<?>> factories = List.of(
                new CountingCustomizerFactory("first", 100, events),
                new CountingCustomizerFactory("second", 200, events)
        );
        registry.bind("providerMessageCustomizerFactoryRegistry", new ProviderMessageCustomizerFactoryRegistry(factories));
        registry.bind("providerMessageCustomizerPipelineFactory", new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(factories), objectMapper));
        registry.bind("unconfiguredGlobalCustomizer", new FailingGlobalCustomizer());

        DefaultCamelContext camelContext = new DefaultCamelContext(registry);
        camelContext.addComponent("rest-provider", new RestProviderComponent(camelContext));
        return camelContext;
    }

    private ProviderRegistryProperties properties(boolean withCustomizers) {
        ProviderRegistryProperties properties = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider instance = new ProviderRegistryProperties.Provider();
        instance.setType("rest");
        instance.setBaseUrl("https://provider.example");
        if (withCustomizers) {
            instance.getMessageCustomizers().add(definition("second"));
            instance.getMessageCustomizers().add(definition("first"));
        }
        properties.getProviders().put("hps", instance);
        return properties;
    }

    private ProviderMessageCustomizerDefinition definition(String type) {
        ProviderMessageCustomizerDefinition definition = new ProviderMessageCustomizerDefinition();
        definition.setType(type);
        return definition;
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

    private static final class CountingCustomizerFactory implements ProviderMessageCustomizerFactory<Config> {
        private final String type;
        private final int order;
        private final List<String> events;

        private CountingCustomizerFactory(String type, int order, List<String> events) {
            this.type = type;
            this.order = order;
            this.events = events;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public Class<Config> configType() {
            return Config.class;
        }

        @Override
        public int defaultOrder() {
            return order;
        }

        @Override
        public ProviderMessageCustomizer create(ProviderMessageCustomizerFactoryContext context, Config config) {
            return new CountingCustomizer(type, order, events);
        }
    }

    private static final class Config {
    }

    private record CountingCustomizer(String name, int order, List<String> events) implements ProviderMessageCustomizer {
        @Override
        public void beforeSend(ProviderExchange exchange) {
            events.add("before-" + name);
            String current = exchange.request().headers().get("X-Customizer-Order");
            exchange.request().putHeader("X-Customizer-Order", current == null ? name : current + "," + name);
        }

        @Override
        public void afterReceive(ProviderExchange exchange) {
            events.add("after-" + name);
            if (exchange.response() == null) {
                throw new IllegalStateException("response must be available before afterReceive");
            }
        }
    }

    private static final class FailingGlobalCustomizer implements ProviderMessageCustomizer {
        @Override
        public int order() {
            return 1;
        }

        @Override
        public void beforeSend(ProviderExchange exchange) {
            throw new AssertionError("Global ProviderMessageCustomizer beans must not be executed");
        }
    }
}
