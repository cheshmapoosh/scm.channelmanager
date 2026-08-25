package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionDispatchRoutingEngine implements RoutingEngine {
    private final SingleStepRoutingExecutor singleStepExecutor;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.ACTION_DISPATCH;
    }

    /**
     * ACTION_DISPATCH is stateless and does not create or manage a routing cursor.
     */
    @Override
    public RoutingExecutionResult execute(Exchange exchange, RoutingPlan plan) {
        return singleStepExecutor.execute(
                strategy(),
                exchange,
                plan,
                new RoutingExecutionContext(exchange.getMessage().getBody()),
                RoutingExecutionLifecycle.NOOP
        );
    }

    @Override
    public RoutingExecutionResult execute(
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingCursor cursor,
            RoutingExecutionLifecycle lifecycle
    ) {
        return singleStepExecutor.execute(
                strategy(), exchange, plan, context, cursor, lifecycle);
    }
}
