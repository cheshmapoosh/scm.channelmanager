package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;

public interface ServiceRouteUriResolver {
    String resolve(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan);

    String routeId(RuntimeRoutePlan routePlan, RuntimeServicePlan servicePlan);
}
