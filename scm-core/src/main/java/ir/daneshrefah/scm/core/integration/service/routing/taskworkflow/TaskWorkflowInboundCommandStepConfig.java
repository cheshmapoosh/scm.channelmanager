package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public record TaskWorkflowInboundCommandStepConfig(
        TaskWorkflowRole role,
        int executionOrder
) {
}
