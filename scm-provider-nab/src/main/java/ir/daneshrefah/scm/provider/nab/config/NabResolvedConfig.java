package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;

import java.util.List;
import java.util.Map;

public record NabResolvedConfig(
        String provider,
        String providerType,
        String endpoint,
        String protocol,
        int connectTimeoutMs,
        int socketTimeoutMs,
        int responseTimeoutMs,
        int responseIdleTimeoutMs,
        int ackLengthBytes,
        String charset,
        String userId,
        String password,
        String defaultServiceCode,
        Map<String, String> serviceCodesByTerminalType,
        Map<String, String> serviceCodesByChannelCode,
        Map<String, List<NabFieldSpec>> headerFieldsByProtocol,
        RateLimit rateLimit,
        RqUid rqUid,
        CharacterNormalization characterNormalization,
        boolean wireLogEnabled
) {
    public NabResolvedConfig(
            String provider,
            String endpoint,
            String protocol,
            int connectTimeoutMs,
            int socketTimeoutMs,
            int responseTimeoutMs,
            int responseIdleTimeoutMs,
            int ackLengthBytes,
            String charset,
            String userId,
            String password,
            String defaultServiceCode,
            Map<String, String> serviceCodesByTerminalType,
            Map<String, String> serviceCodesByChannelCode,
            Map<String, List<NabFieldSpec>> headerFieldsByProtocol,
            RateLimit rateLimit,
            RqUid rqUid,
            CharacterNormalization characterNormalization,
            boolean wireLogEnabled
    ) {
        this(provider, "nab", endpoint, protocol, connectTimeoutMs, socketTimeoutMs, responseTimeoutMs,
                responseIdleTimeoutMs, ackLengthBytes, charset, userId, password, defaultServiceCode,
                serviceCodesByTerminalType, serviceCodesByChannelCode, headerFieldsByProtocol, rateLimit,
                rqUid, characterNormalization, wireLogEnabled);
    }

    public record RateLimit(
            boolean enabled,
            String bucket,
            String key
    ) {
    }

    public record RqUid(
            int length,
            String type
    ) {
    }

    public record CharacterNormalization(
            boolean enabled,
            Map<String, String> replacements
    ) {
    }
}
