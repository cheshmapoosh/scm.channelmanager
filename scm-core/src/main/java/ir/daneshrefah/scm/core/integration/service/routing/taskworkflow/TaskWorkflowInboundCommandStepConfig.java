package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

public record TaskWorkflowInboundCommandStepConfig(
        TaskWorkflowStepType stepType,
        String operationName,
        String decisionPolicy
) {
}
