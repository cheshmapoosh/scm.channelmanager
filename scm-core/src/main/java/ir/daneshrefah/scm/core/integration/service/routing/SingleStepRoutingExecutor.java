package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.error.management.CamelErrorWrapperException;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/**
 * Shared execution lifecycle for routing strategies whose validated plan has one step.
 */
@Component
@RequiredArgsConstructor
public class SingleStepRoutingExecutor {
    private final RoutingStepExecutor stepExecutor;

    public RoutingExecutionResult execute(
            RoutingStrategy expectedStrategy,
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingExecutionLifecycle lifecycle
    ) {
        requirePlan(expectedStrategy, plan);
        return executeStep(expectedStrategy, exchange, plan, context, lifecycle);
    }

    public RoutingExecutionResult execute(
            RoutingStrategy expectedStrategy,
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingCursor cursor,
            RoutingExecutionLifecycle lifecycle
    ) {
        requirePlan(expectedStrategy, plan);
        RoutingStepPlan step = plan.steps().getFirst();
        requireCursor(expectedStrategy, cursor, step, plan);
        return executeStep(expectedStrategy, exchange, plan, context, lifecycle);
    }

    private RoutingExecutionResult executeStep(
            RoutingStrategy expectedStrategy,
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingExecutionLifecycle lifecycle
    ) {
        RoutingStepPlan step = plan.steps().getFirst();
        lifecycle.beforeStep(exchange, plan, step, context);
        RoutingStepExecutionResult result;
        try {
            result = stepExecutor.execute(exchange, expectedStrategy, step, context);
        } catch (RuntimeException failure) {
            lifecycle.stepThrew(exchange, plan, step, context, failure);
            throw routingFailure(plan, step, failure);
        }
        lifecycle.afterStep(exchange, plan, step, context, result);
        if (result.decision() == RoutingDecision.FAIL) {
            throw definitiveFailure(expectedStrategy, plan, step, result);
        }
        RoutingExecutionResult executionResult = new RoutingExecutionResult(
                result.response(),
                context,
                result.decision(),
                result.decision() == RoutingDecision.SUCCESS ? null : step
        );
        lifecycle.afterExecution(exchange, plan, executionResult);
        return executionResult;
    }

    private RuntimeException definitiveFailure(
            RoutingStrategy expectedStrategy,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingStepExecutionResult result
    ) {
        if (expectedStrategy == RoutingStrategy.ACTION_DISPATCH
                && result.response() instanceof ScmFault scmFault
                && scmFault.getErrors() != null
                && !scmFault.getErrors().isEmpty()) {
            return new CamelErrorWrapperException(scmFault.getErrors());
        }
        return new RoutingFailureException(
                plan, step, result.decisionResult(), result.failure());
    }

    private void requirePlan(RoutingStrategy expectedStrategy, RoutingPlan plan) {
        if (plan.routingStrategy() != expectedStrategy || plan.steps().size() != 1) {
            throw new IllegalStateException(expectedStrategy
                    + " engine requires a one-step " + expectedStrategy
                    + " plan=" + plan.planId());
        }
    }

    private void requireCursor(
            RoutingStrategy expectedStrategy,
            RoutingCursor cursor,
            RoutingStepPlan step,
            RoutingPlan plan
    ) {
        if (cursor.stepIndex() != 0
                || !step.stepId().equals(cursor.stepId())
                || cursor.direction() != RoutingCursor.Direction.FORWARD) {
            throw new IllegalStateException("Invalid " + expectedStrategy
                    + " routing cursor for plan=" + plan.planId());
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
