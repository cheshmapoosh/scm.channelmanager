package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionDispatchServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final ActionDispatchPlanCatalog planCatalog;
    private final RoutingEngineRegistry engineRegistry;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.ACTION_DISPATCH;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        ActionDispatchPlan dispatchPlan = planCatalog.planFor(context.service());
        log.debug("Building ACTION_DISPATCH service target routeId={} serviceCode={} actionCount={}",
                context.route().getRouteId(),
                context.service().getCode(),
                dispatchPlan.plansByInboundAction().size());
        context.route().process(exchange -> {
            RoutingPlan selected = dispatchPlan.requireAction(exchange);
            Object response = engineRegistry.getRequired(strategy())
                    .execute(exchange, selected)
                    .response();
            exchange.getMessage().setBody(response);
        });
    }
}
