package ir.daneshrefah.scm.provider.nab.config;

public record NabEndpointOverrides(
        Integer timeoutMs,
        String charset,
        Boolean rateLimitEnabled,
        String rateLimitBucket,
        String rateLimitKey
) {
}
