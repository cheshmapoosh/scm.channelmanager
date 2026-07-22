package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.exception.NabError;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.DefaultSuccessChainStepDecisionPolicy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingResultClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepObservationContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.Test;

import java.io.EOFException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class NabPaymasterRegistrationDecisionPolicyBoundaryTest {

    @Test
    void nabBusinessErrorIsDefinitiveEvenWhenItsCauseIsNetworkRelated() {
        NabPaymasterRegistrationDecisionPolicy policy =
                new NabPaymasterRegistrationDecisionPolicy(
                        new DefaultSuccessChainStepDecisionPolicy(
                                new RoutingResultClassifier()
                        )
                );
        NabError failure = new NabError(
                "configured-business-code",
                "NAB rejected the request",
                new EOFException("closed")
        );
        ServiceOperation operation = new ServiceOperation();
        operation.setOperationName("register-paymaster-in-nab");
        RoutingStepPlan step = new RoutingStepPlan(
                operation.getOperationName(),
                operation,
                "direct:op.register-paymaster-in-nab",
                (exchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext(
                        "my-paymaster",
                        "approve_and_execute",
                        "BUSINESS_OPERATION",
                        1
                )
        );

        ChainStepDecision decision = policy.decide(new ChainStepDecisionContext(
                mock(Exchange.class),
                step,
                new RoutingExecutionContext("request"),
                null,
                failure
        ));

        assertEquals(ChainStepDecision.FAIL, decision);
    }
}
