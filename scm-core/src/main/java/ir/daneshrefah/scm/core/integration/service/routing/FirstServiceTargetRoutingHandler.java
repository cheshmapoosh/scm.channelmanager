package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirstServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final FirstRoutePlanFactory routePlanFactory;
    private final RoutingEngineRegistry engineRegistry;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.FIRST;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        RoutingPlan plan = routePlanFactory.create(context.service());
        log.debug("Building FIRST service target routeId={} serviceCode={}",
                context.route().getRouteId(), context.service().getCode());
        context.route().process(exchange -> exchange.getMessage().setBody(
                engineRegistry.getRequired(strategy()).execute(exchange, plan).response()));
    }
}
