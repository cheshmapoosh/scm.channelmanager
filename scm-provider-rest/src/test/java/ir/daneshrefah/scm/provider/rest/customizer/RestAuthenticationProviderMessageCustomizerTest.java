package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderRequest;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthToken;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthTokenProvider;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestAuthenticationProviderMessageCustomizerTest {

    @Test
    void factoryCreatesRestAuthUrlCustomizerOnlyForRestProvider() {
        RestAuthUrlProviderMessageCustomizerFactory factory = new RestAuthUrlProviderMessageCustomizerFactory(new StaticTokenProvider());

        ProviderMessageCustomizer customizer = factory.create(factoryContext("rest", "rest"), config("header", "Authorization", "{tokenType} {accessToken}"));

        assertEquals("rest-auth-url", factory.type());
        assertEquals(5000, customizer.order());
        assertThrows(IllegalArgumentException.class,
                () -> factory.create(factoryContext("rest", "shetab"), config("header", "Authorization", "{tokenType} {accessToken}")));
    }

    @Test
    void appliesTokenToConfiguredHeader() {
        ProviderExchange exchange = exchange(config("header", "X-Auth-Token", "{accessToken}"));
        ProviderMessageCustomizer customizer = customizer("header", "X-Auth-Token", "{accessToken}");

        customizer.beforeSend(exchange);

        assertEquals("abc", exchange.request().headers().get("X-Auth-Token"));
        assertEquals(Boolean.TRUE, exchange.getAttribute("rest.auth.applied", Boolean.class));
    }

    @Test
    void appliesTokenToConfiguredBodyField() {
        ProviderExchange exchange = exchange(config("body", "accessToken", "{accessToken}"));
        ProviderMessageCustomizer customizer = customizer("body", "accessToken", "{accessToken}");

        customizer.beforeSend(exchange);

        assertEquals("abc", exchange.request().fields().get("accessToken"));
    }

    @Test
    void appliesTokenToConfiguredQueryParameter() {
        ProviderExchange exchange = exchange(config("query", "auth_token", "{accessToken}"));
        ProviderMessageCustomizer customizer = customizer("query", "auth_token", "{accessToken}");

        customizer.beforeSend(exchange);

        assertEquals("abc", exchange.request().queryParameters().get("auth_token"));
    }

    private ProviderMessageCustomizer customizer(String location, String name, String format) {
        return new RestAuthUrlProviderMessageCustomizerFactory(new StaticTokenProvider())
                .create(factoryContext("rest", "rest"), config(location, name, format));
    }

    private ProviderExchange exchange(RestAuthUrlProviderMessageCustomizerConfig authConfig) {
        RestProviderResolvedConfig resolvedConfig = resolvedConfig();
        ProviderMessageCustomizerContext context = new ProviderMessageCustomizerContext(
                "hps-rest", "rest", "svc", "op", "mb", "rest", Map.of(), resolvedConfig, "corr", "trace");
        return new ProviderExchange(new ProviderRequest("POST", URI.create("https://provider.example/pay"), Map.of(), Map.of()), context);
    }

    private RestAuthUrlProviderMessageCustomizerConfig config(String location, String name, String format) {
        RestAuthUrlProviderMessageCustomizerConfig config = new RestAuthUrlProviderMessageCustomizerConfig();
        config.setUrl("https://provider.example/token");
        config.getCache().setName("rest_provider_token_cache");
        config.getCache().setAuthProfile("default");
        config.getCache().setCredentialKey("hps-rest");
        config.getApply().setLocation(location);
        config.getApply().setName(name);
        config.getApply().setFormat(format);
        return config;
    }

    private ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext factoryContext(
            String providerType,
            String transportType
    ) {
        return new ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext(
                "hps-rest", providerType, transportType, "svc", "op", "mb", "corr", "trace", Map.of(), resolvedConfig());
    }

    private RestProviderResolvedConfig resolvedConfig() {
        return new RestProviderResolvedConfig(
                "hps-rest",
                "rest",
                "https://provider.example",
                3000,
                6000,
                true,
                false,
                RestProviderResolvedConfig.HttpRedirect.NORMAL,
                "POST",
                Map.of(),
                Map.of(),
                ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline.empty(),
                new RestProviderResolvedConfig.Proxy(null, null, null, null),
                new RestProviderResolvedConfig.Security(List.of("authorization"), List.of("token"), 400),
                new RestProviderResolvedConfig.RateLimit(false, null, "provider-operation")
        );
    }

    private static final class StaticTokenProvider implements ProviderAuthTokenProvider {
        @Override
        public ProviderAuthToken resolveToken(
                RestProviderResolvedConfig providerConfig,
                RestAuthUrlProviderMessageCustomizerConfig authConfig,
                ProviderMessageCustomizerContext context
        ) {
            return new ProviderAuthToken("abc", "Bearer");
        }
    }
}
