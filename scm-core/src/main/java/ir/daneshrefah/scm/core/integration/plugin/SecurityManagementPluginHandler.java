package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.authority.decision.manager.SecurityDecisionManager;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SecurityManagementPluginHandler implements PluginHandler {

    private final SecurityDecisionManager decisionManager;

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
        if (pluginDetail.getPhase().equals(PluginPhase.BEFORE)){
            decisionManager.decide(exchange);
        }
    }
}
