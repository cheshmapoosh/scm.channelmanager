package ir.daneshrefah.scm.common.provider.message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProviderMessageCustomizerExecutor {

    public void beforeSend(ProviderExchange exchange, ProviderMessageCustomizerPipeline pipeline) {
        execute(exchange, pipeline, true);
    }

    public void afterReceive(ProviderExchange exchange, ProviderMessageCustomizerPipeline pipeline) {
        execute(exchange, pipeline, false);
    }

    private void execute(ProviderExchange exchange, ProviderMessageCustomizerPipeline pipeline, boolean beforeSend) {
        if (pipeline == null || pipeline.isEmpty()) {
            return;
        }
        for (ProviderMessageCustomizerPipeline.Entry entry : pipeline.entries()) {
            if (log.isDebugEnabled()) {
                log.debug("Provider customizer {} phase={} provider={} type={} service={} operation={} channel={} transport={}",
                        entry.type(),
                        beforeSend ? "beforeSend" : "afterReceive",
                        exchange.context().providerCode(),
                        exchange.context().providerType(),
                        exchange.context().serviceCode(),
                        exchange.context().operationCode(),
                        exchange.context().channelCode(),
                        exchange.context().transportType());
            }
            if (beforeSend) {
                entry.customizer().beforeSend(exchange);
            } else {
                entry.customizer().afterReceive(exchange);
            }
        }
    }
}
