package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FirstRoutingEngineTest {
    @Test
    void executesExactlyOneStep() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getMessage()).thenReturn(message);
        RoutingStepPlan step = step("one", null);
        when(executor.execute(org.mockito.ArgumentMatchers.eq(exchange),
                org.mockito.ArgumentMatchers.eq(RoutingStrategy.FIRST),
                org.mockito.ArgumentMatchers.eq(step), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    RoutingExecutionContext executionContext = invocation.getArgument(3);
                    executionContext.record("one", "response");
                    return new RoutingStepExecutionResult("response", null, 1);
                });

        RoutingExecutionResult result = new FirstRoutingEngine(executor).execute(
                exchange, new RoutingPlan("first", RoutingStrategy.FIRST, List.of(step)));

        assertEquals("response", result.response());
        assertEquals("response", result.context().stepResults().get("one"));
        assertEquals(ChainStepDecision.CONTINUE, result.decision());
        verify(executor, times(1)).execute(eq(exchange), eq(RoutingStrategy.FIRST), eq(step), any());
    }

    @Test
    void rejectsZeroOrMultipleSteps() {
        assertThrows(IllegalStateException.class,
                () -> new RoutingPlan("empty", RoutingStrategy.FIRST, List.of()));
        assertThrows(IllegalStateException.class,
                () -> new RoutingPlan("many", RoutingStrategy.FIRST,
                        List.of(step("one", null), step("two", null))));
    }

    @Test
    void preservesRuntimeOperationFailure() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = mock(Exchange.class);
        when(exchange.getMessage()).thenReturn(mock(Message.class));
        RoutingStepPlan step = step("one", null);
        IllegalArgumentException failure = new IllegalArgumentException("operation failed");
        when(executor.execute(eq(exchange), eq(RoutingStrategy.FIRST), eq(step), any()))
                .thenReturn(new RoutingStepExecutionResult(null, failure, 1));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> new FirstRoutingEngine(executor).execute(exchange,
                        new RoutingPlan("first", RoutingStrategy.FIRST, List.of(step))));

        assertSame(failure, thrown);
        verify(executor, times(1)).execute(eq(exchange), eq(RoutingStrategy.FIRST), eq(step), any());
    }

    @Test
    void rejectsPlanForAnotherStrategyBeforeInvokingStep() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = mock(Exchange.class);
        when(exchange.getMessage()).thenReturn(mock(Message.class));
        RoutingStepPlan step = step("one", policy(ChainStepDecision.CONTINUE));

        assertThrows(IllegalStateException.class,
                () -> new FirstRoutingEngine(executor).execute(exchange,
                        new RoutingPlan("chain", RoutingStrategy.CHAIN_ON_APPROVE, List.of(step))));

        verifyNoInteractions(executor);
    }

    static RoutingStepPlan step(String name, ChainStepDecisionPolicy policy) {
        ServiceOperation operation = new ServiceOperation();
        operation.setOperationName(name);
        return new RoutingStepPlan(name, operation, "direct:op." + name,
                (exchange, context) -> context.originalRequest(), policy,
                new RoutingStepObservationContext("service", null, null, 0));
    }

    static ChainStepDecisionPolicy policy(ChainStepDecision decision) {
        return new ChainStepDecisionPolicy() {
            @Override
            public String code() {
                return decision.name();
            }

            @Override
            public ChainStepDecision decide(ChainStepDecisionContext context) {
                return decision;
            }
        };
    }
}
