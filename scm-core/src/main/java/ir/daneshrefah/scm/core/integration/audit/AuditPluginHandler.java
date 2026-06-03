package ir.daneshrefah.scm.core.integration.audit;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component("auditPluginHandler")
@RequiredArgsConstructor
@Slf4j
public class AuditPluginHandler implements PluginHandler {
    private final AuditEventWriter auditEventWriter;
    private final ScmExchangeMdc scmExchangeMdc;

    @Override
    public PluginType getType() {
        return PluginType.LOGGER;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) {
        Map<String, String> fields = scmExchangeMdc.put(exchange);
        try {
            auditEventWriter.write(new AuditEvent(
                    Instant.now(),
                    fields.get("traceId"),
                    fields.get("spanId"),
                    fields.get("correlationId"),
                    fields.get("gatewayName"),
                    fields.get("channelCode"),
                    fields.get("serviceCode"),
                    fields.get("serviceVersion"),
                    fields.get("operationName"),
                    pluginDetail.getPhase() != null ? pluginDetail.getPhase().name() : null,
                    "SUCCESS",
                    null,
                    null,
                    fields.get("routeId"),
                    fields.get("exchangeId")));
        } catch (Exception e) {
            log.warn("Audit plugin could not write audit event routeId={} exchangeId={}",
                    exchange.getFromRouteId(), exchange.getExchangeId(), e);
        }
    }
}
