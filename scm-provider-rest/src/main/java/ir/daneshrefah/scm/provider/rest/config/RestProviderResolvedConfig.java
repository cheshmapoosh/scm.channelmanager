package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;

import java.util.List;
import java.util.Map;

public record RestProviderResolvedConfig(
        String provider,
        String providerType,
        String baseUrl,
        int connectTimeoutMs,
        int responseTimeoutMs,
        boolean virtualThreadsEnabled,
        boolean insecureSsl,
        HttpRedirect followRedirects,
        String defaultMethod,
        Map<String, String> defaultHeaders,
        Map<String, Object> providerConfig,
        List<ProviderMessageCustomizerDefinition> messageCustomizers,
        Proxy proxy,
        Auth auth,
        Security security,
        RateLimit rateLimit
) {
    public enum HttpRedirect {
        NEVER,
        NORMAL,
        ALWAYS
    }

    public enum AuthType {
        NONE,
        BASIC,
        BEARER,
        JWT,
        API_KEY
    }

    public record Proxy(
            String host,
            Integer port,
            String username,
            String password
    ) {
    }

    public record Auth(
            AuthType type,
            String headerName,
            String prefix,
            String token,
            String username,
            String password,
            boolean basicBase64
    ) {
    }

    public record Security(
            List<String> sensitiveHeaders,
            List<String> sensitiveBodyKeys,
            int maxBodyLogLength
    ) {
    }

    public record RateLimit(
            boolean enabled,
            String bucket,
            String key
    ) {
    }
}
