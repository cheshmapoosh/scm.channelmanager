package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionContext;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecisionPolicy;

final class TaskWorkflowStepDecisionPolicy implements ChainStepDecisionPolicy {
    private final TaskWorkflowStepType stepType;
    private final ChainStepDecisionPolicy delegate;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;

    TaskWorkflowStepDecisionPolicy(
            TaskWorkflowStepType stepType,
            ChainStepDecisionPolicy delegate,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator
    ) {
        this.stepType = stepType;
        this.delegate = delegate;
        this.payloadMapper = payloadMapper;
        this.transactionCoordinator = transactionCoordinator;
    }

    @Override
    public String code() {
        return delegate.code();
    }

    @Override
    public ChainStepDecision decide(ChainStepDecisionContext context) {
        ChainStepDecision decision = delegate.decide(context);
        if (context.failure() != null && decision == ChainStepDecision.CONTINUE) {
            decision = ChainStepDecision.FAIL;
        }
        switch (stepType) {
            case APPROVE_PROCESS -> handleApprove(context, decision);
            case BUSINESS_OPERATION -> transactionCoordinator.afterBusinessOperation(
                    context.exchange(),
                    context.response(),
                    decision,
                    context.failure()
            );
            case COMPLETE_PROCESS -> {
                if (decision == ChainStepDecision.CONTINUE) {
                    transactionCoordinator.afterCompleteProcess(context.exchange());
                }
            }
            default -> {
            }
        }
        return decision;
    }

    @Override
    public String normalizedOutcome(
            ChainStepDecisionContext context,
            ChainStepDecision decision
    ) {
        return delegate.normalizedOutcome(context, decision);
    }

    private void handleApprove(
            ChainStepDecisionContext context,
            ChainStepDecision decision
    ) {
        if (decision != ChainStepDecision.CONTINUE) {
            return;
        }
        payloadMapper.rememberApproveContext(
                context.response(), context.executionContext());
        Long processId = context.executionContext().processId();
        if (processId == null) {
            processId = context.exchange().getProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    Long.class
            );
            context.executionContext().processId(processId);
        }
        transactionCoordinator.afterApprove(
                context.exchange(),
                context.response(),
                processId
        );
    }
}
