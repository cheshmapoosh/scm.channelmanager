package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestProviderConfigResolverTest {

    @Test
    void resolvesUnifiedRestProviderWithoutDefaults() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider hps = new ProviderRegistryProperties.Provider();
        hps.setType("rest");
        hps.setBaseUrl("https://hps.example");
        hps.setResponseTimeoutMs(9000);
        hps.getHeaders().put("Accept", "application/json");
        hps.getHeaders().put("X-Provider", "hps");
        hps.getRateLimit().setEnabled(true);
        hps.getRateLimit().setBucket("hps-rest");
        hps.getRateLimit().setKey("provider-operation");
        ProviderMessageCustomizerDefinition customizer = new ProviderMessageCustomizerDefinition();
        customizer.setType("rest-auth-url");
        customizer.setConfig(Map.of("url", "https://hps.example/token"));
        hps.getMessageCustomizers().add(customizer);
        registry.getProviders().put("hps-rest", hps);

        RestProviderResolvedConfig config = new RestProviderConfigResolver(registry, new RestProviderProperties())
                .resolve("rest:hps-rest", new RestProviderEndpointOverrides(7000, null, null, null));

        assertEquals("hps-rest", config.provider());
        assertEquals("rest", config.providerType());
        assertEquals("https://hps.example", config.baseUrl());
        assertEquals(7000, config.responseTimeoutMs());
        assertEquals("application/json", config.defaultHeaders().get("Accept"));
        assertEquals("hps", config.defaultHeaders().get("X-Provider"));
        assertEquals("hps-rest", config.rateLimit().bucket());
        assertEquals("rest-auth-url", config.messageCustomizers().getFirst().getType());
    }

    @Test
    void failsFastWhenUnifiedProviderTypeIsNotRest() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider provider = new ProviderRegistryProperties.Provider();
        provider.setType("shetab");
        provider.setBaseUrl("https://wrong.example");
        registry.getProviders().put("wrong", provider);

        assertThrows(IllegalArgumentException.class,
                () -> new RestProviderConfigResolver(registry, new RestProviderProperties()).resolve("wrong", null));
    }

    @Test
    void missingUnifiedBaseUrlFailsFast() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider provider = new ProviderRegistryProperties.Provider();
        provider.setType("rest");
        registry.getProviders().put("missing", provider);

        assertThrows(IllegalArgumentException.class,
                () -> new RestProviderConfigResolver(registry, new RestProviderProperties()).resolve("missing", null));
    }

    @Test
    void legacyTokenConfigMapsToDeprecatedRestAuthUrlCustomizer() {
        RestProviderProperties legacy = new RestProviderProperties();
        RestProviderProperties.Instance hps = new RestProviderProperties.Instance();
        hps.setBaseUrl("https://hps.example");
        hps.getToken().setEnabled(true);
        hps.getToken().setPath("/oauth/token");
        hps.getToken().setCacheName("rest_provider_token_cache");
        hps.getToken().setAuthProfile("default");
        hps.getToken().setCredentialKey("hps");
        legacy.getProviders().put("hps", hps);

        RestProviderResolvedConfig config = new RestProviderConfigResolver(new ProviderRegistryProperties(), legacy).resolve("hps", null);

        assertEquals(1, config.messageCustomizers().size());
        assertEquals("rest-auth-url", config.messageCustomizers().getFirst().getType());
        assertEquals("/oauth/token", config.messageCustomizers().getFirst().config().get("path"));
    }
}
