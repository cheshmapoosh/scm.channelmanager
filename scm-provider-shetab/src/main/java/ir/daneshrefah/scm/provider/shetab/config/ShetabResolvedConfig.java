package ir.daneshrefah.scm.provider.shetab.config;

import java.util.List;

public record ShetabResolvedConfig(
        String provider,
        String host,
        int port,
        String localAddress,
        List<Integer> localPorts,
        String channelType,
        int lengthDigits,
        String packagerClass,
        String packagerXml,
        int connectTimeoutMs,
        int socketTimeoutMs,
        int responseTimeoutMs,
        int sendTimeoutMs,
        int reconnectDelayMs,
        int queueCapacity,
        RateLimit rateLimit,
        PortLease portLease
) {
    public record RateLimit(
            boolean enabled,
            String bucket,
            String key
    ) {
    }

    public record PortLease(
            boolean enabled,
            long ttlMs
    ) {
    }
}
