package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.MulticastDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailOverServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final ServiceOperationSelector operationSelector;
    private final ServiceOperationEndpointResolver endpointResolver;

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.FAIL_OVER;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        List<ServiceOperation> operations = operationSelector.requireAtLeastTwoActive(context.service(), strategy());
        log.debug("Building FAIL_OVER service target routeId={} serviceCode={} operationCount={}",
                context.route().getRouteId(), context.service().getCode(), operations.size());
        MulticastDefinition multicast = context.route().multicast()
                .parallelProcessing(false)
                .stopOnException("false");
        operations.forEach(operation ->
                multicast.to(endpointResolver.resolve(operation.getOperationName())).end());
    }
}
