package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record RoutingPlan(
        String planId,
        RoutingPlanIdentity identity,
        RoutingStrategy routingStrategy,
        List<RoutingStepPlan> steps
) {
    public RoutingPlan(
            String planId,
            RoutingStrategy routingStrategy,
            List<RoutingStepPlan> steps
    ) {
        this(planId, null, routingStrategy, steps);
    }

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
        for (int index = 0; index < steps.size(); index++) {
            RoutingStepPlan step = steps.get(index);
            if (step.stepIndex() != index) {
                throw new IllegalStateException("Routing plan=" + planId
                        + " stepId=" + step.stepId()
                        + " has stepIndex=" + step.stepIndex()
                        + "; expected " + index);
            }
        }
        long uniqueStepIds = steps.stream()
                .map(RoutingStepPlan::stepId)
                .map(stepId -> stepId.toLowerCase(Locale.ROOT))
                .distinct()
                .count();
        if (uniqueStepIds != steps.size()) {
            throw new IllegalStateException(
                    "Routing plan=" + planId + " contains duplicate stepId values");
        }
    }
}
