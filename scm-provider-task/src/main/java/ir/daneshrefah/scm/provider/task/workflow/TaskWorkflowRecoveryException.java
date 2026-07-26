package ir.daneshrefah.scm.provider.task.workflow;

public class TaskWorkflowRecoveryException extends RuntimeException {

    public TaskWorkflowRecoveryException(String message) {
        super(message);
    }

    public TaskWorkflowRecoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
