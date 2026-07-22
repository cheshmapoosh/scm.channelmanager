package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.EOFException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RoutingResultClassifierContractTest {
    private RoutingResultClassifier classifier;
    private DefaultSuccessChainStepDecisionPolicy policy;

    @BeforeEach
    void setUp() {
        classifier = new RoutingResultClassifier();
        policy = new DefaultSuccessChainStepDecisionPolicy(classifier);
    }

    @Test
    void successfulStandardMessageContinues() {
        Message response = Message.builder()
                .status(MessageStatus.SC_SUCCESS)
                .build();

        assertEquals(RoutingResultClassifier.Result.SUCCESS,
                classifier.classify(response, null));
        assertEquals(ChainStepDecision.CONTINUE, decide(response, null));
    }

    @Test
    void nullStatusMessageIsAnUnknownOutcome() {
        Message response = Message.builder().build();

        assertEquals(RoutingResultClassifier.Result.TEMPORARY_OR_UNKNOWN,
                classifier.classify(response, null));
        assertEquals(ChainStepDecision.RETRY_LATER, decide(response, null));
    }

    @Test
    void networkInterruptionIsRetryable() {
        EOFException failure = new EOFException("NAB connection ended before a response");

        assertEquals(RoutingResultClassifier.Result.TEMPORARY_OR_UNKNOWN,
                classifier.classify(null, failure));
        assertEquals(ChainStepDecision.RETRY_LATER, decide(null, failure));
    }

    @Test
    void scmBusinessExceptionIsDefinitiveEvenWhenItsCauseIsNetworkRelated() {
        AbstractBaseException failure = new DefinitiveScmException(
                new EOFException("closed")
        );

        assertEquals(RoutingResultClassifier.Result.DEFINITIVE_FAILURE,
                classifier.classify(null, failure));
        assertEquals(ChainStepDecision.FAIL, decide(null, failure));
    }

    @Test
    void unsuccessfulMessageAndFaultAreDefinitive() {
        Message message = Message.builder()
                .status(MessageStatus.SC_ERROR_BUSINESS)
                .build();
        ScmFault fault = ScmFault.builder()
                .status(MessageStatus.SC_ERROR_VALIDATION)
                .build();

        assertEquals(RoutingResultClassifier.Result.DEFINITIVE_FAILURE,
                classifier.classify(message, null));
        assertEquals(ChainStepDecision.FAIL, decide(message, null));
        assertEquals(RoutingResultClassifier.Result.DEFINITIVE_FAILURE,
                classifier.classify(fault, null));
        assertEquals(ChainStepDecision.FAIL, decide(fault, null));
    }

    @Test
    void explicitJsonFailureIsDefinitive() {
        Object response = JsonNodeFactory.instance.objectNode()
                .put("successful", false);

        assertEquals(RoutingResultClassifier.Result.DEFINITIVE_FAILURE,
                classifier.classify(response, null));
        assertEquals(ChainStepDecision.FAIL, decide(response, null));
    }

    @Test
    void unrecognizedJsonOutcomeIsUnknownButOpaqueDataIsSuccessful() {
        Object unrecognizedOutcome = JsonNodeFactory.instance.objectNode()
                .put("status", "NAB_NEW_UNCLASSIFIED_STATUS")
                .put("responseCode", "unconfigured-code");
        Object opaqueData = JsonNodeFactory.instance.objectNode()
                .put("registrationId", "nab-registration-id");

        assertEquals(RoutingResultClassifier.Result.TEMPORARY_OR_UNKNOWN,
                classifier.classify(unrecognizedOutcome, null));
        assertEquals(ChainStepDecision.RETRY_LATER,
                decide(unrecognizedOutcome, null));
        assertEquals(RoutingResultClassifier.Result.SUCCESS,
                classifier.classify(opaqueData, null));
        assertEquals(ChainStepDecision.CONTINUE, decide(opaqueData, null));
    }

    private ChainStepDecision decide(Object response, Throwable failure) {
        ServiceOperation operation = new ServiceOperation();
        operation.setOperationName("operation");
        RoutingStepPlan step = new RoutingStepPlan(
                "operation",
                operation,
                "direct:op.operation",
                (exchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext("service", null, null, 0)
        );
        return policy.decide(new ChainStepDecisionContext(
                mock(Exchange.class),
                step,
                new RoutingExecutionContext("request"),
                response,
                failure
        ));
    }

    private static final class DefinitiveScmException extends AbstractBaseException {
        private DefinitiveScmException(Throwable cause) {
            super("SCM business failure", cause);
        }

        @Override
        public ExceptionInformation getExceptionInformation() {
            return ExceptionInformationBuilder.createInstance()
                    .buildWithStatus(MessageStatus.SC_ERROR_BUSINESS);
        }
    }
}
