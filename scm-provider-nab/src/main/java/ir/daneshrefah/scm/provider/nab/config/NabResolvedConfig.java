package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipeline;
import ir.daneshrefah.scm.provider.nab.domain.NabFieldSpec;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        ProviderMessageCustomizerPipeline messageCustomizerPipeline,
        RateLimit rateLimit,
        RqUid rqUid,
        CharacterNormalization characterNormalization,
        boolean wireLogEnabled
) {
    public NabResolvedConfig {
        serviceCodesByTerminalType = serviceCodesByTerminalType == null ? Map.of() : Map.copyOf(serviceCodesByTerminalType);
        serviceCodesByChannelCode = serviceCodesByChannelCode == null ? Map.of() : Map.copyOf(serviceCodesByChannelCode);
        headerFieldsByProtocol = headerFieldsByProtocol == null ? Map.of() : Map.copyOf(headerFieldsByProtocol);
        messageCustomizerPipeline = messageCustomizerPipeline == null
                ? ProviderMessageCustomizerPipeline.empty()
                : messageCustomizerPipeline;
    }

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
                serviceCodesByTerminalType, serviceCodesByChannelCode, headerFieldsByProtocol,
                ProviderMessageCustomizerPipeline.empty(), rateLimit, rqUid, characterNormalization, wireLogEnabled);
    }

    public NabResolvedConfig withOverrides(NabEndpointOverrides overrides) {
        if (overrides == null) {
            return this;
        }
        int resolvedResponseTimeoutMs = overrides.timeoutMs() == null ? responseTimeoutMs : overrides.timeoutMs();
        String resolvedCharset = overrides.charset() == null || overrides.charset().isBlank() ? charset : overrides.charset();
        boolean rateLimitEnabled = overrides.rateLimitEnabled() == null ? rateLimit.enabled() : overrides.rateLimitEnabled();
        String rateLimitBucket = overrides.rateLimitBucket() == null || overrides.rateLimitBucket().isBlank()
                ? rateLimit.bucket()
                : overrides.rateLimitBucket();
        String rateLimitKey = overrides.rateLimitKey() == null || overrides.rateLimitKey().isBlank()
                ? rateLimit.key()
                : overrides.rateLimitKey();
        if (resolvedResponseTimeoutMs == responseTimeoutMs
                && Objects.equals(resolvedCharset, charset)
                && rateLimitEnabled == rateLimit.enabled()
                && Objects.equals(rateLimitBucket, rateLimit.bucket())
                && Objects.equals(rateLimitKey, rateLimit.key())) {
            return this;
        }
        return new NabResolvedConfig(provider, providerType, endpoint, protocol, connectTimeoutMs, socketTimeoutMs,
                resolvedResponseTimeoutMs, responseIdleTimeoutMs, ackLengthBytes, resolvedCharset, userId, password,
                defaultServiceCode, serviceCodesByTerminalType, serviceCodesByChannelCode, headerFieldsByProtocol,
                messageCustomizerPipeline, new RateLimit(rateLimitEnabled, rateLimitBucket, rateLimitKey), rqUid,
                characterNormalization, wireLogEnabled);
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
        public CharacterNormalization {
            replacements = replacements == null ? Map.of() : Map.copyOf(replacements);
        }
    }
}
