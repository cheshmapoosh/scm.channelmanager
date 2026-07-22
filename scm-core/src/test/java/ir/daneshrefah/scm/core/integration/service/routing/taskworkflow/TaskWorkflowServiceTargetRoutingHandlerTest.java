package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.service.routing.*;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TaskWorkflowServiceTargetRoutingHandlerTest {

    @Test
    void taskFirstCommandUsesSharedFirstEngine() throws Exception {
        assertDelegates(TaskWorkflowCommand.COMPLETE_TASK, RoutingStrategy.FIRST);
    }

    @Test
    void taskChainCommandUsesSharedChainEngine() throws Exception {
        assertDelegates(
                TaskWorkflowCommand.APPROVE_AND_EXECUTE,
                RoutingStrategy.CHAIN_ON_APPROVE);
    }

    private void assertDelegates(
            TaskWorkflowCommand command,
            RoutingStrategy strategy
    ) throws Exception {
        Service service = new Service();
        service.setCode("my-paymaster");
        RoutingPlan routingPlan = strategy == RoutingStrategy.FIRST
                ? new RoutingPlan("task:first", strategy,
                List.of(step("complete-task", null, TaskWorkflowRole.COMPLETE_TASK, 0)))
                : new RoutingPlan("task:chain", strategy,
                List.of(
                        step("approve", policy(), TaskWorkflowRole.APPROVE_PROCESS, 0),
                        step("business", policy(), TaskWorkflowRole.BUSINESS_OPERATION, 1),
                        step("complete", policy(), TaskWorkflowRole.COMPLETE_PROCESS, 2)));
        TaskWorkflowCommandPlan commandPlan = new TaskWorkflowCommandPlan(
                command,
                command == TaskWorkflowCommand.COMPLETE_TASK
                        ? "task_complete" : "approve_and_execute",
                routingPlan,
                new InboundChannelServiceDefinition());
        TaskWorkflowRoutePlan taskPlan = new TaskWorkflowRoutePlan(
                service, Map.of(command, commandPlan));
        TaskWorkflowRoutePlanFactory planFactory = mock(TaskWorkflowRoutePlanFactory.class);
        when(planFactory.create(eq(service), anyList())).thenReturn(taskPlan);
        TaskWorkflowCommandResolver resolver = mock(TaskWorkflowCommandResolver.class);
        RoutingEngine engine = mock(RoutingEngine.class);
        when(engine.strategy()).thenReturn(strategy);
        RoutingEngineRegistry registry = new RoutingEngineRegistry(List.of(engine));
        TaskWorkflowServiceTargetRoutingHandler handler =
                new TaskWorkflowServiceTargetRoutingHandler(planFactory, resolver, registry);
        RouteDefinition route = mock(RouteDefinition.class);
        when(route.getRouteId()).thenReturn("service.my-paymaster");
        handler.buildTarget(new ServiceTargetRouteContext(route, service, List.of()));
        ArgumentCaptor<Processor> processor = ArgumentCaptor.forClass(Processor.class);
        verify(route).process(processor.capture());
        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getMessage()).thenReturn(message);
        when(resolver.resolve(exchange)).thenReturn(command);
        RoutingExecutionContext executionContext = new RoutingExecutionContext("task-request");
        if (strategy == RoutingStrategy.CHAIN_ON_APPROVE) {
            executionContext.record("business", "business-response");
            executionContext.record("complete", "completion-response");
        }
        when(engine.execute(exchange, routingPlan)).thenReturn(new RoutingExecutionResult(
                strategy == RoutingStrategy.CHAIN_ON_APPROVE
                        ? "completion-response" : "task-response",
                executionContext,
                ChainStepDecision.CONTINUE,
                null));

        processor.getValue().process(exchange);

        verify(engine).execute(exchange, routingPlan);
        verify(message).setBody(strategy == RoutingStrategy.CHAIN_ON_APPROVE
                ? "business-response" : "task-response");
        verify(exchange).setProperty(TaskWorkflowExchangeProperties.COMMAND, command);
        verify(exchange).setProperty(TaskWorkflowExchangeProperties.COMMAND_PLAN, commandPlan);
        assertEquals(RoutingStrategy.TASK_WORKFLOW, handler.strategy());
    }

    private RoutingStepPlan step(
            String name,
            ChainStepDecisionPolicy policy,
            TaskWorkflowRole role,
            int index
    ) {
        var operation = new ir.daneshrefah.scm.common.model.gateway.ServiceOperation();
        operation.setOperationName(name);
        operation.setActive(true);
        return new RoutingStepPlan(
                name,
                operation,
                "direct:op." + name,
                (exchange, context) -> context.originalRequest(),
                policy,
                new RoutingStepObservationContext(
                        "my-paymaster", "action", role.name(), index));
    }

    private ChainStepDecisionPolicy policy() {
        return new ChainStepDecisionPolicy() {
            @Override
            public String code() {
                return "TEST";
            }

            @Override
            public ChainStepDecision decide(ChainStepDecisionContext context) {
                return ChainStepDecision.CONTINUE;
            }
        };
    }
}
