package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRouteContext;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRoutingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskWorkflowServiceTargetRoutingHandler implements ServiceTargetRoutingHandler {
    private final TaskWorkflowRoutePlanFactory routePlanFactory;
    private final TaskWorkflowExecutionCoordinator executionCoordinator;

    @Override
    public RoutingStrategy strategy() { return RoutingStrategy.TASK_WORKFLOW; }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        executionCoordinator.verifyDurableStoreAvailable();
        TaskWorkflowRoutePlan routePlan = routePlanFactory.create(
                context.service());
        log.debug("Building TASK_WORKFLOW service target routeId={} serviceCode={} commandCount={}",
                context.route().getRouteId(), context.service().getCode(),
                routePlan.commandPlans().size());
        context.route().process(exchange -> {
            String inboundAction = exchange.getProperty(
                    Message.INBOUND_ROUTE_ACTION, String.class);
            TaskWorkflowCommandPlan commandPlan = routePlan.requireCommandPlan(
                    inboundAction
            );
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.COMMAND,
                    commandPlan.command()
            );
            exchange.setProperty(TaskWorkflowExchangeProperties.COMMAND_PLAN, commandPlan);
            exchange.getMessage().setBody(
                    executionCoordinator.execute(exchange, commandPlan).response()
            );
        });
    }
}
