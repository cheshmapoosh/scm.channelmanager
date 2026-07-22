package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.service.routing.*;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MyPaymasterRoutingEngineTest {
    private RoutingStepExecutor executor;
    private Exchange exchange;
    private ChainOnApproveRoutingEngine engine;
    private DefaultSuccessChainStepDecisionPolicy defaultPolicy;
    private NabPaymasterRegistrationDecisionPolicy nabPolicy;
    private RoutingStepPlan approve;
    private RoutingStepPlan nab;
    private RoutingStepPlan permission;
    private RoutingStepPlan complete;

    @BeforeEach
    void setUp() {
        executor = mock(RoutingStepExecutor.class);
        exchange = mock(Exchange.class);
        when(exchange.getMessage()).thenReturn(mock(org.apache.camel.Message.class));
        engine = new ChainOnApproveRoutingEngine(executor);
        defaultPolicy = new DefaultSuccessChainStepDecisionPolicy(new RoutingResultClassifier());
        nabPolicy = new NabPaymasterRegistrationDecisionPolicy(defaultPolicy);
        approve = step("approve-paymaster-process-for-execution", defaultPolicy, 0);
        nab = step("register-paymaster-in-nab", nabPolicy, 1);
        permission = step("grant-paymaster-account-access-in-scm", defaultPolicy, 2);
        complete = step("complete-paymaster-process", defaultPolicy, 3);
    }

    @Test
    void allSuccessfulStepsExecuteInOrder() {
        success(approve, successMessage());
        success(nab, successMessage());
        success(permission, successMessage());
        success(complete, successMessage());

        RoutingExecutionResult result = engine.execute(exchange, plan());

        assertEquals(ChainStepDecision.CONTINUE, result.decision());
        assertEquals(4, result.context().stepResults().size());
        verifyInOrder(approve, nab, permission, complete);
    }

    @Test
    void nabUnknownOutcomeSkipsPermissionAndCompletion() {
        success(approve, successMessage());
        when(executor.execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(nab), any()))
                .thenReturn(new RoutingStepExecutionResult(
                        null,
                        new SocketTimeoutException("NAB response unknown"),
                        10,
                        ChainStepDecision.RETRY_LATER,
                        null));

        RoutingRetryLaterException retry = assertThrows(RoutingRetryLaterException.class,
                () -> engine.execute(exchange, plan()));

        assertEquals("register-paymaster-in-nab", retry.operationName());
        verify(executor, never()).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(permission), any());
        verify(executor, never()).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(complete), any());
    }

    @Test
    void normalizedNabDuplicateContinuesToPermissionAndCompletion() {
        success(approve, successMessage());
        success(nab, Message.builder()
                .status(MessageStatus.SC_ERROR_BUSINESS)
                .payload(JsonNodeFactory.instance.objectNode()
                        .put("normalizedOutcome", "SUCCESS_ALREADY_APPLIED"))
                .build());
        success(permission, successMessage());
        success(complete, successMessage());

        RoutingExecutionResult result = engine.execute(exchange, plan());

        assertEquals(ChainStepDecision.CONTINUE, result.decision());
        verifyInOrder(approve, nab, permission, complete);
    }

    @Test
    void definitiveNabFailureSkipsPermissionAndCompletion() {
        success(approve, successMessage());
        ScmFault failure = ScmFault.builder()
                .status(MessageStatus.SC_ERROR_BUSINESS)
                .build();
        success(nab, failure);

        RoutingExecutionResult result = engine.execute(exchange, plan());

        assertEquals(ChainStepDecision.FAIL, result.decision());
        assertEquals(nab, result.stoppedAt());
        verify(executor, never()).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(permission), any());
        verify(executor, never()).execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(complete), any());
    }

    private RoutingPlan plan() {
        return new RoutingPlan(
                "my-paymaster:approve_and_execute",
                RoutingStrategy.CHAIN_ON_APPROVE,
                List.of(approve, nab, permission, complete));
    }

    private RoutingStepPlan step(
            String operationName,
            ChainStepDecisionPolicy policy,
            int index
    ) {
        var operation = new ir.daneshrefah.scm.common.model.gateway.ServiceOperation();
        operation.setActive(true);
        operation.setOperationName(operationName);
        return new RoutingStepPlan(
                operationName,
                operation,
                "direct:op." + operationName,
                (currentExchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext(
                        "my-paymaster",
                        "approve_and_execute",
                        index == 0 ? "APPROVE_PROCESS"
                                : index == 3 ? "COMPLETE_PROCESS" : "BUSINESS_OPERATION",
                        index));
    }

    private Message successMessage() {
        return Message.builder().status(MessageStatus.SC_SUCCESS).build();
    }

    private void success(RoutingStepPlan step, Object response) {
        when(executor.execute(
                eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(step), any()))
                .thenAnswer(invocation -> {
                    RoutingExecutionContext context = invocation.getArgument(3);
                    ChainStepDecision decision = step.decisionPolicy().decide(
                            new ChainStepDecisionContext(
                                    exchange, step, context, response, null));
                    if (decision == ChainStepDecision.CONTINUE) {
                        context.record(step.serviceOperation().getOperationName(), response);
                    }
                    return new RoutingStepExecutionResult(
                            response, null, 1, decision,
                            step.decisionPolicy().normalizedOutcome(
                                    new ChainStepDecisionContext(
                                            exchange, step, context, response, null),
                                    decision));
                });
    }

    private void verifyInOrder(RoutingStepPlan... steps) {
        InOrder order = inOrder(executor);
        for (RoutingStepPlan step : steps) {
            order.verify(executor).execute(
                    eq(exchange), eq(RoutingStrategy.CHAIN_ON_APPROVE), eq(step), any());
        }
    }
}
