package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

public interface TaskWorkflowProviderCapability {

    boolean supports(String providerUri);

    boolean supports(
            String providerUri,
            TaskWorkflowStepType stepType
    );
}
