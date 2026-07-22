package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.service.routing.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TaskWorkflowRoutePlanFactoryTest {
    private TaskWorkflowRoutePlanFactory factory;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        RoutingResultClassifier classifier = new RoutingResultClassifier();
        DefaultSuccessChainStepDecisionPolicy defaultPolicy =
                new DefaultSuccessChainStepDecisionPolicy(classifier);
        ChainStepDecisionPolicyRegistry policyRegistry =
                new ChainStepDecisionPolicyRegistry(List.of(
                        defaultPolicy,
                        new NabPaymasterRegistrationDecisionPolicy(defaultPolicy)));
        RoutingOperationMetadataResolver operationMetadataResolver =
                org.mockito.Mockito.mock(RoutingOperationMetadataResolver.class);
        org.mockito.Mockito.when(operationMetadataResolver.spanKind(
                        org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("internal");
        factory = new TaskWorkflowRoutePlanFactory(
                new ServiceOperationSelector(),
                new ServiceOperationEndpointResolver(),
                new TaskWorkflowInboundCommandConfigExtractor(
                        objectMapper, new TaskWorkflowCommandResolver()),
                new TaskWorkflowCommandPlanValidator(),
                policyRegistry,
                new TaskWorkflowPayloadMapper(objectMapper),
                org.mockito.Mockito.mock(TaskWorkflowTransactionCoordinator.class),
                operationMetadataResolver);
    }

    @Test
    void resolvesOperationsByNameAndPreservesConfiguredArrayOrder() {
        Service service = service(
                operation("complete-paymaster-process"),
                operation("grant-paymaster-account-access-in-scm"),
                operation("register-paymaster-in-nab"),
                operation("approve-paymaster-process-for-execution"));
        Map<String, ServiceOperation> byName = service.getServiceOperations().stream()
                .collect(Collectors.toMap(ServiceOperation::getOperationName, Function.identity()));

        TaskWorkflowCommandPlan commandPlan = factory.create(
                        service, List.of(inbound(TaskWorkflowInboundCommandConfigExtractorTest.chainJson())))
                .requireCommandPlan(TaskWorkflowCommand.APPROVE_AND_EXECUTE);

        assertEquals(RoutingStrategy.CHAIN_ON_APPROVE,
                commandPlan.routingPlan().routingStrategy());
        assertEquals(List.of(
                        "approve-paymaster-process-for-execution",
                        "register-paymaster-in-nab",
                        "grant-paymaster-account-access-in-scm",
                        "complete-paymaster-process"),
                commandPlan.routingPlan().steps().stream()
                        .map(RoutingStepPlan::stepName)
                        .toList());
        commandPlan.routingPlan().steps().forEach(step ->
                assertSame(byName.get(step.stepName()), step.serviceOperation()));
        assertEquals(List.of(
                        "APPROVE_PROCESS",
                        "BUSINESS_OPERATION",
                        "BUSINESS_OPERATION",
                        "COMPLETE_PROCESS"),
                commandPlan.routingPlan().steps().stream()
                        .map(step -> step.observationContext().taskRole())
                        .toList());
        assertEquals(NabPaymasterRegistrationDecisionPolicy.CODE,
                commandPlan.routingPlan().steps().get(1).decisionPolicy().code());
        assertEquals(DefaultSuccessChainStepDecisionPolicy.CODE,
                commandPlan.routingPlan().steps().get(2).decisionPolicy().code());
    }

    @Test
    void rejectsDuplicateOperationNameWithinOneInboundAction() {
        Service service = service(
                operation("approve"), operation("register"), operation("complete"));
        String details = """
                {
                  "inboundAction":"approve_and_execute",
                  "taskWorkflow":{
                    "routingStrategy":"CHAIN_ON_APPROVE",
                    "steps":[
                      {"role":"APPROVE_PROCESS","operationName":"approve"},
                      {"role":"BUSINESS_OPERATION","operationName":"register"},
                      {"role":"BUSINESS_OPERATION","operationName":"register"},
                      {"role":"COMPLETE_PROCESS","operationName":"complete"}
                    ]
                  }
                }
                """;

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> factory.create(service, List.of(inbound(details))));

        assertAll(
                () -> assertTrue(failure.getMessage().contains("inboundAction=approve_and_execute")),
                () -> assertTrue(failure.getMessage().contains("stepIndex=2")),
                () -> assertTrue(failure.getMessage().contains("operationName=register")),
                () -> assertTrue(failure.getMessage().contains(
                        "reason=duplicate operationName in one inbound action")));
    }

    @Test
    void missingOperationFailsAtStartupWithActionAndStepContext() {
        Service service = service(
                operation("approve-paymaster-process-for-execution"),
                operation("register-paymaster-in-nab"),
                operation("complete-paymaster-process"));

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> factory.create(service, List.of(inbound(
                        TaskWorkflowInboundCommandConfigExtractorTest.chainJson()))));

        assertAll(
                () -> assertTrue(failure.getMessage().contains("serviceCode=my-paymaster")),
                () -> assertTrue(failure.getMessage().contains("inboundAction=approve_and_execute")),
                () -> assertTrue(failure.getMessage().contains("routingStrategy=CHAIN_ON_APPROVE")),
                () -> assertTrue(failure.getMessage().contains("stepIndex=2")),
                () -> assertTrue(failure.getMessage().contains(
                        "operationName=grant-paymaster-account-access-in-scm")),
                () -> assertTrue(failure.getMessage().contains(
                        "reason=no matching active ServiceOperation")));
    }

    @Test
    void resolvesNameCaseInsensitivelyInsteadOfUsingRoleAsIdentity() {
        ServiceOperation operation = operation("start-paymaster-process");
        Service service = service(operation);
        String details = """
                {
                  "inboundAction":"start",
                  "taskWorkflow":{
                    "routingStrategy":"FIRST",
                    "steps":[{
                      "role":"START_PROCESS",
                      "operationName":"START-PAYMASTER-PROCESS"
                    }]
                  }
                }
                """;

        RoutingStepPlan step = factory.create(service, List.of(inbound(details)))
                .requireCommandPlan(TaskWorkflowCommand.START)
                .routingPlan().steps().getFirst();

        assertSame(operation, step.serviceOperation());
    }

    private Service service(ServiceOperation... operations) {
        Service service = new Service();
        service.setCode("my-paymaster");
        service.setRoutingStrategy(RoutingStrategy.TASK_WORKFLOW);
        service.setServiceOperations(List.of(operations));
        return service;
    }

    private ServiceOperation operation(String operationName) {
        ServiceOperation operation = new ServiceOperation();
        operation.setActive(true);
        operation.setOperationName(operationName);
        return operation;
    }

    private ChannelServiceDefinition inbound(String details) {
        Definition definition = new Definition();
        definition.setId("definition-" + Math.abs(details.hashCode()));
        definition.setName("paymaster-command");
        definition.setDetails(details);
        InboundChannelServiceDefinition inbound = new InboundChannelServiceDefinition();
        inbound.setId("inbound-" + Math.abs(details.hashCode()));
        inbound.setDefinition(definition);
        return inbound;
    }
}
