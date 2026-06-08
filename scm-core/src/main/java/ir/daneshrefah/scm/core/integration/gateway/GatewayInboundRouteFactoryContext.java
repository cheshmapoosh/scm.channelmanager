package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import org.apache.camel.builder.RouteBuilder;

import java.util.Objects;

public record GatewayInboundRouteFactoryContext(
        GatewayChannel gatewayChannel,
        RuntimeRoutePlan routePlan,
        RouteBuilder routeBuilder
) {

    public GatewayInboundRouteFactoryContext {
        Objects.requireNonNull(gatewayChannel, "gatewayChannel must not be null");
        Objects.requireNonNull(routePlan, "routePlan must not be null");
        Objects.requireNonNull(routeBuilder, "routeBuilder must not be null");
    }
}
