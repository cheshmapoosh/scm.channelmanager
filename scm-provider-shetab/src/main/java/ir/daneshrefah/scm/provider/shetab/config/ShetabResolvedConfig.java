package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;

import java.util.List;
import java.util.Map;

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
        List<ProviderMessageCustomizerDefinition> messageCustomizers,
        RateLimit rateLimit,
        EndpointLease endpointLease,
        Security security
) {
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
            EndpointLease endpointLease,
            Security security
    ) {
        this(provider, "shetab", endpoints, packagerClass, packagerXml, connectTimeoutMs, socketTimeoutMs,
                responseTimeoutMs, sendTimeoutMs, reconnectDelayMs, sameEndpointReconnectAttempts, queueCapacity,
                Map.of(), List.of(), rateLimit, endpointLease, security);
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

    public record Security(
            Pin pin,
            Mac mac,
            Expiry expiry,
            Cvv2 cvv2
    ) {
        public Security(Pin pin, Mac mac) {
            this(pin, mac, new Expiry(false, 14), new Cvv2(false, 48, "P92", 3, 3, 4));
        }
    }

    public record Pin(
            boolean enabled,
            String key,
            int field,
            int panField
    ) {
    }

    public record Mac(
            boolean enabled,
            String key,
            int field,
            boolean verifyResponse,
            String placeholder,
            int packedLengthBytes
    ) {
    }

    public record Expiry(
            boolean enabled,
            int field
    ) {
    }

    public record Cvv2(
            boolean enabled,
            int field,
            String tag,
            int lengthDigits,
            int minLength,
            int maxLength
    ) {
    }
}
