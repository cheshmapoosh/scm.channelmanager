package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoutingOperationMetadataResolverTest {

    @Test
    void providerAndRestOperationsUseClientSpans() {
        assertEquals("client", resolve(OperationType.PROVIDER));
        assertEquals("client", resolve(OperationType.REST));
    }

    @Test
    void internalOperationTypesUseInternalSpans() {
        assertEquals("internal", resolve(OperationType.JAVA));
    }

    @Test
    void missingOperationMetadataFailsPlanConstruction() {
        OperationService operationService = mock(OperationService.class);
        when(operationService.findActiveOperationsByNames(List.of("missing")))
                .thenReturn(List.of());

        assertThrows(
                IllegalStateException.class,
                () -> new RoutingOperationMetadataResolver(operationService)
                        .spanKind("missing")
        );
    }

    private String resolve(OperationType type) {
        Operation operation = new Operation();
        operation.setName("operation");
        operation.setType(type);
        OperationService operationService = mock(OperationService.class);
        when(operationService.findActiveOperationsByNames(List.of("operation")))
                .thenReturn(List.of(operation));
        return new RoutingOperationMetadataResolver(operationService)
                .spanKind("operation");
    }
}
