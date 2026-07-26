package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.commons.lang3.StringUtils;

public record RoutingCursor(
        int stepIndex,
        String stepId,
        Direction direction
) {
    public RoutingCursor {
        if (stepIndex < 0) {
            throw new IllegalArgumentException("stepIndex must not be negative");
        }
        stepId = StringUtils.trimToNull(stepId);
        if (stepId == null) {
            throw new IllegalArgumentException("stepId must not be blank");
        }
        direction = direction == null ? Direction.FORWARD : direction;
    }

    public static RoutingCursor start(RoutingPlan plan) {
        RoutingStepPlan first = plan.steps().getFirst();
        return new RoutingCursor(first.stepIndex(), first.stepId(), Direction.FORWARD);
    }

    public enum Direction {
        FORWARD,
        COMPENSATION
    }
}
