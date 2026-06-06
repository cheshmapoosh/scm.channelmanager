package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestProviderConfigResolverTest {

    @Test
    void resolvesRestProviderFromUnifiedRegistryAndBuildsPipeline() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        Map<String, Object> hps = restProvider("https://hps.example");
        hps.put("response-timeout-ms", 9000);
        hps.put("headers", Map.of("Accept", "application/json", "X-Provider", "hps"));
        hps.put("rate-limit", Map.of("enabled", true, "bucket", "hps-rest", "key", "provider-operation"));
        hps.put("message-customizers", List.of(Map.of(
                "type", "hps-rest-outlet",
                "config", Map.of("name", "outlet", "value", "123")
        )));
        registry.put("hps-rest", hps);

        RestProviderResolvedConfig config = new RestProviderConfigResolver(registry, pipelineFactory())
                .resolve("rest:hps-rest", new RestProviderEndpointOverrides(7000, null, null, null));

        assertEquals("hps-rest", config.provider());
        assertEquals("rest", config.providerType());
        assertEquals("https://hps.example", config.baseUrl());
        assertEquals(7000, config.responseTimeoutMs());
        assertEquals("application/json", config.defaultHeaders().get("Accept"));
        assertEquals("hps", config.defaultHeaders().get("X-Provider"));
        assertEquals("hps-rest", config.rateLimit().bucket());
        assertEquals("hps-rest-outlet", config.messageCustomizerPipeline().entries().getFirst().type());
    }

    @Test
    void failsFastWhenProviderTypeIsNotRest() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        registry.put("wrong", Map.of("type", "shetab", "base-url", "https://wrong.example"));

        assertThrows(IllegalArgumentException.class,
                () -> new RestProviderConfigResolver(registry, pipelineFactory()).resolve("wrong", null));
    }

    @Test
    void missingBaseUrlFailsFast() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        registry.put("missing", Map.of("type", "rest"));

        assertThrows(IllegalArgumentException.class,
                () -> new RestProviderConfigResolver(registry, pipelineFactory()).resolve("missing", null));
    }

    @Test
    void missingProviderFailsFast() {
        assertThrows(IllegalArgumentException.class,
                () -> new RestProviderConfigResolver(new ProviderRegistryProperties(), pipelineFactory()).resolve("missing", null));
    }

    private Map<String, Object> restProvider(String baseUrl) {
        Map<String, Object> provider = new LinkedHashMap<>();
        provider.put("type", "rest");
        provider.put("base-url", baseUrl);
        return provider;
    }

    private ProviderMessageCustomizerPipelineFactory pipelineFactory() {
        return new ProviderMessageCustomizerPipelineFactory(new ProviderMessageCustomizerFactoryRegistry(
                List.of(new ir.daneshrefah.scm.provider.rest.customizer.HpsRestOutletProviderMessageCustomizerFactory())
        ));
    }
}
