package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

import java.util.Objects;

public record RoutingStepPlan(
        String stepId,
        int stepIndex,
        ServiceOperation serviceOperation,
        String endpointUri,
        RoutingStepRequestFactory requestFactory,
        RoutingDecisionPolicy decisionPolicy,
        RoutingStepObservationContext observationContext
) {
    public RoutingStepPlan {
        Objects.requireNonNull(stepId, "stepId must not be null");
        if (stepId.isBlank()) {
            throw new IllegalArgumentException("stepId must not be blank");
        }
        if (stepIndex < 0) {
            throw new IllegalArgumentException("stepIndex must not be negative");
        }
        Objects.requireNonNull(serviceOperation, "serviceOperation must not be null");
        Objects.requireNonNull(endpointUri, "endpointUri must not be null");
        Objects.requireNonNull(requestFactory, "requestFactory must not be null");
        Objects.requireNonNull(observationContext, "observationContext must not be null");
    }
}
