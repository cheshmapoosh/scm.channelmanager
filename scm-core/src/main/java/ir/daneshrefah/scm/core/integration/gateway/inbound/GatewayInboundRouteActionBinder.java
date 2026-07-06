package ir.daneshrefah.scm.core.integration.gateway.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
public class GatewayInboundRouteActionBinder {

    public void bind(RouteDefinition route, InboundRouteActionConfig config) {
        if (route != null && config != null && config.inboundAction() != null) {
            route.setProperty(Message.INBOUND_ROUTE_ACTION, constant(config.inboundAction()));
        }
    }
}
