package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirstServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final ServiceOperationRouteMetadataSetter metadataSetter;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.FIRST;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        ServiceOperation operation = operationSelector.requireExactlyOneActive(
                context.service(),
                strategy()
        );
        log.debug("Building FIRST service target routeId={} serviceCode={}",
                context.route().getRouteId(), context.service().getCode());
        metadataSetter.apply(context.route(), operation);
        context.route().to(endpointResolver.resolve(operation.getOperationName()));
    }
}
