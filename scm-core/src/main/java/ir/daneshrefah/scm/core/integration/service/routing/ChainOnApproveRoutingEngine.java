package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChainOnApproveRoutingEngine implements RoutingEngine {
    private final RoutingStepExecutor stepExecutor;

    @Override
    public RoutingStrategy strategy() { return RoutingStrategy.CHAIN_ON_APPROVE; }

    @Override
    public RoutingExecutionResult execute(
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingCursor cursor,
            RoutingExecutionLifecycle lifecycle
    ) {
        if (plan.routingStrategy() != strategy()) {
            throw new IllegalStateException("CHAIN_ON_APPROVE engine cannot execute plan=" + plan.planId());
        }
        requireCursor(plan, cursor);
        Object response = null;
        Object lastBusinessResponse = priorBusinessResponse(
                plan,
                context,
                cursor.stepIndex()
        );
        for (int index = cursor.stepIndex(); index < plan.steps().size(); index++) {
            RoutingStepPlan step = plan.steps().get(index);
            lifecycle.beforeStep(exchange, plan, step, context);
            RoutingStepExecutionResult result;
            try {
                result = stepExecutor.execute(exchange, strategy(), step, context);
            } catch (RuntimeException failure) {
                lifecycle.stepThrew(exchange, plan, step, context, failure);
                throw routingFailure(plan, step, failure);
            }
            lifecycle.afterStep(exchange, plan, step, context, result);
            response = result.response();
            RoutingDecision decision = result.decision();
            if (decision == RoutingDecision.SUCCESS) {
                if (step.observationContext().taskWorkflowStepType()
                        == TaskWorkflowStepType.BUSINESS_OPERATION) {
                    lastBusinessResponse = result.response();
                }
                continue;
            }
            if (decision == RoutingDecision.RETRY_LATER) {
                RoutingExecutionResult executionResult =
                        new RoutingExecutionResult(
                        response, context, RoutingDecision.RETRY_LATER, step);
                lifecycle.afterExecution(exchange, plan, executionResult);
                return executionResult;
            }
            throw new RoutingFailureException(
                    plan, step, result.decisionResult(), result.failure());
        }
        RoutingExecutionResult executionResult = new RoutingExecutionResult(
                lastBusinessResponse == null
                        ? response
                        : lastBusinessResponse,
                context,
                RoutingDecision.SUCCESS,
                null
        );
        lifecycle.afterExecution(exchange, plan, executionResult);
        return executionResult;
    }

    private Object priorBusinessResponse(
            RoutingPlan plan,
            RoutingExecutionContext context,
            int beforeStepIndex
    ) {
        Object selected = null;
        for (int index = 0; index < beforeStepIndex; index++) {
            RoutingStepPlan step = plan.steps().get(index);
            if (step.observationContext().taskWorkflowStepType()
                    == TaskWorkflowStepType.BUSINESS_OPERATION) {
                Object candidate = context.result(step.stepId());
                if (candidate != null) {
                    selected = candidate;
                }
            }
        }
        return selected;
    }

    private void requireCursor(RoutingPlan plan, RoutingCursor cursor) {
        if (cursor.direction() != RoutingCursor.Direction.FORWARD
                || cursor.stepIndex() >= plan.steps().size()) {
            throw new IllegalStateException(
                    "Invalid CHAIN_ON_APPROVE routing cursor for plan="
                            + plan.planId());
        }
        RoutingStepPlan selected = plan.steps().get(cursor.stepIndex());
        if (!selected.stepId().equals(cursor.stepId())) {
            throw new IllegalStateException(
                    "Routing cursor step identity does not match plan="
                            + plan.planId());
        }
    }

    private RoutingFailureException routingFailure(
            RoutingPlan plan,
            RoutingStepPlan step,
            RuntimeException failure
    ) {
        if (failure instanceof RoutingFailureException routingFailure) {
            return routingFailure;
        }
        return new RoutingFailureException(
                plan,
                step,
                new RoutingDecisionResult(
                        RoutingDecision.FAIL,
                        MessageStatus.SC_ERROR_SYSTEM,
                        "ROUTING_STEP_EXECUTION_ERROR",
                        "Routing step execution failed",
                        null
                ),
                failure
        );
    }
}
