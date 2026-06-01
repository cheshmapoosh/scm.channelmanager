package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.camel.builder.RouteBuilder;

import java.util.List;

public interface ProtocolHandler {
    ProtocolType getProtocol();
    ProtocolConfigurer config(GatewayChannel gatewayChannel, RouteBuilder builder);

    interface ProtocolConfigurer {
        List<InboundRouteDefinition> routeDefinition(RuntimeServicePlan servicePlan);

        @Deprecated(forRemoval = false)
        default List<InboundRouteDefinition> routeDefinition(ChannelServiceAccess channelServiceAccess,
                                                            List<ChannelServiceDefinition> channelServiceDefinitions) {
            return routeDefinition(new RuntimeServicePlan(
                    null,
                    channelServiceAccess,
                    channelServiceAccess.getService(),
                    channelServiceDefinitions));
        }
    }
}
