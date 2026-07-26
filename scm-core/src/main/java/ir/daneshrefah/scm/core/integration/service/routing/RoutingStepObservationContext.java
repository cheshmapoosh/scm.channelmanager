package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

public record RoutingStepObservationContext(
        String serviceCode,
        String inboundAction,
        TaskWorkflowStepType taskWorkflowStepType,
        int stepIndex,
        String spanKind
) {
    public RoutingStepObservationContext(
            String serviceCode,
            String inboundAction,
            TaskWorkflowStepType taskWorkflowStepType,
            int stepIndex
    ) {
        this(serviceCode, inboundAction, taskWorkflowStepType, stepIndex, "internal");
    }
}
