package ir.daneshrefah.scm.common.provider.message;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class ProviderMessageCustomizerExecutor {
    private final List<ProviderMessageCustomizer> customizers;

    public ProviderMessageCustomizerExecutor(Collection<ProviderMessageCustomizer> customizers) {
        this.customizers = customizers == null ? List.of() : List.copyOf(customizers);
    }

    public List<ProviderMessageCustomizer> matchedCustomizers(ProviderMessageCustomizerContext context) {
        return customizers.stream()
                .filter(customizer -> customizer.supports(context))
                .sorted(Comparator
                        .comparingInt(ProviderMessageCustomizer::order)
                        .thenComparing(customizer -> customizer.getClass().getName()))
                .toList();
    }

    public void beforeSend(ProviderExchange exchange, List<ProviderMessageCustomizer> matchedCustomizers) {
        execute(exchange, matchedCustomizers, true);
    }

    public void afterReceive(ProviderExchange exchange, List<ProviderMessageCustomizer> matchedCustomizers) {
        execute(exchange, matchedCustomizers, false);
    }

    private void execute(ProviderExchange exchange, List<ProviderMessageCustomizer> matchedCustomizers, boolean beforeSend) {
        if (matchedCustomizers == null || matchedCustomizers.isEmpty()) {
            return;
        }
        for (ProviderMessageCustomizer customizer : matchedCustomizers) {
            if (log.isDebugEnabled()) {
                log.debug("Provider customizer {} phase={} provider={} service={} operation={} channel={} transport={}",
                        customizer.getClass().getSimpleName(),
                        beforeSend ? "beforeSend" : "afterReceive",
                        exchange.context().providerCode(),
                        exchange.context().serviceCode(),
                        exchange.context().operationCode(),
                        exchange.context().channelCode(),
                        exchange.context().transportType());
            }
            if (beforeSend) {
                customizer.beforeSend(exchange);
            } else {
                customizer.afterReceive(exchange);
            }
        }
    }
}
