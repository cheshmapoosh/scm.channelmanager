package ir.daneshrefah.scm.common.provider.message;

import java.util.Map;
import java.util.Optional;

public record ProviderMessageCustomizerContext(
        String providerCode,
        String serviceCode,
        String operationCode,
        String channelCode,
        String transportType,
        Map<String, Object> providerConfig,
        Object resolvedProviderConfig,
        String correlationId,
        String traceId
) {

    public ProviderMessageCustomizerContext {
        providerConfig = providerConfig == null ? Map.of() : Map.copyOf(providerConfig);
    }

    public <T> Optional<T> resolvedProviderConfig(Class<T> type) {
        if (type == null || resolvedProviderConfig == null || !type.isInstance(resolvedProviderConfig)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(resolvedProviderConfig));
    }
}
