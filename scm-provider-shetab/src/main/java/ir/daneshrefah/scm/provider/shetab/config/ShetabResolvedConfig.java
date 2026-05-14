package ir.daneshrefah.scm.provider.shetab.config;

import java.util.List;

public record ShetabResolvedConfig(
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
            Mac mac
    ) {
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
}
