package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class RoutingFailureException extends AbstractBaseException {
    private final RoutingFailureDetails details;

    public RoutingFailureException(
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingDecisionResult decision,
            Throwable cause
    ) {
        this(details(plan, step, decision), cause);
    }

    public RoutingFailureException(
            RoutingFailureDetails details,
            Throwable cause
    ) {
        super(message(details), cause);
        this.details = details;
    }

    public RoutingFailureDetails details() {
        return details;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        MessageStatus status = details == null || details.messageStatus() == null
                ? MessageStatus.SC_ERROR_BUSINESS
                : details.messageStatus();
        return ExceptionInformationBuilder.createInstance()
                .buildWithStatus(status);
    }

    private static RoutingFailureDetails details(
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingDecisionResult decision
    ) {
        return new RoutingFailureDetails(
                plan.identity(),
                plan.planId(),
                step.stepId(),
                step.stepIndex(),
                step.serviceOperation().getOperationName(),
                RoutingDecision.FAIL,
                decision.messageStatus(),
                decision.reasonCode(),
                decision.reasonMessage(),
                decision.normalizedOutcome()
        );
    }

    private static String message(RoutingFailureDetails details) {
        return "Routing failed planId=" + details.planId()
                + ", stepId=" + details.stepId()
                + ", stepIndex=" + details.stepIndex()
                + ", operationName=" + details.operationName()
                + ", reasonCode=" + details.reasonCode()
                + ", messageStatus=" + details.messageStatus()
                + ", normalizedOutcome=" + details.normalizedOutcome();
    }
}
