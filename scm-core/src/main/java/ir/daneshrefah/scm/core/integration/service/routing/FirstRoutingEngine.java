package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
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
    public RoutingExecutionResult execute(Exchange exchange, RoutingPlan plan) {
        requireStrategy(plan);
        RoutingExecutionContext context = new RoutingExecutionContext(exchange.getMessage().getBody());
        RoutingStepPlan step = plan.steps().getFirst();
        RoutingStepExecutionResult result = stepExecutor.execute(exchange, strategy(), step, context);
        if (result.failure() != null) {
            throw propagate(result.failure(), step);
        }
        return new RoutingExecutionResult(
                result.response(),
                context,
                result.decision(),
                result.decision() == ChainStepDecision.CONTINUE ? null : step
        );
    }

    private void requireStrategy(RoutingPlan plan) {
        if (plan.routingStrategy() != strategy() || plan.steps().size() != 1) {
            throw new IllegalStateException("FIRST engine requires one-step FIRST plan=" + plan.planId());
        }
    }

    private RuntimeException propagate(Throwable failure, RoutingStepPlan step) {
        return failure instanceof RuntimeException runtime ? runtime
                : new IllegalStateException("Routing step failed operationName="
                + step.serviceOperation().getOperationName(), failure);
    }
}
