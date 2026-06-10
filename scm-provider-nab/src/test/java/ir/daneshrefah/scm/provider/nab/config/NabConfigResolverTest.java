package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NabConfigResolverTest {

    @Test
    void resolvesUnifiedNabProviderWithoutDefaults() {
        ProviderRegistryProperties registry = registry("nab-atps", baseConfig());

        NabResolvedConfig config = resolver(registry).resolve("nab-atps", null);

        assertEquals("nab-atps", config.provider());
        assertEquals("scm-nab", config.scheme());
        assertEquals("ATPS", config.protocol());
        assertEquals("127.0.0.1:9999", config.endpoint());
        assertEquals(2, config.headerFieldsByProtocol().get("ATPS").size());
        assertTrue(config.messageCustomizerPipeline().isEmpty());
    }

    @Test
    void unifiedNabProviderRequiresInstanceHeaderFields() {
        Map<String, Object> config = baseConfig();
        config.remove("header-fields");
        ProviderRegistryProperties registry = registry("nab-atps", config);

        assertThrows(IllegalArgumentException.class, () -> resolver(registry).resolve("nab-atps", null));
    }

    @Test
    void rejectsNonNabProviderScheme() {
        Map<String, Object> config = baseConfig();
        config.put("scheme", "rest-provider");
        ProviderRegistryProperties registry = registry("nab-atps", config);

        assertThrows(IllegalArgumentException.class, () -> resolver(registry).resolve("nab-atps", null));
    }

    @Test
    void resolvesTypedProviderNameAndExplicitInstanceSettings() {
        Map<String, Object> config = baseConfig();
        config.put("connect-timeout-ms", 1111);
        config.put("charset", "windows-1256");
        config.put("service-codes-by-terminal-type", Map.of("ATM", "01"));
        ProviderRegistryProperties registry = registry("core", config);

        NabResolvedConfig resolved = resolver(registry).resolve("scm-nab:core", null);

        assertEquals("core", resolved.provider());
        assertEquals(1111, resolved.connectTimeoutMs());
        assertEquals("windows-1256", resolved.charset());
        assertEquals("01", resolved.serviceCodesByTerminalType().get("ATM"));
    }

    @Test
    void headerFieldsCanBeConfiguredPerProtocol() {
        Map<String, Object> config = baseConfig();
        config.remove("header-fields");
        config.put("header-fields-by-protocol", Map.of(
                "ATPI", List.of(
                        Map.of("name", "protocol", "length", 4, "required", true),
                        Map.of("name", "clientAddress", "length", 20, "required", true)
                )
        ));
        config.put("protocol", "ATPI");
        ProviderRegistryProperties registry = registry("core", config);

        NabResolvedConfig resolved = resolver(registry).resolve("core", null);

        assertEquals(2, resolved.headerFieldsByProtocol().get("ATPI").size());
        assertEquals(20, resolved.headerFieldsByProtocol().get("ATPI").get(1).length());
    }

    @Test
    void missingRequiredProviderSpecificConfigFailsFast() {
        Map<String, Object> config = baseConfig();
        config.remove("password");
        ProviderRegistryProperties registry = registry("core", config);

        assertThrows(IllegalArgumentException.class, () -> resolver(registry).resolve("core", null));
    }

    @Test
    void resolvesRateLimitAndAppliesOverrides() {
        Map<String, Object> config = baseConfig();
        config.put("rate-limit", Map.of("enabled", false, "bucket", "core-default", "key", "provider"));
        ProviderRegistryProperties registry = registry("core", config);

        NabEndpointOverrides overrides = new NabEndpointOverrides(1000, "windows-1252", true, "bucket-x", "operation");
        NabResolvedConfig resolved = resolver(registry).resolve("core", overrides);

        assertTrue(resolved.rateLimit().enabled());
        assertEquals("bucket-x", resolved.rateLimit().bucket());
        assertEquals("operation", resolved.rateLimit().key());
        assertEquals(1000, resolved.responseTimeoutMs());
    }

    private NabConfigResolver resolver(ProviderRegistryProperties registry) {
        return new NabConfigResolver(registry, new ProviderMessageCustomizerPipelineFactory(
                new ProviderMessageCustomizerFactoryRegistry(List.of())));
    }

    private ProviderRegistryProperties registry(String providerCode, Map<String, Object> config) {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        registry.put(providerCode, config);
        return registry;
    }

    private Map<String, Object> baseConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("scheme", "scm-nab");
        config.put("protocol", "ATPS");
        config.put("endpoint", "127.0.0.1:9999");
        config.put("user-id", "999998");
        config.put("password", "secret");
        config.put("rq-uid", Map.of("length", 16, "type", "NUMERIC"));
        config.put("header-fields", List.of(
                Map.of("name", "protocol", "length", 4, "required", true),
                Map.of("name", "command", "length", 2, "required", true)
        ));
        return config;
    }
}
