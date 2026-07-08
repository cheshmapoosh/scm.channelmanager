package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRouteContext;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRoutingHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskWorkflowServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final TaskWorkflowRoutePlanFactory routePlanFactory;
    private final TaskWorkflowCommandResolver commandResolver;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;
    private final TaskWorkflowOperationInvoker operationInvoker;
    /**
     * Command plans are resolved from startup configuration, but the command is selected per
     * request. Synchronous ProducerTemplate calls preserve one Exchange across conditional
     * operation-route invocations and let the coordinator classify each result before deciding
     * whether COMPLETE_PROCESS is safe. Targets remain limited to direct:op.* routes.
     */

    public TaskWorkflowServiceTargetRoutingHandler(
            TaskWorkflowRoutePlanFactory routePlanFactory,
            TaskWorkflowCommandResolver commandResolver,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator,
            TaskWorkflowOperationInvoker operationInvoker
    ) {
        this.routePlanFactory = routePlanFactory;
        this.commandResolver = commandResolver;
        this.payloadMapper = payloadMapper;
        this.transactionCoordinator = transactionCoordinator;
        this.operationInvoker = operationInvoker;
    }

    @Override
    public RoutingStrategy strategy() {
        return RoutingStrategy.TASK_WORKFLOW;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        TaskWorkflowRoutePlan routePlan = routePlanFactory.create(
                context.service(),
                context.routeDefinitions()
        );
        log.debug("Building TASK_WORKFLOW service target routeId={} serviceCode={} commandCount={}",
                context.route().getRouteId(),
                context.service().getCode(),
                routePlan.commandPlans().size());
        context.route().process(exchange -> execute(exchange, routePlan));
    }

    private void execute(Exchange exchange, TaskWorkflowRoutePlan routePlan) {
        TaskWorkflowCommand command = commandResolver.resolve(exchange);
        TaskWorkflowCommandPlan commandPlan = routePlan.requireCommandPlan(command);
        exchange.setProperty(TaskWorkflowExchangeProperties.COMMAND, command);
        exchange.setProperty(TaskWorkflowExchangeProperties.COMMAND_PLAN, commandPlan);

        if (command == TaskWorkflowCommand.APPROVE_AND_EXECUTE) {
            executeApproveAndExecute(exchange, commandPlan);
            return;
        }
        executeSimple(exchange, commandPlan);
    }

    private void executeSimple(Exchange exchange, TaskWorkflowCommandPlan commandPlan) {
        TaskWorkflowStepPlan step = commandPlan.steps().getFirst();
        Object request = payloadMapper.toSimpleRequest(exchange, step.role());
        invoke(exchange, step, request);
    }

    private void executeApproveAndExecute(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan
    ) {
        TaskWorkflowStepPlan approveStep =
                commandPlan.requireRole(TaskWorkflowRole.APPROVE_PROCESS);
        TaskWorkflowStepPlan businessStep =
                commandPlan.requireRole(TaskWorkflowRole.BUSINESS_OPERATION);
        TaskWorkflowStepPlan completeStep =
                commandPlan.requireRole(TaskWorkflowRole.COMPLETE_PROCESS);

        transactionCoordinator.beforeApprove(exchange);
        Object approveResponseValue = invoke(
                exchange,
                approveStep,
                payloadMapper.toApproveRequest(exchange)
        );
        requireControlOperationSuccess(approveStep, approveResponseValue);
        transactionCoordinator.afterApprove(
                exchange,
                approveResponseValue,
                payloadMapper.approveProcessId(approveResponseValue)
        );

        transactionCoordinator.beforeBusinessOperation(exchange);
        Object businessResponse;
        try {
            businessResponse = invoke(
                    exchange,
                    businessStep,
                    payloadMapper.toBusinessRequest(exchange)
            );
        } catch (RuntimeException exception) {
            if (handleBusinessException(exchange, completeStep, exception)) {
                exchange.getMessage().setBody(
                        payloadMapper.toDefinitiveBusinessFailureResponse(exchange)
                );
                return;
            }
            throw new TaskWorkflowUnknownBusinessResultException(
                    exchange.getProperty(TaskWorkflowExchangeProperties.PROCESS_ID, Long.class),
                    exception
            );
        }

        TaskWorkflowBusinessResultClassifier.BusinessResult businessResult =
                transactionCoordinator.afterBusinessOperation(exchange, businessResponse);
        if (businessResult == TaskWorkflowBusinessResultClassifier.BusinessResult.UNKNOWN) {
            transactionCoordinator.handleUnknownBusinessResult(exchange, null);
            throw new TaskWorkflowUnknownBusinessResultException(
                    exchange.getProperty(TaskWorkflowExchangeProperties.PROCESS_ID, Long.class)
            );
        }
        completeProcess(exchange, completeStep, businessResult);
        exchange.getMessage().setBody(businessResponse);
    }

    private boolean handleBusinessException(
            Exchange exchange,
            TaskWorkflowStepPlan completeStep,
            RuntimeException exception
    ) {
        if (transactionCoordinator.classifyException(exception)
                == TaskWorkflowExceptionClassifier.ExceptionResult.DEFINITIVE_FAILURE) {
            transactionCoordinator.handleDefinitiveBusinessFailure(exchange, exception);
            try {
                completeProcess(
                        exchange,
                        completeStep,
                        TaskWorkflowBusinessResultClassifier.BusinessResult.FAILURE
                );
            } catch (RuntimeException completionFailure) {
                preserveBusinessFailureContext(
                        exchange,
                        completeStep,
                        exception,
                        completionFailure
                );
                throw completionFailure;
            }
            return true;
        }
        transactionCoordinator.handleUnknownBusinessResult(exchange, exception);
        return false;
    }

    private void preserveBusinessFailureContext(
            Exchange exchange,
            TaskWorkflowStepPlan completeStep,
            RuntimeException businessFailure,
            RuntimeException completionFailure
    ) {
        if (businessFailure == completionFailure) {
            return;
        }
        Long processId = exchange.getProperty(
                TaskWorkflowExchangeProperties.PROCESS_ID,
                Long.class
        );
        completionFailure.addSuppressed(new IllegalStateException(
                "Original definitive business failure before COMPLETE_PROCESS(status=FAIL)"
                        + ", processId=" + (processId == null ? "<unknown>" : processId)
                        + ", completionOperationName="
                        + completeStep.serviceOperation().getOperationName(),
                businessFailure
        ));
    }

    private void completeProcess(
            Exchange exchange,
            TaskWorkflowStepPlan completeStep,
            TaskWorkflowBusinessResultClassifier.BusinessResult businessResult
    ) {
        transactionCoordinator.beforeCompleteProcess(exchange, businessResult);
        Object completionResponse = invoke(
                exchange,
                completeStep,
                payloadMapper.toCompleteProcessRequest(exchange, businessResult)
        );
        requireControlOperationSuccess(completeStep, completionResponse);
        transactionCoordinator.afterCompleteProcess(exchange, businessResult);
    }

    private Object invoke(
            Exchange exchange,
            TaskWorkflowStepPlan step,
            Object request
    ) {
        return operationInvoker.invoke(exchange, step, request);
    }

    private void requireControlOperationSuccess(
            TaskWorkflowStepPlan step,
            Object response
    ) {
        if (response instanceof ScmFault
                || response instanceof Message message && !message.isSuccessful()) {
            throw new IllegalStateException("TASK_WORKFLOW control operation failed for role="
                    + step.role() + ", operationName="
                    + step.serviceOperation().getOperationName());
        }
    }
}
