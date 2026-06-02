package ir.daneshrefah.scm.provider.rest.config;

public record RestProviderEndpointOverrides(
        Integer timeoutMs,
        Boolean rateLimitEnabled,
        String rateLimitBucket,
        String rateLimitKey
) {
}
