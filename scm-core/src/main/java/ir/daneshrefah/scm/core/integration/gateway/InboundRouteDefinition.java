package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import org.apache.camel.model.RouteDefinition;

public record InboundRouteDefinition(
        RouteDefinition route,
        ChannelServiceDefinition channelServiceDefinition,
        String serviceVersion) {

    public InboundRouteDefinition(RouteDefinition route,
                                  ChannelServiceDefinition channelServiceDefinition) {
        this(route, channelServiceDefinition, "v1");
    }
}
