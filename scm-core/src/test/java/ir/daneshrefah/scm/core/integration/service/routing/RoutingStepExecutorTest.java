package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class RoutingStepExecutorTest {

    @Test
    void invokesOneOperationRouteWithMetadataAndContextBuiltRequest() {
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        CoreObservationTraceSupport observation = mock(CoreObservationTraceSupport.class);
        RoutingStepExecutor executor = new RoutingStepExecutor(
                new ServiceOperationRouteMetadataSetter(),
                producerTemplate,
                new RoutingResultClassifier(),
                observation);
        CamelContext camelContext = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setBody("original");
        RoutingExecutionContext context = new RoutingExecutionContext("original");
        context.record("approved", "approval-result");
        ServiceOperation operation = operation("register-paymaster-in-nab");
        RoutingStepPlan step = new RoutingStepPlan(
                operation.getOperationName(),
                operation,
                "direct:op.register-paymaster-in-nab",
                (currentExchange, currentContext) ->
                        "request:" + currentContext.stepResults().get("approved"),
                FirstRoutingEngineTest.policy(ChainStepDecision.CONTINUE),
                new RoutingStepObservationContext(
                        "my-paymaster", "approve_and_execute", "BUSINESS_OPERATION", 1));
        when(producerTemplate.send(eq(step.endpointUri()), same(exchange)))
                .thenAnswer(invocation -> {
                    assertEquals("request:approval-result", exchange.getMessage().getBody());
                    exchange.getMessage().setBody("nab-response");
                    return exchange;
                });

        RoutingStepExecutionResult result = executor.execute(
                exchange, RoutingStrategy.CHAIN_ON_APPROVE, step, context);

        assertEquals("nab-response", result.response());
        assertNull(result.failure());
        assertSame(operation, exchange.getProperty(Message.SERVICE_OPERATION));
        assertEquals(operation.getOperationName(), exchange.getProperty(Message.OPERATION_NAME));
        assertEquals("BUSINESS_OPERATION", exchange.getProperty(Message.TASK_WORKFLOW_ROLE));
        verify(producerTemplate, times(1)).send(step.endpointUri(), exchange);
        verify(observation, times(1)).startRoutingStepCall(
                exchange,
                "my-paymaster",
                "register-paymaster-in-nab",
                RoutingStrategy.CHAIN_ON_APPROVE,
                1,
                "approve_and_execute",
                "BUSINESS_OPERATION",
                "internal");
        verify(observation, times(1)).finishRoutingStepCall(
                eq(exchange), eq("CONTINUE"), isNull(), isNull(), anyLong());
    }

    @Test
    void returnsTheOriginalOperationFailureForEngineClassification() {
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        CoreObservationTraceSupport observation = mock(CoreObservationTraceSupport.class);
        RoutingStepExecutor executor = new RoutingStepExecutor(
                new ServiceOperationRouteMetadataSetter(),
                producerTemplate,
                new RoutingResultClassifier(),
                observation);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        IllegalArgumentException failure = new IllegalArgumentException("provider rejected");
        RoutingStepPlan step = FirstRoutingEngineTest.step(
                "operation", FirstRoutingEngineTest.policy(ChainStepDecision.FAIL));
        when(producerTemplate.send(eq(step.endpointUri()), same(exchange)))
                .thenAnswer(invocation -> {
                    exchange.setException(failure);
                    return exchange;
                });

        RoutingStepExecutionResult result = executor.execute(
                exchange,
                RoutingStrategy.CHAIN_ON_APPROVE,
                step,
                new RoutingExecutionContext("request"));

        assertSame(failure, result.failure());
        verify(producerTemplate, times(1)).send(step.endpointUri(), exchange);
        verify(observation, times(1)).finishRoutingStepCall(
                eq(exchange), eq("FAIL"), isNull(), same(failure), anyLong());
    }

    private ServiceOperation operation(String name) {
        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName(name);
        return operation;
    }
}
