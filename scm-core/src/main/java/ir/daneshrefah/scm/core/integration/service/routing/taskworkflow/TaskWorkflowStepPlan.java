package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

import java.util.Objects;

public record TaskWorkflowStepPlan(
        TaskWorkflowRole role,
        int executionOrder,
        ServiceOperation serviceOperation,
        String operationEndpointUri
) {
    public TaskWorkflowStepPlan {
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(serviceOperation, "serviceOperation must not be null");
        Objects.requireNonNull(operationEndpointUri, "operationEndpointUri must not be null");
    }
}
