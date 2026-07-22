package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
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
    public RoutingExecutionResult execute(Exchange exchange, RoutingPlan plan) {
        if (plan.routingStrategy() != strategy()) {
            throw new IllegalStateException("CHAIN_ON_APPROVE engine cannot execute plan=" + plan.planId());
        }
        RoutingExecutionContext context = new RoutingExecutionContext(exchange.getMessage().getBody());
        Object response = null;
        for (RoutingStepPlan step : plan.steps()) {
            RoutingStepExecutionResult result = stepExecutor.execute(exchange, strategy(), step, context);
            response = result.response();
            ChainStepDecision decision = result.decision();
            if (result.failure() == null && decision == ChainStepDecision.CONTINUE) {
                continue;
            }
            if (decision == ChainStepDecision.RETRY_LATER) {
                throw new RoutingRetryLaterException(plan.planId(), step, response, context, result.failure());
            }
            if (result.failure() != null) {
                throw result.failure() instanceof RuntimeException runtime ? runtime
                        : new IllegalStateException("Routing step failed operationName="
                        + step.serviceOperation().getOperationName(), result.failure());
            }
            return new RoutingExecutionResult(response, context, ChainStepDecision.FAIL, step);
        }
        return new RoutingExecutionResult(response, context, ChainStepDecision.CONTINUE, null);
    }
}
