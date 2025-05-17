package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

import java.util.List;

public interface ProtocolHandler {
    ProtocolType getProtocol();
    ProtocolConfigurer config(GatewayChannel gatewayChannel, RouteBuilder builder);

    interface ProtocolConfigurer {
        RouteDefinition routeDefinition(ChannelServiceAccess channelServiceAccess, List<ChannelServiceDefinition> channelServiceDefinitions);
    }
}
