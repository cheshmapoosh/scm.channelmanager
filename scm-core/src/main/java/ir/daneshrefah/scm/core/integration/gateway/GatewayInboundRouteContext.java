package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.camel.builder.RouteBuilder;

import java.util.Objects;

public record GatewayInboundRouteContext(
        GatewayChannel gatewayChannel,
        RuntimeRoutePlan routePlan,
        RuntimeServicePlan servicePlan,
        RouteBuilder routeBuilder
) {

    public GatewayInboundRouteContext {
        Objects.requireNonNull(gatewayChannel, "gatewayChannel must not be null");
        Objects.requireNonNull(routePlan, "routePlan must not be null");
        Objects.requireNonNull(servicePlan, "servicePlan must not be null");
        Objects.requireNonNull(routeBuilder, "routeBuilder must not be null");
    }
}
