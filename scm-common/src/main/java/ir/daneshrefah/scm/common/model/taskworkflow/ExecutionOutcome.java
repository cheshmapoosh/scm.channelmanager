package ir.daneshrefah.scm.common.model.taskworkflow;

import java.io.Serializable;

public record ExecutionOutcome(
        String executionId,
        String serviceCode,
        String inboundAction,
        String actionPlanName,
        String serviceVersion,
        String routingStrategy,
        String routingDecision,
        boolean retryable,
        String reasonCode
) implements Serializable {
}
