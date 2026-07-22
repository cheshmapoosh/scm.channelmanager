package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskWorkflowCommandPlanValidatorTest {
    private final TaskWorkflowCommandPlanValidator validator =
            new TaskWorkflowCommandPlanValidator();
    private Service service;

    @BeforeEach
    void setUp() {
        service = new Service();
        service.setCode("my-paymaster");
    }

    @Test
    void acceptsTaskCompleteAsOneCompleteTaskFirstStep() {
        assertDoesNotThrow(() -> validator.validate(service, config(
                TaskWorkflowCommand.COMPLETE_TASK,
                "task_complete",
                RoutingStrategy.FIRST,
                step(TaskWorkflowRole.COMPLETE_TASK, "complete-paymaster-approval-task"))));
    }

    @Test
    void rejectsEveryFirstPlanThatDoesNotHaveExactlyOneStep() {
        IllegalStateException empty = assertThrows(IllegalStateException.class,
                () -> validator.validate(service, config(
                        TaskWorkflowCommand.START, "start", RoutingStrategy.FIRST)));
        IllegalStateException multiple = assertThrows(IllegalStateException.class,
                () -> validator.validate(service, config(
                        TaskWorkflowCommand.START,
                        "start",
                        RoutingStrategy.FIRST,
                        step(TaskWorkflowRole.START_PROCESS, "start-one"),
                        step(TaskWorkflowRole.START_PROCESS, "start-two"))));

        assertTrue(empty.getMessage().contains("FIRST requires exactly one step"));
        assertTrue(multiple.getMessage().contains("FIRST requires exactly one step"));
    }

    @Test
    void rejectsTaskCompleteWithWrongRole() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> validator.validate(service, config(
                        TaskWorkflowCommand.COMPLETE_TASK,
                        "task_complete",
                        RoutingStrategy.FIRST,
                        step(TaskWorkflowRole.START_PROCESS, "wrong-operation"))));

        assertAll(
                () -> assertTrue(failure.getMessage().contains("serviceCode=my-paymaster")),
                () -> assertTrue(failure.getMessage().contains("inboundAction=task_complete")),
                () -> assertTrue(failure.getMessage().contains("routingStrategy=FIRST")),
                () -> assertTrue(failure.getMessage().contains("stepIndex=0")),
                () -> assertTrue(failure.getMessage().contains("role=START_PROCESS")),
                () -> assertTrue(failure.getMessage().contains("operationName=wrong-operation")));
    }

    @Test
    void acceptsApproveAndExecuteWithMultipleBusinessOperations() {
        assertDoesNotThrow(() -> validator.validate(service, config(
                TaskWorkflowCommand.APPROVE_AND_EXECUTE,
                "approve_and_execute",
                RoutingStrategy.CHAIN_ON_APPROVE,
                step(TaskWorkflowRole.APPROVE_PROCESS, "approve"),
                step(TaskWorkflowRole.BUSINESS_OPERATION, "register-in-nab"),
                step(TaskWorkflowRole.BUSINESS_OPERATION, "grant-in-scm"),
                step(TaskWorkflowRole.COMPLETE_PROCESS, "complete"))));
    }

    @Test
    void rejectsApproveAndExecuteWithInvalidMiddleRoleAndReportsThatStep() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> validator.validate(service, config(
                        TaskWorkflowCommand.APPROVE_AND_EXECUTE,
                        "approve_and_execute",
                        RoutingStrategy.CHAIN_ON_APPROVE,
                        step(TaskWorkflowRole.APPROVE_PROCESS, "approve"),
                        step(TaskWorkflowRole.COMPLETE_TASK, "wrong-middle"),
                        step(TaskWorkflowRole.COMPLETE_PROCESS, "complete"))));

        assertAll(
                () -> assertTrue(failure.getMessage().contains("stepIndex=1")),
                () -> assertTrue(failure.getMessage().contains("role=COMPLETE_TASK")),
                () -> assertTrue(failure.getMessage().contains("operationName=wrong-middle")),
                () -> assertTrue(failure.getMessage().contains(
                        "reason=middle roles must be BUSINESS_OPERATION")));
    }

    @Test
    void doesNotExposeCompleteProcessAsDirectFirstAction() {
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> validator.validate(service, config(
                        TaskWorkflowCommand.START,
                        "start",
                        RoutingStrategy.FIRST,
                        step(TaskWorkflowRole.COMPLETE_PROCESS, "complete-process"))));

        assertTrue(failure.getMessage().contains(
                "COMPLETE_PROCESS cannot be a direct inbound action"));
    }

    private TaskWorkflowInboundCommandConfig config(
            TaskWorkflowCommand command,
            String action,
            RoutingStrategy strategy,
            TaskWorkflowInboundCommandStepConfig... steps
    ) {
        return new TaskWorkflowInboundCommandConfig(
                command,
                action,
                strategy,
                List.of(steps),
                new InboundChannelServiceDefinition());
    }

    private TaskWorkflowInboundCommandStepConfig step(
            TaskWorkflowRole role,
            String operationName
    ) {
        return new TaskWorkflowInboundCommandStepConfig(role, operationName, null);
    }
}
