package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.Service;

import java.util.List;
import java.util.Objects;

public record ChainOnApproveRoutePlan(
        Service service,
        List<ChainOnApproveStepPlan> steps
) {
    public ChainOnApproveRoutePlan {
        Objects.requireNonNull(service, "service must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
    }
}
