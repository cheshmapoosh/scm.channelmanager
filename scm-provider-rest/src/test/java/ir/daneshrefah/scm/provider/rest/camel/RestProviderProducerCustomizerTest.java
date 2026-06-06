package ir.daneshrefah.scm.provider.rest.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.rest.config.RestProviderConfigResolver;
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
import org.springframework.http.HttpHeaders;
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

    @Test
    void providerWithoutAuthCustomizersDoesNotForwardRequestAuthorizationHeaders() throws Exception {
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
                exchange.getMessage().setHeader(HttpHeaders.AUTHORIZATION, "Bearer inbound-token");
                exchange.getMessage().setHeader(HttpHeaders.PROXY_AUTHORIZATION, "Basic proxy-token");
                exchange.getMessage().setBody(Map.of(
                        "headers", Map.of(
                                HttpHeaders.AUTHORIZATION, "Bearer envelope-token",
                                HttpHeaders.PROXY_AUTHORIZATION, "Basic envelope-proxy-token"
                        ),
                        "auth", Map.of("type", "BEARER", "token", "request-token"),
                        "body", Map.of("amount", "1", "auth", "body-auth-like-field")
                ));
                producer.process(exchange);
            } finally {
                producer.stop();
            }
        }

        assertEquals(List.of("transport"), events);
        assertEquals(0, metrics.provider("hps").customizerExecutionCount());
        assertFalse(clientRegistry.requestSpec.headers().containsKey(HttpHeaders.AUTHORIZATION));
        assertFalse(clientRegistry.requestSpec.headers().containsKey(HttpHeaders.PROXY_AUTHORIZATION));
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
        List<ProviderMessageCustomizerFactory<?>> factories = List.of(
                new CountingCustomizerFactory("first", 100, events),
                new CountingCustomizerFactory("second", 200, events)
        );
        ProviderMessageCustomizerPipelineFactory pipelineFactory = new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(factories));
        registry.bind("restProviderConfigResolver", new RestProviderConfigResolver(providerRegistryProperties, pipelineFactory));
        registry.bind("restProviderClientRegistry", clientRegistry);
        registry.bind("restProviderMetrics", metrics);
        registry.bind("restProviderRateLimiter", new NoopRestProviderRateLimiter());
        registry.bind("restProviderTraceSupport", new RestProviderTraceSupport(OpenTelemetry.noop().getTracer("scm-provider-rest-test")));
        registry.bind("restProviderLogSanitizer", new RestProviderLogSanitizer(objectMapper));
        registry.bind("objectMapper", objectMapper);
        registry.bind("unconfiguredGlobalCustomizer", new FailingGlobalCustomizer());

        DefaultCamelContext camelContext = new DefaultCamelContext(registry);
        camelContext.addComponent("rest-provider", new RestProviderComponent(camelContext));
        return camelContext;
    }

    private ProviderRegistryProperties properties(boolean withCustomizers) {
        ProviderRegistryProperties properties = new ProviderRegistryProperties();
        Map<String, Object> instance = new java.util.LinkedHashMap<>();
        instance.put("type", "rest");
        instance.put("base-url", "https://provider.example");
        if (withCustomizers) {
            instance.put("message-customizers", List.of(
                    Map.of("type", "second", "config", Map.of()),
                    Map.of("type", "first", "config", Map.of())
            ));
        }
        properties.put("hps", instance);
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

    public static final class Config {
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
