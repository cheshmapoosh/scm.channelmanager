package ir.daneshrefah.scm.provider.shetab.config;

public record ShetabEndpointOverrides(
        Integer timeoutMs,
        Boolean rateLimitEnabled,
        String rateLimitBucket,
        String rateLimitKey
) {
}
