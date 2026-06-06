package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.config.ProviderConfigurationBinder;
import ir.daneshrefah.scm.provider.rest.customizer.RestAuthUrlProviderMessageCustomizerConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderConfigurationBinderRestTest {

    @Test
    void restAuthUrlBindingPreservesBusinessMapKeysAndBindsDashedProperties() {
        RestAuthUrlProviderMessageCustomizerConfig config = ProviderConfigurationBinder.bind(Map.of(
                "url", "https://provider/auth/token",
                "method", "POST",
                "request", Map.of(
                        "headers", Map.of(
                                "Content-Type", "application/x-www-form-urlencoded",
                                "X-Client-Id", "abc"
                        ),
                        "form", Map.of(
                                "grant_type", "client_credentials",
                                "client_id", "test-client",
                                "client_secret", "test-secret"
                        ),
                        "query", Map.of("scope", "card:read")
                ),
                "response", Map.of(
                        "token-field", "access_token",
                        "expires-in-field", "expires_in",
                        "token-type-field", "token_type"
                ),
                "cache", Map.of(
                        "name", "rest_provider_token_cache",
                        "auth-profile", "default",
                        "credential-key", "hps-rest"
                ),
                "lock", Map.of(
                        "wait-timeout", "3s",
                        "retry-delay", "100ms"
                ),
                "apply", Map.of(
                        "location", "header",
                        "name", "Authorization",
                        "format", "{tokenType} {accessToken}"
                )
        ), RestAuthUrlProviderMessageCustomizerConfig.class, "rest-auth-url test");

        assertEquals("application/x-www-form-urlencoded", config.request().getHeaders().get("Content-Type"));
        assertEquals("abc", config.request().getHeaders().get("X-Client-Id"));
        assertEquals("client_credentials", config.request().getForm().get("grant_type"));
        assertEquals("test-client", config.request().getForm().get("client_id"));
        assertEquals("test-secret", config.request().getForm().get("client_secret"));
        assertEquals("access_token", config.response().getTokenField());
        assertEquals("expires_in", config.response().getExpiresInField());
        assertEquals("token_type", config.response().getTokenTypeField());
        assertEquals(Duration.ofSeconds(3), config.lock().waitTimeoutDuration());
        assertEquals(Duration.ofMillis(100), config.lock().retryDelayDuration());
    }

    @Test
    void restProviderInstanceBindingPreservesProviderConfigAndMessageCustomizerConfigKeys() {
        RestProviderInstanceProperties config = ProviderConfigurationBinder.bind(Map.of(
                "type", "rest",
                "base-url", "https://hps-rest.example.ir",
                "connect-timeout-ms", 3000,
                "response-timeout-ms", 6000,
                "provider-config", Map.of(
                        "Content-Type", "application/json",
                        "merchant_id", "123456",
                        "client-id", "hps"
                ),
                "message-customizers", List.of(Map.of(
                        "type", "hps-rest-outlet",
                        "order", 101,
                        "config", Map.of(
                                "body_field", "outlet",
                                "X-Outlet-Id", "123456789012345"
                        )
                ))
        ), RestProviderInstanceProperties.class, "rest provider test");

        assertEquals("https://hps-rest.example.ir", config.getBaseUrl());
        assertEquals(3000, config.getConnectTimeoutMs());
        assertEquals(6000, config.getResponseTimeoutMs());
        assertEquals("application/json", config.getProviderConfig().get("Content-Type"));
        assertEquals("123456", config.getProviderConfig().get("merchant_id"));
        assertEquals("hps", config.getProviderConfig().get("client-id"));
        assertEquals(1, config.getMessageCustomizers().size());
        assertEquals("hps-rest-outlet", config.getMessageCustomizers().getFirst().getType());
        assertEquals(101, config.getMessageCustomizers().getFirst().getOrder());
        assertEquals("outlet", config.getMessageCustomizers().getFirst().getConfig().get("body_field"));
        assertEquals("123456789012345", config.getMessageCustomizers().getFirst().getConfig().get("X-Outlet-Id"));
    }
}
