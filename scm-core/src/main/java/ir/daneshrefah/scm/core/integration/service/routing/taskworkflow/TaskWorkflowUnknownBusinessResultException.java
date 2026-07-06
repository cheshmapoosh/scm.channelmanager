package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

public class TaskWorkflowUnknownBusinessResultException extends IllegalStateException {

    public TaskWorkflowUnknownBusinessResultException(Long processId) {
        super("TASK_WORKFLOW business result is unknown for processId="
                + (processId == null ? "<unknown>" : processId)
                + "; COMPLETE_PROCESS was not called");
    }
}
