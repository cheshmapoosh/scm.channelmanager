package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

import java.util.List;

/**
 * Provider-owned registry for task-workflow capability declarations.
 */
public class TaskWorkflowProviderCapabilityRegistry {
    private final List<TaskWorkflowProviderCapability> capabilities;

    public TaskWorkflowProviderCapabilityRegistry(
            List<TaskWorkflowProviderCapability> capabilities
    ) {
        this.capabilities = List.copyOf(capabilities);
    }

    public void requireSupported(
            String serviceCode,
            String providerUri,
            TaskWorkflowStepType stepType
    ) {
        List<TaskWorkflowProviderCapability> matching = capabilities.stream()
                .filter(capability -> capability.supports(providerUri))
                .toList();
        if (matching.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one TASK_WORKFLOW provider capability "
                            + "for serviceCode=" + serviceCode
                            + ", providerUri=" + providerUri
                            + "; found " + matching.size()
            );
        }
        if (!matching.getFirst().supports(providerUri, stepType)) {
            throw new IllegalStateException(
                    "Task provider does not support stepType=" + stepType
                            + " for providerUri=" + providerUri
            );
        }
    }
}
