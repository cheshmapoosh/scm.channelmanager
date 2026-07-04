package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

import java.util.Objects;

public record ChainOnApproveStepPlan(
        ServiceOperation serviceOperation,
        int executionOrder,
        String operationEndpointUri,
        OperationApprovalPolicy approvalPolicy,
        Definition approvalDefinition
) {
    public ChainOnApproveStepPlan {
        Objects.requireNonNull(serviceOperation, "serviceOperation must not be null");
        Objects.requireNonNull(operationEndpointUri, "operationEndpointUri must not be null");
        Objects.requireNonNull(approvalPolicy, "approvalPolicy must not be null");
        Objects.requireNonNull(approvalDefinition, "approvalDefinition must not be null");
    }
}
