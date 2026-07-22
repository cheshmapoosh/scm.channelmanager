package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

import java.util.Objects;

public record RoutingStepPlan(
        String stepName,
        ServiceOperation serviceOperation,
        String endpointUri,
        RoutingStepRequestFactory requestFactory,
        ChainStepDecisionPolicy decisionPolicy,
        RoutingStepObservationContext observationContext
) {
    public RoutingStepPlan {
        Objects.requireNonNull(stepName, "stepName must not be null");
        Objects.requireNonNull(serviceOperation, "serviceOperation must not be null");
        Objects.requireNonNull(endpointUri, "endpointUri must not be null");
        Objects.requireNonNull(requestFactory, "requestFactory must not be null");
        Objects.requireNonNull(observationContext, "observationContext must not be null");
    }
}
