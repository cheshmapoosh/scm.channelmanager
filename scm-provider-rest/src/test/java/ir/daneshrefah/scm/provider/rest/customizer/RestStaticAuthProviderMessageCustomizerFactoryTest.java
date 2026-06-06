package ir.daneshrefah.scm.provider.rest.customizer;

import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryContext;
import ir.daneshrefah.scm.common.provider.message.ProviderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestStaticAuthProviderMessageCustomizerFactoryTest {
    private final RestStaticAuthProviderMessageCustomizerFactory factory = new RestStaticAuthProviderMessageCustomizerFactory();

    @Test
    void basicAuthAppliesBase64AuthorizationHeader() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("BASIC");
        config.setUsername("user");
        config.setPassword("pass");

        ProviderExchange exchange = apply(config);

        assertEquals("Basic dXNlcjpwYXNz", exchange.request().headers().get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void bearerAuthAppliesBearerToken() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("BEARER");
        config.setToken("test-token");

        ProviderExchange exchange = apply(config);

        assertEquals("Bearer test-token", exchange.request().headers().get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void jwtAuthAppliesJwtToken() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("JWT");
        config.setToken("jwt-token");

        ProviderExchange exchange = apply(config);

        assertEquals("JWT jwt-token", exchange.request().headers().get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void apiKeyAuthAppliesRawTokenByDefault() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("API_KEY");
        config.setToken("api-key-token");

        ProviderExchange exchange = apply(config);

        assertEquals("api-key-token", exchange.request().headers().get(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void apiKeyAuthAppliesConfiguredPrefixAndHeaderName() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("API_KEY");
        config.setHeaderName("X-Api-Key");
        config.setPrefix("ApiKey");
        config.setToken("api-key-token");

        ProviderExchange exchange = apply(config);

        assertEquals("ApiKey api-key-token", exchange.request().headers().get("X-Api-Key"));
        assertFalse(exchange.request().headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void customHeaderNameIsHonored() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("BEARER");
        config.setHeaderName("X-Auth-Token");
        config.setToken("test-token");

        ProviderExchange exchange = apply(config);

        assertEquals("Bearer test-token", exchange.request().headers().get("X-Auth-Token"));
    }

    @Test
    void noneDoesNotAddHeader() {
        ProviderExchange exchange = apply(config("NONE"));

        assertFalse(exchange.request().headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void rejectsNonRestContext() {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = config("NONE");

        assertThrows(IllegalArgumentException.class,
                () -> factory.create(ProviderMessageCustomizerFactoryContext.from(context("shetab", "shetab")), config));
    }

    @Test
    void missingBasicUsernameOrPasswordFailsFast() {
        RestStaticAuthProviderMessageCustomizerFactory.Config missingUsername = config("BASIC");
        missingUsername.setPassword("pass");
        RestStaticAuthProviderMessageCustomizerFactory.Config missingPassword = config("BASIC");
        missingPassword.setUsername("user");

        assertThrows(IllegalArgumentException.class, () -> create(missingUsername));
        assertThrows(IllegalArgumentException.class, () -> create(missingPassword));
    }

    @Test
    void missingTokenFailsFastForTokenBasedAuthTypes() {
        assertThrows(IllegalArgumentException.class, () -> create(config("BEARER")));
        assertThrows(IllegalArgumentException.class, () -> create(config("JWT")));
        assertThrows(IllegalArgumentException.class, () -> create(config("API_KEY")));
    }

    private ProviderExchange apply(RestStaticAuthProviderMessageCustomizerFactory.Config config) {
        ProviderMessageCustomizer customizer = create(config);
        ProviderExchange exchange = new ProviderExchange(
                new ProviderRequest("POST", URI.create("https://provider.example"), Map.of(), Map.of()),
                context("rest", "rest")
        );
        customizer.beforeSend(exchange);
        return exchange;
    }

    private ProviderMessageCustomizer create(RestStaticAuthProviderMessageCustomizerFactory.Config config) {
        return factory.create(ProviderMessageCustomizerFactoryContext.from(context("rest", "rest")), config);
    }

    private RestStaticAuthProviderMessageCustomizerFactory.Config config(String type) {
        RestStaticAuthProviderMessageCustomizerFactory.Config config = new RestStaticAuthProviderMessageCustomizerFactory.Config();
        config.setType(type);
        return config;
    }

    private ProviderMessageCustomizerContext context(String providerType, String transportType) {
        return new ProviderMessageCustomizerContext(
                "hps-rest", providerType, "svc", "op", "mb", transportType, Map.of(), null, "corr", "trace");
    }
}
