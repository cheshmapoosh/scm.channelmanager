package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskWorkflowCommandPlanValidatorShapeTest {
    private final TaskWorkflowCommandPlanValidator validator =
            new TaskWorkflowCommandPlanValidator();
    private Service service;

    @BeforeEach
    void setUp() {
        service = new Service();
        service.setCode("my-paymaster");
    }

    @Test
    void rejectsWrongApproveAndExecuteFirstRole() {
        IllegalStateException failure = invalid(config(
                RoutingStrategy.CHAIN_ON_APPROVE,
                step(TaskWorkflowRole.BUSINESS_OPERATION, "wrong-first"),
                step(TaskWorkflowRole.BUSINESS_OPERATION, "register-paymaster-in-nab"),
                step(TaskWorkflowRole.COMPLETE_PROCESS, "complete-paymaster-process")
        ));

        assertContext(failure, 0, "BUSINESS_OPERATION", "wrong-first");
        assertTrue(failure.getMessage().contains("reason=first role must be APPROVE_PROCESS"));
    }

    @Test
    void rejectsWrongApproveAndExecuteLastRole() {
        IllegalStateException failure = invalid(config(
                RoutingStrategy.CHAIN_ON_APPROVE,
                step(TaskWorkflowRole.APPROVE_PROCESS, "approve-paymaster-process"),
                step(TaskWorkflowRole.BUSINESS_OPERATION, "register-paymaster-in-nab"),
                step(TaskWorkflowRole.BUSINESS_OPERATION, "wrong-last")
        ));

        assertContext(failure, 2, "BUSINESS_OPERATION", "wrong-last");
        assertTrue(failure.getMessage().contains("reason=last role must be COMPLETE_PROCESS"));
    }

    @Test
    void rejectsApproveAndExecuteWithoutABusinessStep() {
        IllegalStateException failure = invalid(config(
                RoutingStrategy.CHAIN_ON_APPROVE,
                step(TaskWorkflowRole.APPROVE_PROCESS, "approve-paymaster-process"),
                step(TaskWorkflowRole.COMPLETE_PROCESS, "complete-paymaster-process")
        ));

        assertContext(failure, 0, "APPROVE_PROCESS", "approve-paymaster-process");
        assertTrue(failure.getMessage().contains(
                "reason=approve_and_execute requires approve, one or more business, and complete steps"));
    }

    @Test
    void rejectsApproveAndExecuteUsingFirst() {
        IllegalStateException failure = invalid(config(
                RoutingStrategy.FIRST,
                step(TaskWorkflowRole.APPROVE_PROCESS, "approve-paymaster-process")
        ));

        assertContext(failure, 0, "APPROVE_PROCESS", "approve-paymaster-process");
        assertTrue(failure.getMessage().contains(
                "reason=approve_and_execute requires CHAIN_ON_APPROVE"));
    }

    private IllegalStateException invalid(TaskWorkflowInboundCommandConfig config) {
        return assertThrows(
                IllegalStateException.class,
                () -> validator.validate(service, config)
        );
    }

    private void assertContext(
            IllegalStateException failure,
            int index,
            String role,
            String operationName
    ) {
        assertAll(
                () -> assertTrue(failure.getMessage().contains("serviceCode=my-paymaster")),
                () -> assertTrue(failure.getMessage().contains(
                        "inboundAction=approve_and_execute")),
                () -> assertTrue(failure.getMessage().contains("stepIndex=" + index)),
                () -> assertTrue(failure.getMessage().contains("role=" + role)),
                () -> assertTrue(failure.getMessage().contains(
                        "operationName=" + operationName))
        );
    }

    private TaskWorkflowInboundCommandConfig config(
            RoutingStrategy strategy,
            TaskWorkflowInboundCommandStepConfig... steps
    ) {
        return new TaskWorkflowInboundCommandConfig(
                TaskWorkflowCommand.APPROVE_AND_EXECUTE,
                "approve_and_execute",
                strategy,
                List.of(steps),
                new InboundChannelServiceDefinition()
        );
    }

    private TaskWorkflowInboundCommandStepConfig step(
            TaskWorkflowRole role,
            String operationName
    ) {
        return new TaskWorkflowInboundCommandStepConfig(role, operationName, null);
    }
}
