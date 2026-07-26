package ir.daneshrefah.scm.provider.task.workflow;

public class TaskWorkflowAttemptConflictException
        extends TaskWorkflowRecoveryException {

    public TaskWorkflowAttemptConflictException(String message) {
        super(message);
    }
}
