package ir.daneshrefah.scm.provider.rest.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestProviderConfigResolverTest {

    @Test
    void resolvesTypedProviderNameAndMergesDefaults() {
        RestProviderProperties properties = new RestProviderProperties();
        properties.getDefaults().setBaseUrl("https://default.example");
        properties.getDefaults().getHeaders().put("Accept", "application/json");
        properties.getDefaults().setDefaultMethod("POST");
        properties.getDefaults().getAuth().setType("BEARER");
        properties.getDefaults().getAuth().setToken("default-token");
        properties.getDefaults().getToken().setEnabled(true);
        properties.getDefaults().getToken().setPath("/connect/token");
        properties.getDefaults().getToken().getForm().put("grant_type", "client_credentials");

        RestProviderProperties.Instance hps = new RestProviderProperties.Instance();
        hps.setBaseUrl("https://hps.example");
        hps.setResponseTimeoutMs(9000);
        hps.getHeaders().put("X-Provider", "hps");
        properties.getProviders().put("hps", hps);

        RestProviderResolvedConfig config = new RestProviderConfigResolver(properties)
                .resolve("rest-provider:hps", new RestProviderEndpointOverrides(7000, null, null, null));

        assertEquals("hps", config.provider());
        assertEquals("https://hps.example", config.baseUrl());
        assertEquals(7000, config.responseTimeoutMs());
        assertEquals("POST", config.defaultMethod());
        assertEquals("application/json", config.defaultHeaders().get("Accept"));
        assertEquals("hps", config.defaultHeaders().get("X-Provider"));
        assertEquals(RestProviderResolvedConfig.AuthType.BEARER, config.auth().type());
        assertEquals("default-token", config.auth().token());
        assertTrue(config.virtualThreadsEnabled());
        assertTrue(config.token().enabled());
        assertEquals("/connect/token", config.token().path());
        assertEquals("client_credentials", config.token().form().get("grant_type"));
    }

    @Test
    void authTypeCanBeOverriddenPerProvider() {
        RestProviderProperties properties = new RestProviderProperties();
        properties.getDefaults().setBaseUrl("https://default.example");
        properties.getDefaults().getAuth().setType("BEARER");
        properties.getDefaults().getAuth().setToken("token-a");

        RestProviderProperties.Instance partner = new RestProviderProperties.Instance();
        partner.setBaseUrl("https://partner.example");
        partner.getAuth().setType("BASIC");
        partner.getAuth().setUsername("user");
        partner.getAuth().setPassword("pass");
        partner.getToken().setEnabled(true);
        partner.getToken().setPath("/oauth/token");
        partner.getToken().setResponseTokenField("token");
        partner.setHeaders(Map.of("X-App", "scm"));
        properties.getProviders().put("partner", partner);

        RestProviderResolvedConfig config = new RestProviderConfigResolver(properties).resolve("rest:partner", null);

        assertEquals(RestProviderResolvedConfig.AuthType.BASIC, config.auth().type());
        assertEquals("user", config.auth().username());
        assertEquals("pass", config.auth().password());
        assertEquals("scm", config.defaultHeaders().get("X-App"));
        assertTrue(config.token().enabled());
        assertEquals("/oauth/token", config.token().path());
        assertEquals("token", config.token().responseTokenField());
    }

    @Test
    void rateLimitCanBeConfiguredAndOverriddenLikeNab() {
        RestProviderProperties properties = new RestProviderProperties();
        properties.getDefaults().setBaseUrl("https://default.example");
        properties.getDefaults().getRateLimit().setEnabled(false);
        properties.getDefaults().getRateLimit().setBucket("rest-default");
        properties.getDefaults().getRateLimit().setKey("provider");

        RestProviderProperties.Instance hps = new RestProviderProperties.Instance();
        hps.setBaseUrl("https://hps.example");
        hps.getRateLimit().setEnabled(true);
        hps.getRateLimit().setBucket("rest-hps");
        hps.getRateLimit().setKey("provider-operation");
        properties.getProviders().put("hps", hps);

        RestProviderEndpointOverrides overrides = new RestProviderEndpointOverrides(6000, true, "rest-override", "operation");
        RestProviderResolvedConfig config = new RestProviderConfigResolver(properties).resolve("hps", overrides);

        assertEquals(true, config.rateLimit().enabled());
        assertEquals("rest-override", config.rateLimit().bucket());
        assertEquals("operation", config.rateLimit().key());
    }

    @Test
    void endpointAliasCanBeUsedInsteadOfBaseUrl() {
        RestProviderProperties properties = new RestProviderProperties();
        RestProviderProperties.Instance hps = new RestProviderProperties.Instance();
        hps.setEndpoint("https://hps.example");
        properties.getProviders().put("hps", hps);

        RestProviderResolvedConfig config = new RestProviderConfigResolver(properties).resolve("hps", null);

        assertEquals("https://hps.example", config.baseUrl());
    }
}
