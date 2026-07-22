package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.core.integration.service.routing.ChainStepDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingEngineRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionResult;
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
    private final TaskWorkflowCommandResolver commandResolver;
    private final RoutingEngineRegistry engineRegistry;

    @Override
    public RoutingStrategy strategy() { return RoutingStrategy.TASK_WORKFLOW; }

    @Override
    public void buildTarget(ServiceTargetRouteContext context) {
        TaskWorkflowRoutePlan routePlan = routePlanFactory.create(
                context.service(), context.routeDefinitions());
        log.debug("Building TASK_WORKFLOW service target routeId={} serviceCode={} commandCount={}",
                context.route().getRouteId(), context.service().getCode(),
                routePlan.commandPlans().size());
        context.route().process(exchange -> {
            TaskWorkflowCommand command = commandResolver.resolve(exchange);
            TaskWorkflowCommandPlan commandPlan = routePlan.requireCommandPlan(command);
            exchange.setProperty(TaskWorkflowExchangeProperties.COMMAND, command);
            exchange.setProperty(TaskWorkflowExchangeProperties.COMMAND_PLAN, commandPlan);
            RoutingExecutionResult result = engineRegistry
                    .getRequired(commandPlan.routingPlan().routingStrategy())
                    .execute(exchange, commandPlan.routingPlan());
            exchange.getMessage().setBody(responseFor(commandPlan, result));
        });
    }

    private Object responseFor(
            TaskWorkflowCommandPlan commandPlan,
            RoutingExecutionResult result
    ) {
        if (result.decision() != ChainStepDecision.CONTINUE
                || commandPlan.routingPlan().routingStrategy() != RoutingStrategy.CHAIN_ON_APPROVE) {
            return result.response();
        }
        var stepResults = result.context().stepResults();
        for (int index = commandPlan.routingPlan().steps().size() - 1; index >= 0; index--) {
            var step = commandPlan.routingPlan().steps().get(index);
            if (TaskWorkflowRole.BUSINESS_OPERATION.name().equals(
                    step.observationContext().taskRole())
                    && stepResults.containsKey(step.serviceOperation().getOperationName())) {
                return stepResults.get(step.serviceOperation().getOperationName());
            }
        }
        return result.response();
    }
}
