package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

public class ProviderTaskWorkflowCapability
        implements TaskWorkflowProviderCapability {
    private static final String PREFIX = "scm-task:";

    private final TaskWorkflowEngineRegistry engineRegistry;

    public ProviderTaskWorkflowCapability(
            TaskWorkflowEngineRegistry engineRegistry
    ) {
        this.engineRegistry = engineRegistry;
    }

    @Override
    public boolean supports(String providerUri) {
        return providerUri != null
                && providerUri.regionMatches(
                        true, 0, PREFIX, 0, PREFIX.length());
    }

    @Override
    public boolean supports(
            String providerUri,
            TaskWorkflowStepType stepType
    ) {
        String remaining = providerUri.substring(PREFIX.length());
        int queryIndex = remaining.indexOf('?');
        String providerCode = queryIndex < 0
                ? remaining
                : remaining.substring(0, queryIndex);
        return engineRegistry.engineFor(providerCode).supports(stepType);
    }
}
