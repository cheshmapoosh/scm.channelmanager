package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChainOnApproveRoutingEngineTest {
    @Test
    void continueRunsInOrderAndSharesResults() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = mock(Exchange.class);
        when(exchange.getMessage()).thenReturn(mock(Message.class));
        ChainStepDecisionPolicy policy = policy(ChainStepDecision.CONTINUE);
        RoutingStepPlan first = FirstRoutingEngineTest.step("first", policy);
        RoutingStepPlan second = FirstRoutingEngineTest.step("second", policy);
        when(executor.execute(eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(first), any()))
                .thenAnswer(invocation -> {
                    RoutingExecutionContext context = invocation.getArgument(3);
                    context.record("first", "a");
                    return new RoutingStepExecutionResult("a", null, 1);
                });
        when(executor.execute(eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(second), any()))
                .thenAnswer(invocation -> {
                    RoutingExecutionContext context = invocation.getArgument(3);
                    String response = context.stepResults().get("first") + "b";
                    context.record("second", response);
                    return new RoutingStepExecutionResult(response, null, 1);
                });

        RoutingExecutionResult result = new ChainOnApproveRoutingEngine(executor).execute(exchange,
                new RoutingPlan("chain", RoutingStrategy.CHAIN_ON_APPROVE, List.of(first, second)));

        assertEquals("ab", result.response());
        assertEquals("a", result.context().stepResults().get("first"));
        assertEquals("ab", result.context().stepResults().get("second"));
        InOrder order = inOrder(executor);
        order.verify(executor).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(first), any());
        order.verify(executor).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(second), any());
    }

    @Test
    void retryLaterSkipsFollowingSteps() {
        assertStops(ChainStepDecision.RETRY_LATER, true);
    }

    @Test
    void failSkipsFollowingSteps() {
        assertStops(ChainStepDecision.FAIL, false);
    }

    @Test
    void retryLaterPreservesResponseFailureStepAndContext() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = exchange();
        RoutingStepPlan accepted = FirstRoutingEngineTest.step(
                "accepted", FirstRoutingEngineTest.policy(ChainStepDecision.CONTINUE));
        RoutingStepPlan retry = FirstRoutingEngineTest.step(
                "nab", FirstRoutingEngineTest.policy(ChainStepDecision.RETRY_LATER));
        RuntimeException failure = new RuntimeException("unknown outcome");
        when(executor.execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(accepted), any()))
                .thenAnswer(invocation -> {
                    RoutingExecutionContext context = invocation.getArgument(3);
                    context.record("accepted", "approved");
                    return new RoutingStepExecutionResult("approved", null, 1);
                });
        when(executor.execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(retry), any()))
                .thenReturn(new RoutingStepExecutionResult(
                        "provider-response", failure, 1, ChainStepDecision.RETRY_LATER, null));

        RoutingRetryLaterException thrown = assertThrows(RoutingRetryLaterException.class,
                () -> new ChainOnApproveRoutingEngine(executor).execute(exchange,
                        new RoutingPlan("chain", RoutingStrategy.CHAIN_ON_APPROVE,
                                List.of(accepted, retry))));

        assertEquals("provider-response", thrown.response());
        assertEquals("nab", thrown.operationName());
        assertEquals("approved", thrown.executionContext().stepResults().get("accepted"));
        assertSame(failure, thrown.getCause());
    }

    @Test
    void definitiveFailurePreservesOriginalRuntimeException() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = exchange();
        RoutingStepPlan failed = FirstRoutingEngineTest.step(
                "failed", FirstRoutingEngineTest.policy(ChainStepDecision.FAIL));
        IllegalStateException failure = new IllegalStateException("definitive");
        when(executor.execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(failed), any()))
                .thenReturn(new RoutingStepExecutionResult(null, failure, 1));

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> new ChainOnApproveRoutingEngine(executor).execute(exchange,
                        new RoutingPlan("chain", RoutingStrategy.CHAIN_ON_APPROVE,
                                List.of(failed))));

        assertSame(failure, thrown);
    }

    @Test
    void rejectsPlanForAnotherStrategy() {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = exchange();
        RoutingStepPlan step = FirstRoutingEngineTest.step("only", null);

        assertThrows(IllegalStateException.class,
                () -> new ChainOnApproveRoutingEngine(executor).execute(exchange,
                        new RoutingPlan("first", RoutingStrategy.FIRST, List.of(step))));

        verifyNoInteractions(executor);
    }

    private void assertStops(ChainStepDecision decision, boolean throwsRetry) {
        RoutingStepExecutor executor = mock(RoutingStepExecutor.class);
        Exchange exchange = exchange();
        RoutingStepPlan first = FirstRoutingEngineTest.step("first", policy(decision));
        RoutingStepPlan skipped = FirstRoutingEngineTest.step("skipped", policy(ChainStepDecision.CONTINUE));
        when(executor.execute(eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(first), any()))
                .thenReturn(new RoutingStepExecutionResult("result", null, 1, decision, null));
        ChainOnApproveRoutingEngine engine = new ChainOnApproveRoutingEngine(executor);
        RoutingPlan plan = new RoutingPlan("chain", RoutingStrategy.CHAIN_ON_APPROVE,
                List.of(first, skipped));
        if (throwsRetry) assertThrows(RoutingRetryLaterException.class,
                () -> engine.execute(exchange, plan));
        else assertEquals(ChainStepDecision.FAIL, engine.execute(exchange, plan).decision());
        verify(executor, never()).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(skipped), any());
    }

    private ChainStepDecisionPolicy policy(ChainStepDecision decision) {
        return FirstRoutingEngineTest.policy(decision);
    }

    private Exchange exchange() {
        Exchange exchange = mock(Exchange.class);
        when(exchange.getMessage()).thenReturn(mock(Message.class));
        return exchange;
    }
}
