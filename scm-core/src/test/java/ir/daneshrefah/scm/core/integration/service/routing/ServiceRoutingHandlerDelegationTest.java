package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ServiceRoutingHandlerDelegationTest {

    @Test
    void firstServiceAdapterDelegatesToSharedFirstEngine() throws Exception {
        Service service = service("ordinary-first");
        RoutingPlan plan = new RoutingPlan(
                service.getCode(), RoutingStrategy.FIRST,
                List.of(FirstRoutingEngineTest.step("only-operation", null)));
        FirstRoutePlanFactory planFactory = mock(FirstRoutePlanFactory.class);
        when(planFactory.create(service)).thenReturn(plan);
        EngineFixture engine = engineFor(plan);
        assertDelegates(
                new FirstServiceTargetRoutingHandler(planFactory, engine.registry()),
                service,
                plan,
                RoutingStrategy.FIRST,
                engine.engine());
    }

    @Test
    void chainServiceAdapterDelegatesToSharedChainEngine() throws Exception {
        Service service = service("ordinary-chain");
        RoutingPlan plan = new RoutingPlan(
                service.getCode(), RoutingStrategy.CHAIN_ON_APPROVE,
                List.of(
                        FirstRoutingEngineTest.step(
                                "first", FirstRoutingEngineTest.policy(ChainStepDecision.CONTINUE)),
                        FirstRoutingEngineTest.step(
                                "second", FirstRoutingEngineTest.policy(ChainStepDecision.CONTINUE))));
        ChainOnApproveRoutePlanFactory planFactory = mock(ChainOnApproveRoutePlanFactory.class);
        when(planFactory.create(service)).thenReturn(plan);
        EngineFixture engine = engineFor(plan);
        assertDelegates(
                new ChainOnApproveServiceTargetRoutingHandler(planFactory, engine.registry()),
                service,
                plan,
                RoutingStrategy.CHAIN_ON_APPROVE,
                engine.engine());
    }

    private EngineFixture engineFor(RoutingPlan plan) {
        RoutingEngine engine = mock(RoutingEngine.class);
        when(engine.strategy()).thenReturn(plan.routingStrategy());
        when(engine.execute(any(), same(plan))).thenAnswer(invocation ->
                new RoutingExecutionResult(
                        "engine-response",
                        new RoutingExecutionContext("request"),
                        ChainStepDecision.CONTINUE,
                        null));
        return new EngineFixture(engine, new RoutingEngineRegistry(List.of(engine)));
    }

    private void assertDelegates(
            ServiceTargetRoutingHandler handler,
            Service service,
            RoutingPlan plan,
            RoutingStrategy strategy,
            RoutingEngine engine
    ) throws Exception {
        RouteDefinition route = mock(RouteDefinition.class);
        when(route.getRouteId()).thenReturn("route-" + service.getCode());
        handler.buildTarget(new ServiceTargetRouteContext(route, service));
        ArgumentCaptor<Processor> processorCaptor = ArgumentCaptor.forClass(Processor.class);
        verify(route).process(processorCaptor.capture());

        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getMessage()).thenReturn(message);
        processorCaptor.getValue().process(exchange);

        verify(engine).execute(exchange, plan);
        verify(message).setBody("engine-response");
        assertEquals(strategy, handler.strategy());
    }

    private Service service(String code) {
        Service service = new Service();
        service.setCode(code);
        return service;
    }

    private record EngineFixture(RoutingEngine engine, RoutingEngineRegistry registry) {
    }
}
