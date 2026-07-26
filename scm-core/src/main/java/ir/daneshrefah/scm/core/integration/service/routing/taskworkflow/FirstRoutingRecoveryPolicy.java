package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingCursor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import org.springframework.stereotype.Component;

@Component
public class FirstRoutingRecoveryPolicy implements RoutingRecoveryPolicy {
    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.FIRST;
    }

    @Override
    public RoutingCursor resolveRetryCursor(
            RoutingPlan plan,
            RoutingExecutionSnapshot snapshot
    ) {
        requireRetryLater(snapshot);
        if (plan.steps().size() != 1 || snapshot.steps().size() != 1) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "FIRST retry snapshot must contain exactly one step");
        }
        var planStep = plan.steps().getFirst();
        var savedStep = snapshot.steps().getFirst();
        if (savedStep.stepIndex() != 0
                || !planStep.stepId().equals(savedStep.stepId())
                || savedStep.decision() != RoutingDecision.RETRY_LATER) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "FIRST retry snapshot step identity or decision is inconsistent");
        }
        return RoutingCursor.start(plan);
    }

    private void requireRetryLater(RoutingExecutionSnapshot snapshot) {
        if (snapshot.decision() != RoutingDecision.RETRY_LATER
                || snapshot.executionState() != RoutingExecutionState.RETRY_PENDING) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Recovery is allowed only for a RETRY_LATER snapshot");
        }
    }
}
