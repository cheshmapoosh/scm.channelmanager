package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;

import java.util.Objects;

public record ChannelRouteBuildContext(
        RuntimeRoutePlan routePlan,
        RuntimeServicePlan servicePlan
) {

    public ChannelRouteBuildContext {
        Objects.requireNonNull(routePlan, "routePlan must not be null");
        Objects.requireNonNull(servicePlan, "servicePlan must not be null");
    }
}
