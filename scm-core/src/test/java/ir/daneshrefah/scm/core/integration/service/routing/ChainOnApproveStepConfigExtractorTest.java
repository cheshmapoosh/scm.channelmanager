package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChainOnApproveStepConfigExtractorTest {
    private static final String SERVICE_CODE = "paymaster-send";
    private static final String OPERATION_NAME = "register-paymaster-in-nab";

    private final ChainOnApproveStepConfigExtractor extractor =
            new ChainOnApproveStepConfigExtractor(new ObjectMapper());

    @Test
    void extractsAndTrimsConfiguredDecisionPolicy() {
        ChainOnApproveStepConfig config = extract("""
                {
                  "executionOrder": 10,
                  "decisionPolicy": "  NAB_PAYMASTER_REGISTRATION  "
                }
                """);

        assertEquals(10, config.executionOrder());
        assertEquals("NAB_PAYMASTER_REGISTRATION", config.decisionPolicy());
    }

    @Test
    void selectsDefaultSuccessWhenDecisionPolicyIsMissing() {
        ChainOnApproveStepConfig config = extract("""
                {
                  "executionOrder": 10
                }
                """);

        assertEquals(DefaultSuccessChainStepDecisionPolicy.CODE, config.decisionPolicy());
    }

    @Test
    void rejectsBlankDecisionPolicyWithServiceOperationAndFieldContext() {
        assertConfigurationFailure("""
                        {
                          "executionOrder": 10,
                          "decisionPolicy": "   "
                        }
                        """,
                "field=decisionPolicy",
                "non-blank");
    }

    @Test
    void rejectsNonStringDecisionPolicyWithServiceOperationAndFieldContext() {
        assertConfigurationFailure("""
                        {
                          "executionOrder": 10,
                          "decisionPolicy": 42
                        }
                        """,
                "field=decisionPolicy",
                "must be a string");
    }

    @Test
    void rejectsUnsupportedFieldInsteadOfFallingBackToDefaultSuccess() {
        assertConfigurationFailure("""
                        {
                          "executionOrder": 10,
                          "unsupportedPolicyField": "CUSTOM_POLICY"
                        }
                        """,
                "field=unsupportedPolicyField",
                "Unsupported CHAIN_ON_APPROVE step configuration field");
    }

    @Test
    void rejectsUnsupportedFieldEvenWhenDecisionPolicyIsValid() {
        assertConfigurationFailure("""
                        {
                          "executionOrder": 10,
                          "decisionPolicy": "DEFAULT_SUCCESS",
                          "unsupportedPolicyField": "CUSTOM_POLICY"
                        }
                        """,
                "field=unsupportedPolicyField",
                "Unsupported CHAIN_ON_APPROVE step configuration field");
    }

    @Test
    void rejectsUnknownDecisionPolicyDuringRoutePlanConstruction() {
        Service service = service();
        ServiceOperation unknownPolicyOperation = operation(OPERATION_NAME, """
                {
                  "executionOrder": 10,
                  "decisionPolicy": "UNKNOWN_POLICY"
                }
                """);
        ServiceOperation defaultPolicyOperation = operation(
                "grant-paymaster-account-access-in-scm", """
                        {
                          "executionOrder": 20
                        }
                        """);
        ServiceOperationSelector operationSelector = mock(ServiceOperationSelector.class);
        when(operationSelector.requireAtLeastTwoActive(service, RoutingStrategy.CHAIN_ON_APPROVE))
                .thenReturn(List.of(unknownPolicyOperation, defaultPolicyOperation));

        ChainStepDecisionPolicy defaultPolicy = mock(ChainStepDecisionPolicy.class);
        when(defaultPolicy.code()).thenReturn(DefaultSuccessChainStepDecisionPolicy.CODE);
        ChainOnApproveRoutePlanFactory factory = new ChainOnApproveRoutePlanFactory(
                operationSelector,
                new ServiceOperationEndpointResolver(),
                extractor,
                new ChainStepDecisionPolicyRegistry(List.of(defaultPolicy)),
                mock(RoutingOperationMetadataResolver.class)
        );

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> factory.create(service)
        );

        assertContains(failure, "serviceCode=" + SERVICE_CODE);
        assertContains(failure, "operationName=" + OPERATION_NAME);
        assertContains(failure, "decisionPolicy=UNKNOWN_POLICY");
        assertContains(failure, "Unsupported chain decision policy code=UNKNOWN_POLICY");
    }

    private ChainOnApproveStepConfig extract(String details) {
        return extractor.extract(service(), operation(OPERATION_NAME, details));
    }

    private void assertConfigurationFailure(String details, String fieldContext, String reason) {
        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> extract(details)
        );

        assertContains(failure, "serviceCode=" + SERVICE_CODE);
        assertContains(failure, "operationName=" + OPERATION_NAME);
        assertContains(failure, fieldContext);
        assertContains(failure, reason);
    }

    private void assertContains(IllegalStateException failure, String expected) {
        assertTrue(failure.getMessage().contains(expected), () ->
                "Expected error to contain '" + expected + "' but was: " + failure.getMessage());
    }

    private Service service() {
        Service service = new Service();
        service.setCode(SERVICE_CODE);
        return service;
    }

    private ServiceOperation operation(String operationName, String details) {
        Definition definition = new Definition();
        definition.setId("definition-" + operationName);
        definition.setName("definition-" + operationName);
        definition.setDetails(details);

        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName(operationName);
        operation.setDefinition(definition);
        return operation;
    }
}
