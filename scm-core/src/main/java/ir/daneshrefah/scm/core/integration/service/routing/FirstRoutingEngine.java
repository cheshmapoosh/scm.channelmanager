package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FirstRoutingEngine implements RoutingEngine {
    private final RoutingStepExecutor stepExecutor;

    @Override
    public RoutingStrategy strategy() { return RoutingStrategy.FIRST; }

    @Override
    public RoutingExecutionResult execute(
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingCursor cursor,
            RoutingExecutionLifecycle lifecycle
    ) {
        requireStrategy(plan);
        RoutingStepPlan step = plan.steps().getFirst();
        requireCursor(cursor, step, plan);
        lifecycle.beforeStep(exchange, plan, step, context);
        RoutingStepExecutionResult result;
        try {
            result = stepExecutor.execute(exchange, strategy(), step, context);
        } catch (RuntimeException failure) {
            lifecycle.stepThrew(exchange, plan, step, context, failure);
            throw routingFailure(plan, step, failure);
        }
        lifecycle.afterStep(exchange, plan, step, context, result);
        if (result.decision() == RoutingDecision.FAIL) {
            throw new RoutingFailureException(
                    plan, step, result.decisionResult(), result.failure());
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

    private void requireStrategy(RoutingPlan plan) {
        if (plan.routingStrategy() != strategy() || plan.steps().size() != 1) {
            throw new IllegalStateException("FIRST engine requires one-step FIRST plan=" + plan.planId());
        }
    }

    private void requireCursor(
            RoutingCursor cursor,
            RoutingStepPlan step,
            RoutingPlan plan
    ) {
        if (cursor.stepIndex() != 0
                || !step.stepId().equals(cursor.stepId())
                || cursor.direction() != RoutingCursor.Direction.FORWARD) {
            throw new IllegalStateException(
                    "Invalid FIRST routing cursor for plan=" + plan.planId());
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
