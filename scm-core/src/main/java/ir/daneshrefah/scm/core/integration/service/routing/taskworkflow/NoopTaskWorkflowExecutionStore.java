package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public class NoopTaskWorkflowExecutionStore implements TaskWorkflowExecutionStore {

    @Override
    public void record(
            TaskWorkflowCommand command,
            TaskWorkflowRole role,
            String state,
            String correlationId,
            Long processId
    ) {
        // Extension point for durable workflow recovery state.
    }
}
