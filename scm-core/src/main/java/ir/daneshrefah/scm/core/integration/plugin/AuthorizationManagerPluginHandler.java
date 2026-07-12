package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.authority.decision.configuration.handler.AuthorizationDecisionChainManager;
import ir.daneshrefah.scm.core.integration.observability.CamelSecurityTraceEventRecorder;
import ir.daneshrefah.scm.uaa.starter.security.event.ScmSecurityEventType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthorizationManagerPluginHandler implements PluginHandler {

    private final AuthorizationDecisionChainManager decisionManager;
    private final ObjectProvider<CamelSecurityTraceEventRecorder> securityTraceEventRecorders;

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        //THIS PLUGIN MUST REGISTERED ON USAGE 'CHANNEL'
    }

    /**
     * @apiNote This method is used to decide the request is authentication and authorization or not.
     */
    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        if (pluginDetail.getPhase().equals(PluginPhase.BEFORE)) {
            try {
                decisionManager.decide(exchange);
            } catch (Exception exception) {
                recordAccessDenied(exchange, exception);
                throw exception;
            }
        }
    }

    private void recordAccessDenied(Exchange exchange, Exception exception) {
        Map<String, Object> attributes = Map.of(
                "security.failure.reason", ScmSecurityEventType.ACCESS_DENIED.code(),
                "error.type", exception.getClass().getSimpleName(),
                "error.code", exception.getClass().getSimpleName()
        );
        securityTraceEventRecorders.orderedStream().forEach(recorder -> {
            try {
                recorder.record(exchange, "gateway", ScmSecurityEventType.ACCESS_DENIED, attributes);
            } catch (RuntimeException ignored) {
                // Security trace enrichment must never alter authorization behavior.
            }
        });
    }
}
