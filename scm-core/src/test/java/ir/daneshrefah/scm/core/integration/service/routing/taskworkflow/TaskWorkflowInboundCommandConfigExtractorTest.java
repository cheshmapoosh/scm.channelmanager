package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskWorkflowInboundCommandConfigExtractorTest {
    private TaskWorkflowInboundCommandConfigExtractor extractor;
    private Service service;

    @BeforeEach
    void setUp() {
        extractor = new TaskWorkflowInboundCommandConfigExtractor(
                new ObjectMapper(), new TaskWorkflowCommandResolver());
        service = new Service();
        service.setCode("my-paymaster");
    }

    @Test
    void extractsFirstWithoutExecutionOrder() {
        TaskWorkflowInboundCommandConfig config = extract("""
                {
                  "inboundAction": "task_complete",
                  "taskWorkflow": {
                    "routingStrategy": "FIRST",
                    "steps": [
                      {
                        "role": "COMPLETE_TASK",
                        "operationName": "complete-paymaster-approval-task"
                      }
                    ]
                  }
                }
                """);

        assertEquals(TaskWorkflowCommand.COMPLETE_TASK, config.command());
        assertEquals(RoutingStrategy.FIRST, config.routingStrategy());
        assertEquals(List.of("complete-paymaster-approval-task"),
                config.steps().stream().map(
                        TaskWorkflowInboundCommandStepConfig::operationName).toList());
        assertNull(config.steps().getFirst().decisionPolicy());
    }

    @Test
    void preservesArrayOrderAndMultipleBusinessRoles() {
        TaskWorkflowInboundCommandConfig config = extract(chainJson());

        assertEquals(RoutingStrategy.CHAIN_ON_APPROVE, config.routingStrategy());
        assertEquals(List.of(
                        "approve-paymaster-process-for-execution",
                        "register-paymaster-in-nab",
                        "grant-paymaster-account-access-in-scm",
                        "complete-paymaster-process"),
                config.steps().stream().map(
                        TaskWorkflowInboundCommandStepConfig::operationName).toList());
        assertEquals(List.of(
                        TaskWorkflowRole.APPROVE_PROCESS,
                        TaskWorkflowRole.BUSINESS_OPERATION,
                        TaskWorkflowRole.BUSINESS_OPERATION,
                        TaskWorkflowRole.COMPLETE_PROCESS),
                config.steps().stream().map(
                        TaskWorkflowInboundCommandStepConfig::role).toList());
        assertEquals("NAB_PAYMASTER_REGISTRATION", config.steps().get(1).decisionPolicy());
    }

    @Test
    void requiresRoutingStrategy() {
        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> extract("""
                {
                  "inboundAction": "start",
                  "taskWorkflow": {
                    "steps": [{"role":"START_PROCESS","operationName":"start-paymaster-process"}]
                  }
                }
                """));

        assertAll(
                () -> assertTrue(failure.getMessage().contains("serviceCode=my-paymaster")),
                () -> assertTrue(failure.getMessage().contains("field=taskWorkflow.routingStrategy")));
    }

    @Test
    void rejectsExecutionStrategyInsteadOfUsingIt() {
        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> extract("""
                {
                  "inboundAction": "start",
                  "taskWorkflow": {
                    "executionStrategy": "FIRST",
                    "routingStrategy": "FIRST",
                    "steps": [{"role":"START_PROCESS","operationName":"start-paymaster-process"}]
                  }
                }
                """));

        assertTrue(failure.getMessage().contains("executionStrategy is not supported"));
    }

    @Test
    void requiresOperationNameWithStepContext() {
        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> extract("""
                {
                  "inboundAction": "task_complete",
                  "taskWorkflow": {
                    "routingStrategy": "FIRST",
                    "steps": [{"role":"COMPLETE_TASK"}]
                  }
                }
                """));

        assertAll(
                () -> assertTrue(failure.getMessage().contains(
                        "field=taskWorkflow.steps[0].operationName")),
                () -> assertTrue(failure.getMessage().contains("operationName is required")));
    }

    @Test
    void rejectsTaskWorkflowAsAnInboundExecutionStrategy() {
        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> extract("""
                {
                  "inboundAction": "start",
                  "taskWorkflow": {
                    "routingStrategy": "TASK_WORKFLOW",
                    "steps": [{"role":"START_PROCESS","operationName":"start-paymaster-process"}]
                  }
                }
                """));

        assertTrue(failure.getMessage().contains(
                "routingStrategy must be FIRST or CHAIN_ON_APPROVE"));
    }

    @Test
    void rejectsNonStringDecisionPolicy() {
        IllegalStateException failure = assertThrows(IllegalStateException.class, () -> extract("""
                {
                  "inboundAction": "approve_and_execute",
                  "taskWorkflow": {
                    "routingStrategy": "CHAIN_ON_APPROVE",
                    "steps": [
                      {
                        "role": "BUSINESS_OPERATION",
                        "operationName": "register-paymaster-in-nab",
                        "decisionPolicy": 42
                      }
                    ]
                  }
                }
                """));

        assertAll(
                () -> assertTrue(failure.getMessage().contains(
                        "field=taskWorkflow.steps[0].decisionPolicy")),
                () -> assertTrue(failure.getMessage().contains(
                        "decisionPolicy must be a string"))
        );
    }

    private TaskWorkflowInboundCommandConfig extract(String details) {
        return extractor.extract(service, inbound(details));
    }

    private InboundChannelServiceDefinition inbound(String details) {
        Definition definition = new Definition();
        definition.setId("definition-1");
        definition.setName("paymaster-command");
        definition.setDetails(details);
        InboundChannelServiceDefinition inbound = new InboundChannelServiceDefinition();
        inbound.setId("inbound-1");
        inbound.setDefinition(definition);
        return inbound;
    }

    static String chainJson() {
        return """
                {
                  "inboundAction": "approve_and_execute",
                  "taskWorkflow": {
                    "routingStrategy": "CHAIN_ON_APPROVE",
                    "steps": [
                      {
                        "role": "APPROVE_PROCESS",
                        "operationName": "approve-paymaster-process-for-execution"
                      },
                      {
                        "role": "BUSINESS_OPERATION",
                        "operationName": "register-paymaster-in-nab",
                        "decisionPolicy": "NAB_PAYMASTER_REGISTRATION"
                      },
                      {
                        "role": "BUSINESS_OPERATION",
                        "operationName": "grant-paymaster-account-access-in-scm"
                      },
                      {
                        "role": "COMPLETE_PROCESS",
                        "operationName": "complete-paymaster-process"
                      }
                    ]
                  }
                }
                """;
    }
}
