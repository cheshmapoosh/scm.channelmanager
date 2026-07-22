package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class RoutingRetryLaterException extends AbstractBaseException {
    private final transient Object response;
    private final transient RoutingExecutionContext executionContext;
    private final String operationName;

    RoutingRetryLaterException(
            String planId,
            RoutingStepPlan step,
            Object response,
            RoutingExecutionContext executionContext,
            Throwable cause
    ) {
        super("Routing outcome is temporary or unknown planId=" + planId
                + ", operationName=" + step.serviceOperation().getOperationName(), cause);
        this.operationName = step.serviceOperation().getOperationName();
        this.response = response;
        this.executionContext = executionContext;
    }

    public Object response() { return response; }
    public RoutingExecutionContext executionContext() { return executionContext; }
    public String operationName() { return operationName; }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder.createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER);
    }
}
