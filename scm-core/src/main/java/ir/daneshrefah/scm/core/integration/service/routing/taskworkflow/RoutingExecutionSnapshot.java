package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureDetails;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlanIdentity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record RoutingExecutionSnapshot(
        int schemaVersion,
        String executionId,
        String serviceCode,
        String inboundAction,
        String actionPlanName,
        String gatewayServiceVersion,
        String definitionId,
        String planFingerprint,
        RoutingStrategy routingStrategy,
        RoutingExecutionState executionState,
        RoutingDecision decision,
        Long processId,
        List<RoutingStepSnapshot> steps,
        RoutingRecoveryContext minimalRecoveryContext,
        StoredRoutingResponse storedResponse,
        RoutingFailureDetails storedFailure,
        String activeAttemptId,
        String activeStepId,
        Integer activeStepIndex,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public RoutingExecutionSnapshot {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(serviceCode, "serviceCode must not be null");
        Objects.requireNonNull(inboundAction, "inboundAction must not be null");
        Objects.requireNonNull(actionPlanName, "actionPlanName must not be null");
        Objects.requireNonNull(
                gatewayServiceVersion,
                "gatewayServiceVersion must not be null"
        );
        Objects.requireNonNull(definitionId, "definitionId must not be null");
        Objects.requireNonNull(planFingerprint, "planFingerprint must not be null");
        Objects.requireNonNull(routingStrategy, "routingStrategy must not be null");
        Objects.requireNonNull(executionState, "executionState must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public RoutingPlanIdentity planIdentity() {
        return new RoutingPlanIdentity(
                serviceCode,
                inboundAction,
                actionPlanName,
                definitionId,
                planFingerprint
        );
    }
}
