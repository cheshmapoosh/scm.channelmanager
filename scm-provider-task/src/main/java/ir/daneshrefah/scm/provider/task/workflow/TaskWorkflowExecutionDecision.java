package ir.daneshrefah.scm.provider.task.workflow;

/**
 * Persistence representation of a task-workflow routing decision.
 */
public enum TaskWorkflowExecutionDecision {
    SUCCESS,
    RETRY_LATER,
    FAIL
}
