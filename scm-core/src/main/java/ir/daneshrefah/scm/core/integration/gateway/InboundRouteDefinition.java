package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;

public record InboundRouteDefinition(
        RouteDefinition route,
        ProcessorDefinition<?> pipeline,
        ChannelServiceDefinition channelServiceDefinition,
        String serviceVersion) {

    public InboundRouteDefinition(RouteDefinition route,
                                  ProcessorDefinition<?> pipeline,
                                  ChannelServiceDefinition channelServiceDefinition) {
        this(route, pipeline, channelServiceDefinition, "v1");
    }

    public InboundRouteDefinition(RouteDefinition route,
                                  ChannelServiceDefinition channelServiceDefinition) {
        this(route, route, channelServiceDefinition, "v1");
    }

    public InboundRouteDefinition(RouteDefinition route,
                                  ChannelServiceDefinition channelServiceDefinition,
                                  String serviceVersion) {
        this(route, route, channelServiceDefinition, serviceVersion);
    }
}
