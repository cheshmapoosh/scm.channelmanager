package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public interface TaskWorkflowExecutionStore {

    void record(
            TaskWorkflowCommand command,
            TaskWorkflowRole role,
            String state,
            String correlationId,
            Long processId
    );
}
