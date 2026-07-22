package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;

import java.util.List;
import java.util.Objects;

public record RoutingPlan(String planId, RoutingStrategy routingStrategy, List<RoutingStepPlan> steps) {
    public RoutingPlan {
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(routingStrategy, "routingStrategy must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        if (routingStrategy != RoutingStrategy.FIRST
                && routingStrategy != RoutingStrategy.CHAIN_ON_APPROVE) {
            throw new IllegalStateException("Routing plan=" + planId
                    + " requires an execution-engine strategy; found " + routingStrategy);
        }
        if (routingStrategy == RoutingStrategy.FIRST && steps.size() != 1) {
            throw new IllegalStateException("FIRST routing plan=" + planId
                    + " requires exactly one step; found " + steps.size());
        }
        if (routingStrategy == RoutingStrategy.CHAIN_ON_APPROVE && steps.isEmpty()) {
            throw new IllegalStateException("CHAIN_ON_APPROVE routing plan=" + planId
                    + " requires at least one step");
        }
    }
}
