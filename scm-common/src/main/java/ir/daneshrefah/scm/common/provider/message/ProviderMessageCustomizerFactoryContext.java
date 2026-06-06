package ir.daneshrefah.scm.common.provider.message;

import java.util.Map;
import java.util.Optional;

public record ProviderMessageCustomizerFactoryContext(
        String providerCode,
        String providerType,
        String transportType,
        String serviceCode,
        String operationCode,
        String channelCode,
        String correlationId,
        String traceId,
        Map<String, Object> providerConfig,
        Object resolvedProviderConfig
) {

    public ProviderMessageCustomizerFactoryContext {
        providerConfig = providerConfig == null ? Map.of() : Map.copyOf(providerConfig);
    }

    public static ProviderMessageCustomizerFactoryContext from(ProviderMessageCustomizerContext context) {
        if (context == null) {
            throw new IllegalArgumentException("Provider customizer context is required");
        }
        return new ProviderMessageCustomizerFactoryContext(
                context.providerCode(),
                context.providerType(),
                context.transportType(),
                context.serviceCode(),
                context.operationCode(),
                context.channelCode(),
                context.correlationId(),
                context.traceId(),
                context.providerConfig(),
                context.resolvedProviderConfig()
        );
    }

    public <T> Optional<T> resolvedProviderConfig(Class<T> type) {
        if (type == null || resolvedProviderConfig == null || !type.isInstance(resolvedProviderConfig)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(resolvedProviderConfig));
    }
}
