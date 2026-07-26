package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.apache.camel.Exchange;

public interface TaskWorkflowEngine {
    String engineType();

    boolean supports(TaskWorkflowStepType stepType);

    Object execute(TaskWorkflowStepType stepType, Exchange exchange);

    /**
     * Explicitly recognizes a normal provider result as successful. The safe
     * default leaves unknown and null results unclassified so core can return
     * RETRY_LATER.
     */
    default boolean isExplicitSuccess(
            TaskWorkflowStepType stepType,
            Object result
    ) {
        return false;
    }
}
