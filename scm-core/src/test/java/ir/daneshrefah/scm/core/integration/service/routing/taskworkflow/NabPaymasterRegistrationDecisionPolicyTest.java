package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.service.routing.*;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class NabPaymasterRegistrationDecisionPolicyTest {
    private NabPaymasterRegistrationDecisionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new NabPaymasterRegistrationDecisionPolicy(
                new DefaultSuccessChainStepDecisionPolicy(new RoutingResultClassifier()));
    }

    @Test
    void successfulNabResponseContinues() {
        Message response = Message.builder().status(MessageStatus.SC_SUCCESS).build();

        assertEquals(ChainStepDecision.CONTINUE, decide(response, null));
    }

    @Test
    void normalizedDuplicateRegistrationContinues() {
        Message response = Message.builder()
                .status(MessageStatus.SC_ERROR_BUSINESS)
                .payload(JsonNodeFactory.instance.objectNode()
                        .put("normalizedOutcome", "SUCCESS_ALREADY_APPLIED"))
                .build();

        assertEquals(ChainStepDecision.CONTINUE, decide(response, null));
        assertEquals(NabPaymasterRegistrationDecisionPolicy.SUCCESS_ALREADY_APPLIED,
                normalizedOutcome(response));
    }

    @Test
    void unknownNetworkOutcomeRetriesLater() {
        assertEquals(ChainStepDecision.RETRY_LATER,
                decide(null, new SocketTimeoutException("no NAB response")));
    }

    @Test
    void definitiveNabBusinessFailureFails() {
        ScmFault response = ScmFault.builder()
                .status(MessageStatus.SC_ERROR_BUSINESS)
                .build();

        assertEquals(ChainStepDecision.FAIL, decide(response, null));
    }

    private ChainStepDecision decide(Object response, Throwable failure) {
        ChainStepDecisionContext context = context(response, failure);
        return policy.decide(context);
    }

    private String normalizedOutcome(Object response) {
        ChainStepDecisionContext context = context(response, null);
        return policy.normalizedOutcome(context, policy.decide(context));
    }

    private ChainStepDecisionContext context(Object response, Throwable failure) {
        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName("register-paymaster-in-nab");
        RoutingStepPlan step = new RoutingStepPlan(
                operation.getOperationName(),
                operation,
                "direct:op.register-paymaster-in-nab",
                (exchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext(
                        "my-paymaster", "approve_and_execute", "BUSINESS_OPERATION", 1));
        return new ChainStepDecisionContext(
                mock(Exchange.class),
                step,
                new RoutingExecutionContext("request"),
                response,
                failure);
    }
}
