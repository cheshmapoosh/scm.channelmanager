package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FirstRoutingScmResultTest {

    @Test
    void unsuccessfulMessageRemainsTheFirstRoutingResponse() {
        Message response = Message.builder()
                .status(MessageStatus.SC_ERROR_BUSINESS)
                .build();

        assertPreservedFailureResponse(response);
    }

    @Test
    void scmFaultRemainsTheFirstRoutingResponse() {
        ScmFault response = ScmFault.builder()
                .status(MessageStatus.SC_ERROR_VALIDATION)
                .build();

        assertPreservedFailureResponse(response);
    }

    private void assertPreservedFailureResponse(Object response) {
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setBody("request");
        RoutingStepPlan onlyStep = step();
        org.mockito.Mockito.when(producerTemplate.send(eq(onlyStep.endpointUri()), eq(exchange)))
                .thenAnswer(invocation -> {
                    exchange.getMessage().setBody(response);
                    return exchange;
                });
        RoutingStepExecutor executor = new RoutingStepExecutor(
                new ServiceOperationRouteMetadataSetter(),
                producerTemplate,
                new RoutingResultClassifier(),
                mock(CoreObservationTraceSupport.class)
        );

        RoutingExecutionResult result = new FirstRoutingEngine(executor).execute(
                exchange,
                new RoutingPlan("task_complete", RoutingStrategy.FIRST, List.of(onlyStep))
        );

        assertSame(response, result.response());
        assertEquals(ChainStepDecision.FAIL, result.decision());
        assertSame(onlyStep, result.stoppedAt());
        assertTrue(result.context().stepResults().isEmpty());
        verify(producerTemplate, times(1)).send(onlyStep.endpointUri(), exchange);
    }

    private RoutingStepPlan step() {
        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName("complete-paymaster-approval-task");
        return new RoutingStepPlan(
                operation.getOperationName(),
                operation,
                "direct:op." + operation.getOperationName(),
                (exchange, context) -> context.originalRequest(),
                null,
                new RoutingStepObservationContext(
                        "my-paymaster",
                        "task_complete",
                        "COMPLETE_TASK",
                        0
                )
        );
    }
}
