package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingCursor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionDecision;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionSnapshot;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionState;

public class ChainOnApproveRoutingRecoveryPolicy implements RoutingRecoveryPolicy {
    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.CHAIN_ON_APPROVE;
    }

    @Override
    public RoutingCursor resolveRetryCursor(
            RoutingPlan plan,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (snapshot.status().decision()
                != TaskWorkflowExecutionDecision.RETRY_LATER
                || snapshot.status().state()
                != TaskWorkflowExecutionState.RETRY_PENDING) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Recovery is allowed only for a RETRY_LATER snapshot");
        }
        if (snapshot.steps().size() != plan.steps().size()) {
            throw corrupted("step count does not match the current plan");
        }

        for (int index = 0; index < plan.steps().size(); index++) {
            var planStep = plan.steps().get(index);
            var savedStep = snapshot.steps().get(index);
            if (savedStep.stepIndex() != index
                    || !planStep.stepId().equals(savedStep.stepId())) {
                throw corrupted("step identity mismatch at stepIndex=" + index);
            }
        }

        for (int index = 0; index < plan.steps().size(); index++) {
            var planStep = plan.steps().get(index);
            var savedStep = snapshot.steps().get(index);
            if (savedStep.decision()
                    == TaskWorkflowExecutionDecision.SUCCESS) {
                continue;
            }
            if (savedStep.decision()
                    != TaskWorkflowExecutionDecision.RETRY_LATER) {
                throw corrupted("first non-successful step is not RETRY_LATER at "
                        + "stepIndex=" + index);
            }
            for (int earlier = 0; earlier < index; earlier++) {
                if (snapshot.steps().get(earlier).decision()
                        != TaskWorkflowExecutionDecision.SUCCESS) {
                    throw corrupted("an earlier step is not SUCCESS at stepIndex="
                            + earlier);
                }
            }
            return new RoutingCursor(
                    index,
                    planStep.stepId(),
                    RoutingCursor.Direction.FORWARD
            );
        }
        throw corrupted("RETRY_LATER snapshot contains no retryable step");
    }

    private InvalidTaskWorkflowExecutionStateException corrupted(String reason) {
        return new InvalidTaskWorkflowExecutionStateException(
                "Corrupted CHAIN_ON_APPROVE retry snapshot: " + reason);
    }
}
