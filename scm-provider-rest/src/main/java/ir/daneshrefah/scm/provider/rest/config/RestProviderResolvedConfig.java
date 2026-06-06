package ir.daneshrefah.scm.provider.rest.config;

import java.time.Duration;
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
        Map<String, Object> providerConfig,
        Customizers customizers,
        Proxy proxy,
        Auth auth,
        Security security,
        Token token,
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

    public record Customizers(
            boolean authentication
    ) {
    }

    public record Token(
            boolean enabled,
            String authProfile,
            String credentialKey,
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
            String defaultTokenType,
            TokenCache cache,
            TokenLock lock,
            TokenApply apply
    ) {
    }

    public record TokenCache(
            boolean enabled,
            String mode,
            String keyPrefix,
            Duration refreshSkew,
            Duration ttlSkew
    ) {
    }

    public record TokenLock(
            boolean enabled,
            String keyPrefix,
            Duration waitTimeout,
            Duration leaseTime,
            Duration retryDelay
    ) {
    }

    public record TokenApply(
            TokenApplyLocation location,
            String name,
            String format
    ) {
    }

    public enum TokenApplyLocation {
        HEADER,
        BODY,
        QUERY
    }

    public record RateLimit(
            boolean enabled,
            String bucket,
            String key
    ) {
    }
}
