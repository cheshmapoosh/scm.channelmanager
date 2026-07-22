package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.service.routing.ChainOnApproveRoutingEngine;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultSuccessChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingResultClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingRetryLaterException;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepObservationContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationRouteMetadataSetter;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyPaymasterRetryRoutingScenarioTest {

    @Test
    void unknownNabAttemptThenNormalizedDuplicateContinuesOnTheNextInvocation() {
        RoutingResultClassifier classifier = new RoutingResultClassifier();
        DefaultSuccessChainStepDecisionPolicy defaultPolicy =
                new DefaultSuccessChainStepDecisionPolicy(classifier);
        NabPaymasterRegistrationDecisionPolicy nabPolicy =
                new NabPaymasterRegistrationDecisionPolicy(defaultPolicy);
        RoutingStepPlan approve = step(
                "approve-paymaster-process-for-execution",
                defaultPolicy,
                "APPROVE_PROCESS",
                0
        );
        RoutingStepPlan nab = step(
                "register-paymaster-in-nab",
                nabPolicy,
                "BUSINESS_OPERATION",
                1
        );
        RoutingStepPlan permission = step(
                "grant-paymaster-account-access-in-scm",
                defaultPolicy,
                "BUSINESS_OPERATION",
                2
        );
        RoutingStepPlan complete = step(
                "complete-paymaster-process",
                defaultPolicy,
                "COMPLETE_PROCESS",
                3
        );
        RoutingPlan plan = new RoutingPlan(
                "my-paymaster:approve_and_execute",
                RoutingStrategy.CHAIN_ON_APPROVE,
                List.of(approve, nab, permission, complete)
        );
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setBody("send-request");
        ProducerTemplate producerTemplate = mock(ProducerTemplate.class);
        AtomicInteger approveCalls = new AtomicInteger();
        AtomicInteger nabCalls = new AtomicInteger();
        AtomicInteger permissionCalls = new AtomicInteger();
        AtomicInteger completionCalls = new AtomicInteger();
        when(producerTemplate.send(anyString(), same(exchange))).thenAnswer(invocation -> {
            String endpoint = invocation.getArgument(0);
            if (endpoint.equals(approve.endpointUri())) {
                approveCalls.incrementAndGet();
                exchange.getMessage().setBody(success());
            } else if (endpoint.equals(nab.endpointUri())) {
                if (nabCalls.incrementAndGet() == 1) {
                    exchange.setException(new SocketTimeoutException("NAB outcome is unknown"));
                    exchange.getMessage().setBody(null);
                } else {
                    exchange.getMessage().setBody(Message.builder()
                            .status(MessageStatus.SC_ERROR_BUSINESS)
                            .payload(JsonNodeFactory.instance.objectNode()
                                    .put("normalizedOutcome", "SUCCESS_ALREADY_APPLIED"))
                            .build());
                }
            } else if (endpoint.equals(permission.endpointUri())) {
                permissionCalls.incrementAndGet();
                exchange.getMessage().setBody(success());
            } else if (endpoint.equals(complete.endpointUri())) {
                completionCalls.incrementAndGet();
                exchange.getMessage().setBody(success());
            }
            return exchange;
        });
        RoutingStepExecutor executor = new RoutingStepExecutor(
                new ServiceOperationRouteMetadataSetter(),
                producerTemplate,
                classifier,
                mock(CoreObservationTraceSupport.class)
        );
        ChainOnApproveRoutingEngine engine = new ChainOnApproveRoutingEngine(executor);

        assertThrows(RoutingRetryLaterException.class, () -> engine.execute(exchange, plan));

        assertEquals(1, approveCalls.get());
        assertEquals(1, nabCalls.get());
        assertEquals(0, permissionCalls.get());
        assertEquals(0, completionCalls.get());

        RoutingExecutionResult retried = engine.execute(exchange, plan);

        assertEquals(ChainStepDecision.CONTINUE, retried.decision());
        assertEquals(2, approveCalls.get());
        assertEquals(2, nabCalls.get());
        assertEquals(1, permissionCalls.get());
        assertEquals(1, completionCalls.get());
        assertEquals(4, retried.context().stepResults().size());
    }

    private RoutingStepPlan step(
            String operationName,
            ChainStepDecisionPolicy policy,
            String role,
            int index
    ) {
        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName(operationName);
        return new RoutingStepPlan(
                operationName,
                operation,
                "direct:op." + operationName,
                (exchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext(
                        "my-paymaster",
                        "approve_and_execute",
                        role,
                        index
                )
        );
    }

    private Message success() {
        return Message.builder().status(MessageStatus.SC_SUCCESS).build();
    }
}
