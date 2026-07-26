package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FirstRoutePlanFactory {
    private final ServiceOperationSelector selector;
    private final ServiceOperationEndpointResolver endpointResolver;
    private final RoutingOperationMetadataResolver operationMetadataResolver;

    public RoutingPlan create(Service service) {
        ServiceOperation operation = selector.requireExactlyOneActive(service, RoutingStrategy.FIRST);
        String spanKind;
        try {
            spanKind = operationMetadataResolver.spanKind(operation.getOperationName());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Cannot build FIRST route for serviceCode="
                    + service.getCode() + ", operationName=" + operation.getOperationName()
                    + ": operation metadata is unavailable", exception);
        }
        return new RoutingPlan(service.getCode(), RoutingStrategy.FIRST, List.of(new RoutingStepPlan(
                operation.getOperationName(),
                0,
                operation,
                endpointResolver.resolve(operation.getOperationName()),
                (exchange, context) -> context.originalRequest(), null,
                new RoutingStepObservationContext(
                        service.getCode(),
                        null,
                        null,
                        operation.getOperationName(),
                        0,
                        spanKind)
        )));
    }
}
