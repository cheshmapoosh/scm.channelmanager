package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ShetabResolvedConfig(
        String provider,
        String providerType,
        List<String> endpoints,
        String packagerClass,
        String packagerXml,
        int connectTimeoutMs,
        int socketTimeoutMs,
        int responseTimeoutMs,
        int sendTimeoutMs,
        int reconnectDelayMs,
        int sameEndpointReconnectAttempts,
        int queueCapacity,
        Map<String, Object> providerConfig,
        ProviderMessageCustomizerPipeline messageCustomizerPipeline,
        RateLimit rateLimit,
        EndpointLease endpointLease
) {
    public ShetabResolvedConfig {
        endpoints = endpoints == null ? List.of() : List.copyOf(endpoints);
        providerConfig = providerConfig == null ? Map.of() : Map.copyOf(providerConfig);
        messageCustomizerPipeline = messageCustomizerPipeline == null
                ? ProviderMessageCustomizerPipeline.empty()
                : messageCustomizerPipeline;
    }

    public ShetabResolvedConfig(
            String provider,
            List<String> endpoints,
            String packagerClass,
            String packagerXml,
            int connectTimeoutMs,
            int socketTimeoutMs,
            int responseTimeoutMs,
            int sendTimeoutMs,
            int reconnectDelayMs,
            int sameEndpointReconnectAttempts,
            int queueCapacity,
            RateLimit rateLimit,
            EndpointLease endpointLease
    ) {
        this(provider, "shetab", endpoints, packagerClass, packagerXml, connectTimeoutMs, socketTimeoutMs,
                responseTimeoutMs, sendTimeoutMs, reconnectDelayMs, sameEndpointReconnectAttempts, queueCapacity,
                Map.of(), ProviderMessageCustomizerPipeline.empty(), rateLimit, endpointLease);
    }

    public ShetabResolvedConfig withOverrides(ShetabEndpointOverrides overrides) {
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
                && Objects.equals(rateLimitBucket, rateLimit.bucket())
                && Objects.equals(rateLimitKey, rateLimit.key())) {
            return this;
        }
        return new ShetabResolvedConfig(provider, providerType, endpoints, packagerClass, packagerXml, connectTimeoutMs,
                socketTimeoutMs, resolvedResponseTimeoutMs, sendTimeoutMs, reconnectDelayMs,
                sameEndpointReconnectAttempts, queueCapacity, providerConfig, messageCustomizerPipeline,
                new RateLimit(rateLimitEnabled, rateLimitBucket, rateLimitKey), endpointLease);
    }

    public record RateLimit(
            boolean enabled,
            String bucket,
            String key
    ) {
    }

    public record EndpointLease(
            boolean enabled,
            long ttlMs
    ) {
    }
}
