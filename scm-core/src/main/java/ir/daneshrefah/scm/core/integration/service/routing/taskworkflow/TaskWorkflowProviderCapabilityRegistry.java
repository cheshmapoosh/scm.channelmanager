package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowProviderCapability;

import java.util.List;

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
                            + "; found " + matching.size());
        }
        if (!matching.getFirst().supports(providerUri, stepType)) {
            throw new IllegalStateException(
                    "Task provider does not support stepType=" + stepType
                            + " for providerUri=" + providerUri);
        }
    }
}
