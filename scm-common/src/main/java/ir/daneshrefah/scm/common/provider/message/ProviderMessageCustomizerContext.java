package ir.daneshrefah.scm.common.provider.message;

import java.util.Map;
import java.util.Optional;

public record ProviderMessageCustomizerContext(
        String providerCode,
        String scheme,
        String providerUri,
        String serviceCode,
        String operationCode,
        String channelCode,
        Map<String, Object> providerConfig,
        Object resolvedProviderConfig,
        String correlationId,
        String traceId
) {

    public ProviderMessageCustomizerContext {
        providerConfig = providerConfig == null ? Map.of() : Map.copyOf(providerConfig);
    }

    public ProviderMessageCustomizerContext(
            String providerCode,
            String scheme,
            String serviceCode,
            String operationCode,
            String channelCode,
            Map<String, Object> providerConfig,
            Object resolvedProviderConfig,
            String correlationId,
            String traceId
    ) {
        this(providerCode, scheme, providerUri(scheme, providerCode), serviceCode, operationCode, channelCode,
                providerConfig, resolvedProviderConfig, correlationId, traceId);
    }

    private static String providerUri(String scheme, String providerCode) {
        if (scheme == null || scheme.isBlank() || providerCode == null || providerCode.isBlank()) {
            return null;
        }
        return scheme.trim() + ":" + providerCode.trim();
    }

    public <T> Optional<T> resolvedProviderConfig(Class<T> type) {
        if (type == null || resolvedProviderConfig == null || !type.isInstance(resolvedProviderConfig)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(resolvedProviderConfig));
    }
}
