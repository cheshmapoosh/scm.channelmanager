package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderRequest;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthToken;
import ir.daneshrefah.scm.provider.rest.token.ProviderAuthTokenProvider;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestAuthenticationProviderMessageCustomizerTest {

    @Test
    void supportsOnlyRestTransportWithTokenAuthenticationEnabled() {
        RestAuthenticationProviderMessageCustomizer customizer = new RestAuthenticationProviderMessageCustomizer(new StaticTokenProvider());

        assertTrue(customizer.supports(context("rest", config(RestProviderResolvedConfig.TokenApplyLocation.HEADER))));
        assertFalse(customizer.supports(context("shetab", config(RestProviderResolvedConfig.TokenApplyLocation.HEADER))));
        assertFalse(customizer.supports(context("rest", config(false, RestProviderResolvedConfig.AuthType.BEARER,
                RestProviderResolvedConfig.TokenApplyLocation.HEADER))));
        assertFalse(customizer.supports(context("rest", config(true, RestProviderResolvedConfig.AuthType.BASIC,
                RestProviderResolvedConfig.TokenApplyLocation.HEADER))));
    }

    @Test
    void appliesTokenToConfiguredHeader() throws Exception {
        RestAuthenticationProviderMessageCustomizer customizer = new RestAuthenticationProviderMessageCustomizer(new StaticTokenProvider());
        RestProviderResolvedConfig config = config(RestProviderResolvedConfig.TokenApplyLocation.HEADER, "X-Auth-Token", "{accessToken}");
        ProviderExchange exchange = exchange(config);

        customizer.beforeSend(exchange);

        assertEquals("token-1", exchange.request().headers().get("X-Auth-Token"));
        assertEquals(Boolean.TRUE, exchange.getAttribute("rest.auth.applied", Boolean.class));
    }

    @Test
    void appliesTokenToConfiguredBodyField() throws Exception {
        RestAuthenticationProviderMessageCustomizer customizer = new RestAuthenticationProviderMessageCustomizer(new StaticTokenProvider());
        RestProviderResolvedConfig config = config(RestProviderResolvedConfig.TokenApplyLocation.BODY, "accessToken", "{accessToken}");
        ProviderExchange exchange = exchange(config);

        customizer.beforeSend(exchange);

        assertEquals("token-1", exchange.request().bodyAsMap().get("accessToken"));
    }

    @Test
    void appliesTokenToConfiguredQueryParameter() throws Exception {
        RestAuthenticationProviderMessageCustomizer customizer = new RestAuthenticationProviderMessageCustomizer(new StaticTokenProvider());
        RestProviderResolvedConfig config = config(RestProviderResolvedConfig.TokenApplyLocation.QUERY, "auth_token", "{accessToken}");
        ProviderExchange exchange = exchange(config);

        customizer.beforeSend(exchange);

        assertEquals("token-1", exchange.request().queryParameters().get("auth_token"));
    }

    private ProviderExchange exchange(RestProviderResolvedConfig config) throws Exception {
        ProviderRequest request = new ProviderRequest("POST", new URI("https://provider.example/do"), Map.of(), new java.util.LinkedHashMap<String, Object>());
        return new ProviderExchange(request, context("rest", config));
    }

    private ProviderMessageCustomizerContext context(String transportType, RestProviderResolvedConfig config) {
        return new ProviderMessageCustomizerContext(
                config.provider(),
                "svc",
                "op",
                "mb",
                transportType,
                config.providerConfig(),
                config,
                "correlation-1",
                "trace-1"
        );
    }

    private RestProviderResolvedConfig config(RestProviderResolvedConfig.TokenApplyLocation location) {
        return config(true, RestProviderResolvedConfig.AuthType.BEARER, location);
    }

    private RestProviderResolvedConfig config(
            RestProviderResolvedConfig.TokenApplyLocation location,
            String applyName,
            String applyFormat
    ) {
        return config(true, RestProviderResolvedConfig.AuthType.BEARER, location, applyName, applyFormat);
    }

    private RestProviderResolvedConfig config(
            boolean authCustomizer,
            RestProviderResolvedConfig.AuthType authType,
            RestProviderResolvedConfig.TokenApplyLocation location
    ) {
        return config(authCustomizer, authType, location, "Authorization", "{tokenType} {accessToken}");
    }

    private RestProviderResolvedConfig config(
            boolean authCustomizer,
            RestProviderResolvedConfig.AuthType authType,
            RestProviderResolvedConfig.TokenApplyLocation location,
            String applyName,
            String applyFormat
    ) {
        return new RestProviderResolvedConfig(
                "hps",
                "https://provider.example",
                3000,
                6000,
                true,
                false,
                RestProviderResolvedConfig.HttpRedirect.NORMAL,
                "POST",
                Map.of(),
                Map.of("outlet", "001"),
                new RestProviderResolvedConfig.Customizers(authCustomizer),
                new RestProviderResolvedConfig.Proxy(null, null, null, null),
                new RestProviderResolvedConfig.Auth(authType, "Authorization", null, null, null, null, true),
                new RestProviderResolvedConfig.Security(java.util.List.of("authorization"), java.util.List.of("token"), 400),
                new RestProviderResolvedConfig.Token(
                        true,
                        "default",
                        "credential-a",
                        "rest_provider_token_cache",
                        "access-token",
                        "rest-provider-token",
                        30,
                        300,
                        "POST",
                        null,
                        "/token",
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        new RestProviderResolvedConfig.Auth(RestProviderResolvedConfig.AuthType.NONE, "Authorization", null, null, null, null, true),
                        "access_token",
                        "expires_in",
                        "token_type",
                        "Bearer",
                        new RestProviderResolvedConfig.TokenCache(true, "centralized", "provider-token", Duration.ofSeconds(30), Duration.ofSeconds(5)),
                        new RestProviderResolvedConfig.TokenLock(true, "provider-token-refresh-lock", Duration.ofSeconds(3), Duration.ofSeconds(10), Duration.ofMillis(100)),
                        new RestProviderResolvedConfig.TokenApply(location, applyName, applyFormat)
                ),
                new RestProviderResolvedConfig.RateLimit(false, "rest-default", "provider")
        );
    }

    private static final class StaticTokenProvider implements ProviderAuthTokenProvider {
        @Override
        public ProviderAuthToken resolveToken(RestProviderResolvedConfig config, ProviderMessageCustomizerContext context) {
            return new ProviderAuthToken("token-1", "Bearer");
        }
    }
}
