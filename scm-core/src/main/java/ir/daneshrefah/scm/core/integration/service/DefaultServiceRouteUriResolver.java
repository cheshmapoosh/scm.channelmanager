package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DefaultServiceRouteUriResolver implements ServiceRouteUriResolver {
    private static final String SERVICE_ROUTE_PREFIX = "direct:scm.service.";

    @Override
    public String resolve(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("Service is required for service route URI resolution.");
        }
        return SERVICE_ROUTE_PREFIX + normalizeServiceCode(service.getCode());
    }

    @Override
    public String resolve(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan) {
        return SERVICE_ROUTE_PREFIX + routeKey(routePlan, servicePlan);
    }

    @Override
    public String routeId(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan) {
        validate(routePlan, servicePlan);
        return RouteIdSupport.serviceRouteId(
                routePlan.targetKind(),
                routePlan.gatewayChannel().getName(),
                servicePlan.service().getCode());
    }

    @Override
    public String normalizeServiceCode(String serviceCode) {
        String normalized = StringUtils.trimToNull(serviceCode);
        if (normalized == null) {
            throw new IllegalArgumentException("Service code is required for service route URI resolution.");
        }
        normalized = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (StringUtils.isBlank(normalized)) {
            throw new IllegalArgumentException("Service code '" + serviceCode + "' cannot be normalized.");
        }
        return normalized;
    }

    private String routeKey(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan) {
        validate(routePlan, servicePlan);
        return RouteIdSupport.serviceRouteKey(
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
