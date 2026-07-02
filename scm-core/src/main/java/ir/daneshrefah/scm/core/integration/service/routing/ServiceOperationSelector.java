package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceOperationSelector {

    public List<ServiceOperation> requireAtLeastTwoActive(Service service, RoutingStrategy strategy) {
        List<ServiceOperation> operations = active(service);
        if (operations.size() < 2) {
            throw new IllegalStateException("Routing strategy " + strategy
                    + " requires at least two active operations for service " + serviceCode(service)
                    + "; found " + operations.size());
        }
        return operations;
    }

    public ServiceOperation requireExactlyOneActive(Service service, RoutingStrategy strategy) {
        List<ServiceOperation> operations = active(service);
        if (operations.size() != 1) {
            throw new IllegalStateException("Routing strategy " + strategy
                    + " requires exactly one active operation for service " + serviceCode(service)
                    + "; found " + operations.size());
        }
        return operations.getFirst();
    }

    public List<ServiceOperation> active(Service service) {
        if (service == null || service.getServiceOperations() == null) {
            return List.of();
        }
        // TODO Add an explicit DB order field if operation ordering must become business-configurable.
        return service.getServiceOperations().stream()
                .filter(operation -> operation != null && Boolean.TRUE.equals(operation.getActive()))
                .toList();
    }

    private String serviceCode(Service service) {
        return service == null ? "<null>" : String.valueOf(service.getCode());
    }
}
