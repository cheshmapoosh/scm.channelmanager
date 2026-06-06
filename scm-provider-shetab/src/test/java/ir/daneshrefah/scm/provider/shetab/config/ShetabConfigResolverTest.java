package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.provider.shetab.customizer.HpsShetabOutletProviderMessageCustomizerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShetabConfigResolverTest {

    @Test
    void resolvesUnifiedShetabProviderWithoutDefaults() {
        ProviderRegistryProperties registry = registry("hps-shetab7", Map.of(
                "type", "shetab",
                "endpoints", List.of("10.10.10.11:9000", "10.10.10.12:9000"),
                "packager-class", "Shetab7AsciiXAPackager",
                "message-customizers", List.of(Map.of(
                        "type", "hps-shetab-outlet",
                        "config", Map.of("field", 42, "value", "123456789012345")
                ))
        ));

        ShetabResolvedConfig config = resolver(registry).resolve("hps-shetab7", null);

        assertEquals("hps-shetab7", config.provider());
        assertEquals("shetab", config.providerType());
        assertEquals(List.of("10.10.10.11:9000", "10.10.10.12:9000"), config.endpoints());
        assertEquals("hps-shetab-outlet", config.messageCustomizerPipeline().entries().getFirst().type());
    }

    @Test
    void unifiedShetabProviderFailsWhenPackagerMissing() {
        ProviderRegistryProperties registry = registry("hps", Map.of(
                "type", "shetab",
                "endpoint", "10.10.10.10:9000"
        ));

        assertThrows(IllegalArgumentException.class, () -> resolver(registry).resolve("hps", null));
    }

    @Test
    void rejectsNonShetabProviderType() {
        ProviderRegistryProperties registry = registry("hps", Map.of(
                "type", "rest",
                "endpoint", "10.10.10.10:9000",
                "packager-class", "Shetab7AsciiXAPackager"
        ));

        assertThrows(IllegalArgumentException.class, () -> resolver(registry).resolve("hps", null));
    }

    @Test
    void resolvesTypedOperationProviderNameToShetabProviderInstance() {
        ProviderRegistryProperties registry = registry("hps", Map.of(
                "type", "shetab",
                "endpoints", List.of("10.10.10.10:9000"),
                "packager-class", "Shetab7AsciiXAPackager"
        ));

        ShetabResolvedConfig config = resolver(registry).resolve("shetab:hps", null);

        assertEquals("hps", config.provider());
        assertEquals(List.of("10.10.10.10:9000"), config.endpoints());
    }

    @Test
    void endpointAliasCanBeUsedInsteadOfEndpointsList() {
        ProviderRegistryProperties registry = registry("hps", Map.of(
                "type", "shetab",
                "endpoint", "10.10.10.10:9000",
                "packager-class", "Shetab7AsciiXAPackager"
        ));

        ShetabResolvedConfig config = resolver(registry).resolve("hps", null);

        assertEquals(List.of("10.10.10.10:9000"), config.endpoints());
    }

    @Test
    void endpointsArePreferredBeforeEndpointAlias() {
        ProviderRegistryProperties registry = registry("hps", Map.of(
                "type", "shetab",
                "endpoints", List.of("10.10.10.11:9000", "10.10.10.12:9000"),
                "endpoint", "10.10.10.10:9000",
                "packager-class", "Shetab7AsciiXAPackager"
        ));

        ShetabResolvedConfig config = resolver(registry).resolve("hps", null);

        assertEquals(List.of("10.10.10.11:9000", "10.10.10.12:9000", "10.10.10.10:9000"), config.endpoints());
    }

    @Test
    void missingMessageCustomizersProducesEmptyPipeline() {
        ProviderRegistryProperties registry = registry("hps", Map.of(
                "type", "shetab",
                "endpoint", "10.10.10.10:9000",
                "packager-class", "Shetab7AsciiXAPackager"
        ));

        ShetabResolvedConfig config = resolver(registry).resolve("hps", null);

        assertTrue(config.messageCustomizerPipeline().isEmpty());
    }

    private ShetabConfigResolver resolver(ProviderRegistryProperties registry) {
        return new ShetabConfigResolver(registry, new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of(new HpsShetabOutletProviderMessageCustomizerFactory()))));
    }

    private ProviderRegistryProperties registry(String providerCode, Map<String, Object> config) {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        registry.put(providerCode, config);
        return registry;
    }
}
