package ir.daneshrefah.scm.provider.rest.config;

import java.util.List;
import java.util.Map;

public record RestProviderResolvedConfig(
        String provider,
        String baseUrl,
        int connectTimeoutMs,
        int responseTimeoutMs,
        boolean virtualThreadsEnabled,
        boolean insecureSsl,
        HttpRedirect followRedirects,
        String defaultMethod,
        Map<String, String> defaultHeaders,
        Proxy proxy,
        Auth auth,
        Security security,
        Token token
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

    public record Token(
            boolean enabled,
            String cacheName,
            String cacheKey,
            String lockName,
            int earlyRefreshSeconds,
            int defaultExpiresInSeconds,
            String method,
            String url,
            String path,
            Map<String, String> headers,
            Map<String, String> query,
            Object body,
            Map<String, String> form,
            Auth auth,
            String responseTokenField,
            String responseExpiresInField,
            String responseTokenTypeField,
            String defaultTokenType
    ) {
    }
}
