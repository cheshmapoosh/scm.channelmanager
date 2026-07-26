package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingCursor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionDecision;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionSnapshot;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionState;

public class FirstRoutingRecoveryPolicy implements RoutingRecoveryPolicy {
    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.FIRST;
    }

    @Override
    public RoutingCursor resolveRetryCursor(
            RoutingPlan plan,
            TaskWorkflowExecutionSnapshot snapshot
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
                || savedStep.decision()
                != TaskWorkflowExecutionDecision.RETRY_LATER) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "FIRST retry snapshot step identity or decision is inconsistent");
        }
        return RoutingCursor.start(plan);
    }

    private void requireRetryLater(
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (snapshot.decision()
                != TaskWorkflowExecutionDecision.RETRY_LATER
                || snapshot.executionState()
                != TaskWorkflowExecutionState.RETRY_PENDING) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Recovery is allowed only for a RETRY_LATER snapshot");
        }
    }
}
