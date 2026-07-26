package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

public interface TaskWorkflowExecutionStore {

    void record(
            TaskWorkflowCommand command,
            TaskWorkflowStepType stepType,
            String state,
            String correlationId,
            Long processId
    );
}
