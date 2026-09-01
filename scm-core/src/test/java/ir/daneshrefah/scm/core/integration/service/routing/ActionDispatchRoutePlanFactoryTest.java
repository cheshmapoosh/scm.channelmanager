package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionDispatchRoutePlanFactoryTest {

    private final ServiceOperationEndpointResolver endpointResolver =
            mock(ServiceOperationEndpointResolver.class);
    private final RoutingOperationMetadataResolver metadataResolver =
            mock(RoutingOperationMetadataResolver.class);
    private final ActionDispatchRoutePlanFactory factory =
            new ActionDispatchRoutePlanFactory(
                    new ServiceOperationSelector(),
                    endpointResolver,
                    metadataResolver,
                    new ObjectMapper()
            );

    @Test
    void derivesDispatchBindingFromDefinitionInboundAction() {
        ServiceOperation balance = operation(
                "balance-inquiry",
                "{\"inboundAction\":\" Balance.Check \"}"
        );
        Service service = service(balance);
        when(endpointResolver.resolveRegisteredOperation("balance-inquiry"))
                .thenReturn("direct:op.balance-inquiry");
        when(metadataResolver.spanKind("balance-inquiry")).thenReturn("INTERNAL");

        ActionDispatchPlan plan = factory.create(service);

        RoutingPlan route = plan.plansByInboundAction().get("balance.check");
        assertEquals(1, plan.plansByInboundAction().size());
        assertSame(balance, route.steps().getFirst().serviceOperation());
    }

    @Test
    void rejectsOperationWithoutDefinition() {
        ServiceOperation operation = operation("balance-inquiry", null);
        operation.setDefinition(null);

        ServiceActionException exception = assertThrows(
                ServiceActionException.class,
                () -> factory.create(service(operation))
        );

        assertTrue(exception.getMessage().contains("must reference a Definition"));
    }

    @Test
    void rejectsDuplicateDefinitionInboundActions() {
        ServiceOperation first = operation(
                "balance-inquiry",
                "{\"inboundAction\":\"balance.check\"}"
        );
        ServiceOperation second = operation(
                "balance-summary",
                "{\"inboundAction\":\" BALANCE.CHECK \"}"
        );
        when(endpointResolver.resolveRegisteredOperation("balance-inquiry"))
                .thenReturn("direct:op.balance-inquiry");
        when(metadataResolver.spanKind("balance-inquiry")).thenReturn("INTERNAL");

        ServiceActionException exception = assertThrows(
                ServiceActionException.class,
                () -> factory.create(service(first, second))
        );

        assertTrue(exception.getMessage().contains(
                "duplicate Definition.details.inboundAction=balance.check"));
    }

    private Service service(ServiceOperation... operations) {
        Service service = new Service();
        service.setCode("card-service");
        service.setRoutingStrategy(RoutingStrategy.ACTION_DISPATCH);
        service.setServiceOperations(List.of(operations));
        return service;
    }

    private ServiceOperation operation(String operationName, String details) {
        Definition definition = new Definition();
        definition.setId(operationName + "-definition");
        definition.setDetails(details);

        ServiceOperation operation = new ServiceOperation();
        operation.setId(operationName + "-binding");
        operation.setActive(true);
        operation.setOperationName(operationName);
        operation.setDefinition(definition);
        return operation;
    }
}
