package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChainOnApproveServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final ChainOnApproveRoutePlanFactory routePlanFactory;
    private final RoutingEngineRegistry engineRegistry;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.CHAIN_ON_APPROVE;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        RoutingPlan plan = routePlanFactory.create(context.service());
        log.debug("Building CHAIN_ON_APPROVE service target routeId={} serviceCode={} operationCount={}",
                context.route().getRouteId(), context.service().getCode(), plan.steps().size());
        context.route().process(exchange -> exchange.getMessage().setBody(
                engineRegistry.getRequired(strategy()).execute(exchange, plan).response()));
    }
}
