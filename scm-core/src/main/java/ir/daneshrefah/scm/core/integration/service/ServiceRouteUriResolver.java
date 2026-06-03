package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;

public interface ServiceRouteUriResolver {
    String resolve(Service service);

    String resolve(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan);

    String routeId(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan);

    String normalizeServiceCode(String serviceCode);
}
