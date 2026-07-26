package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRouteContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskProviderTaskWorkflowRuntime implements TaskWorkflowRuntime {

    private final TaskWorkflowRoutePlanFactory routePlanFactory;
    private final TaskWorkflowExecutionCoordinator executionCoordinator;

    public TaskProviderTaskWorkflowRuntime(
            TaskWorkflowRoutePlanFactory routePlanFactory,
            TaskWorkflowExecutionCoordinator executionCoordinator
    ) {
        this.routePlanFactory = routePlanFactory;
        this.executionCoordinator = executionCoordinator;
    }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        String serviceCode = String.valueOf(context.service().getCode());
        executionCoordinator.verifyInfrastructureAvailable(serviceCode);
        TaskWorkflowRoutePlan routePlan = routePlanFactory.create(
                context.service()
        );
        log.debug(
                "Building TASK_WORKFLOW service target routeId={} "
                        + "serviceCode={} commandCount={}",
                context.route().getRouteId(),
                serviceCode,
                routePlan.commandPlans().size()
        );
        context.route().process(exchange -> {
            String inboundAction = exchange.getProperty(
                    Message.INBOUND_ROUTE_ACTION,
                    String.class
            );
            TaskWorkflowCommandPlan commandPlan =
                    routePlan.requireCommandPlan(inboundAction);
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.COMMAND,
                    commandPlan.command()
            );
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.COMMAND_PLAN,
                    commandPlan
            );
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.ACTION_PLAN_NAME,
                    commandPlan.actionPlanName()
            );
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.ROUTING_STRATEGY,
                    commandPlan.routingPlan().routingStrategy().name()
            );
            exchange.getMessage().setBody(
                    executionCoordinator.execute(
                            exchange,
                            commandPlan
                    ).response()
            );
        });
    }
}
