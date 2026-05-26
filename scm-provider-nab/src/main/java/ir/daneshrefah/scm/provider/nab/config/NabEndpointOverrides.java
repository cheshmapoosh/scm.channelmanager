package ir.daneshrefah.scm.provider.nab.config;

public record NabEndpointOverrides(
        Integer timeoutMs,
        String charset
) {
}
