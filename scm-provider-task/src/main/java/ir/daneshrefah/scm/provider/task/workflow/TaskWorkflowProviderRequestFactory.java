package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

/**
 * Formats requests for task-provider-specific workflow operations without
 * exposing Camel or core routing types.
 */
public interface TaskWorkflowProviderRequestFactory {

    boolean supports(TaskWorkflowStepType stepType);

    Object create(
            TaskWorkflowStepType stepType,
            TaskWorkflowProviderRequestContext context
    );
}
