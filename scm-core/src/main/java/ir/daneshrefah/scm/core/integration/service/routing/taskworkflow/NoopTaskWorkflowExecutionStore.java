package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

public class NoopTaskWorkflowExecutionStore implements TaskWorkflowExecutionStore {

    @Override
    public void record(
            TaskWorkflowCommand command,
            TaskWorkflowStepType stepType,
            String state,
            String correlationId,
            Long processId
    ) {
        // Extension point for durable workflow recovery state.
    }
}
