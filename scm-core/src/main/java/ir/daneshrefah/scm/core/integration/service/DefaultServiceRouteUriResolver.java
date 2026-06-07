package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.springframework.stereotype.Component;

@Component
public class DefaultServiceRouteUriResolver implements ServiceRouteUriResolver {

    @Override
    public String resolve(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan) {
        return "direct:" + routeId(routePlan, servicePlan);
    }

    @Override
    public String routeId(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan) {
        validate(routePlan, servicePlan);
        return RouteIdSupport.serviceRouteId(
                routePlan.targetKind(),
                routePlan.gatewayChannel().getName(),
                servicePlan.service().getCode());
    }

    private void validate(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan) {
        if (routePlan == null) {
            throw new IllegalArgumentException("Runtime route plan is required for service route URI resolution.");
        }
        if (servicePlan == null || servicePlan.service() == null) {
            throw new IllegalArgumentException("Runtime service plan is required for service route URI resolution.");
        }
        if (routePlan.targetKind() == null) {
            throw new IllegalArgumentException("Runtime target kind is required for service route URI resolution.");
        }
        if (routePlan.gatewayChannel() == null) {
            throw new IllegalArgumentException("Runtime gateway channel is required for service route URI resolution.");
        }
    }
}
