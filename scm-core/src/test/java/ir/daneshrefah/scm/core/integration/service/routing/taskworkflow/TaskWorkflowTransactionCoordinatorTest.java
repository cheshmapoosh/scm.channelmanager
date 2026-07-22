package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskWorkflowTransactionCoordinatorTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void eventPublisherResolutionFailureDoesNotChangeRoutingState() {
        ObjectProvider<ScmEventPublisher> publisherProvider = mock(ObjectProvider.class);
        when(publisherProvider.getIfAvailable())
                .thenThrow(new IllegalStateException("publisher unavailable"));
        TaskWorkflowExecutionStore executionStore = mock(TaskWorkflowExecutionStore.class);
        ObjectProvider<TaskWorkflowExecutionStore> storeProvider = mock(ObjectProvider.class);
        when(storeProvider.getIfAvailable(any(Supplier.class)))
                .thenReturn(executionStore);
        TaskWorkflowTransactionCoordinator coordinator =
                new TaskWorkflowTransactionCoordinator(
                        publisherProvider,
                        storeProvider
                );
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setProperty(
                TaskWorkflowExchangeProperties.COMMAND,
                TaskWorkflowCommand.APPROVE_AND_EXECUTE
        );

        assertDoesNotThrow(() -> coordinator.beforeBusinessOperation(exchange));

        verify(executionStore).record(
                TaskWorkflowCommand.APPROVE_AND_EXECUTE,
                TaskWorkflowRole.BUSINESS_OPERATION,
                "started",
                null,
                null
        );
    }
}
