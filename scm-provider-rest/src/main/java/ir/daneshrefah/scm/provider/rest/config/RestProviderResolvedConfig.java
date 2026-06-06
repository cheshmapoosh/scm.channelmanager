package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;

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
        ProviderMessageCustomizerPipeline messageCustomizerPipeline,
        Proxy proxy,
        Security security,
        RateLimit rateLimit
) {
    public RestProviderResolvedConfig {
        defaultHeaders = defaultHeaders == null ? Map.of() : Map.copyOf(defaultHeaders);
        providerConfig = providerConfig == null ? Map.of() : Map.copyOf(providerConfig);
        messageCustomizerPipeline = messageCustomizerPipeline == null
                ? ProviderMessageCustomizerPipeline.empty()
                : messageCustomizerPipeline;
    }

    public RestProviderResolvedConfig withOverrides(RestProviderEndpointOverrides overrides) {
        if (overrides == null) {
            return this;
        }
        int resolvedResponseTimeoutMs = overrides.timeoutMs() == null ? responseTimeoutMs : overrides.timeoutMs();
        boolean rateLimitEnabled = overrides.rateLimitEnabled() == null ? rateLimit.enabled() : overrides.rateLimitEnabled();
        String rateLimitBucket = overrides.rateLimitBucket() == null || overrides.rateLimitBucket().isBlank()
                ? rateLimit.bucket()
                : overrides.rateLimitBucket();
        String rateLimitKey = overrides.rateLimitKey() == null || overrides.rateLimitKey().isBlank()
                ? rateLimit.key()
                : overrides.rateLimitKey();
        if (resolvedResponseTimeoutMs == responseTimeoutMs
                && rateLimitEnabled == rateLimit.enabled()
                && java.util.Objects.equals(rateLimitBucket, rateLimit.bucket())
                && java.util.Objects.equals(rateLimitKey, rateLimit.key())) {
            return this;
        }
        return new RestProviderResolvedConfig(provider, providerType, baseUrl, connectTimeoutMs, resolvedResponseTimeoutMs,
                virtualThreadsEnabled, insecureSsl, followRedirects, defaultMethod, defaultHeaders, providerConfig,
                messageCustomizerPipeline, proxy, security,
                new RateLimit(rateLimitEnabled, rateLimitBucket, rateLimitKey));
    }

    public enum HttpRedirect {
        NEVER,
        NORMAL,
        ALWAYS
    }

    public record Proxy(
            String host,
            Integer port,
            String username,
            String password
    ) {
    }

    public record Security(
            List<String> sensitiveHeaders,
            List<String> sensitiveBodyKeys,
            int maxBodyLogLength
    ) {
        public Security {
            sensitiveHeaders = sensitiveHeaders == null ? List.of() : List.copyOf(sensitiveHeaders);
            sensitiveBodyKeys = sensitiveBodyKeys == null ? List.of() : List.copyOf(sensitiveBodyKeys);
        }
    }

    public record RateLimit(
            boolean enabled,
            String bucket,
            String key
    ) {
    }
}
