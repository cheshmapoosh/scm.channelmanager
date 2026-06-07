package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.utils.RouteUtils;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDefinition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ServiceTargetRouterTest {
    private final ServiceTargetRouter router = new ServiceTargetRouter();

    @Test
    void firstDispatchesToOperationRouteId() throws Exception {
        RouteDefinition route = routeFor(
                service(RoutingStrategy.FIRST, operation("SVC_CARD_INQUIRY_TCP", true)),
                "svc.dm.card.cardinquiry");

        assertEquals(List.of("direct:op.SVC_CARD_INQUIRY_TCP"), toUris(route));
        assertFalse(toUris(route).contains(rawDirect("SVC_CARD_INQUIRY_TCP")));
    }

    @Test
    void failOverDispatchesToEachActiveOperationRouteId() throws Exception {
        RouteDefinition route = routeFor(
                service(RoutingStrategy.FAIL_OVER,
                        operation("SVC_CARD_INQUIRY_TCP", true),
                        operation("SVC_CARD_STATUS_TCP", true),
                        operation("SVC_CARD_INACTIVE_TCP", false)),
                "svc.dm.card.cardinquiry");

        assertEquals(
                List.of("direct:op.SVC_CARD_INQUIRY_TCP", "direct:op.SVC_CARD_STATUS_TCP"),
                toUris(route));
        assertFalse(toUris(route).contains(rawDirect("SVC_CARD_INQUIRY_TCP")));
        assertFalse(toUris(route).contains("direct:op.SVC_CARD_INACTIVE_TCP"));
    }

    @Test
    void multiOperationDispatchesToOperationRouteId() throws Exception {
        String operationName = "SVC_CARD_STATUS_TCP";
        String routeId = "svc.dm.card.cardinquiry-" + RouteUtils.getInstance().generateRouteUniqId(operationName);
        RouteDefinition route = routeFor(
                service(RoutingStrategy.MULTI_OPERATION,
                        operation("SVC_CARD_INQUIRY_TCP", true),
                        operation(operationName, true)),
                routeId);

        assertEquals(List.of("direct:op.SVC_CARD_STATUS_TCP"), toUris(route));
        assertFalse(toUris(route).contains(rawDirect("SVC_CARD_STATUS_TCP")));
    }

    private RouteDefinition routeFor(Service service, String routeId) throws Exception {
        AtomicReference<RouteDefinition> routeDefinition = new AtomicReference<>();
        try (DefaultCamelContext context = new DefaultCamelContext()) {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    RouteDefinition route = from("direct:service").routeId(routeId);
                    router.buildTarget(route, service);
                    routeDefinition.set(route);
                }
            });
        }
        return routeDefinition.get();
    }

    private List<String> toUris(RouteDefinition route) {
        List<String> uris = new ArrayList<>();
        collectToUris(route.getOutputs(), uris);
        return uris;
    }

    private String rawDirect(String operationName) {
        return "direct".concat(":").concat(operationName);
    }

    private void collectToUris(List<ProcessorDefinition<?>> outputs, List<String> uris) {
        for (ProcessorDefinition<?> output : outputs) {
            if (output instanceof ToDefinition toDefinition) {
                uris.add(toDefinition.getUri());
            }
            collectToUris(output.getOutputs(), uris);
        }
    }

    private Service service(RoutingStrategy routingStrategy, ServiceOperation... operations) {
        Service service = new Service();
        service.setCode("cardInquiry");
        service.setRoutingStrategy(routingStrategy);
        service.setServiceOperations(List.of(operations));
        return service;
    }

    private ServiceOperation operation(String operationName, boolean active) {
        ServiceOperation operation = new ServiceOperation();
        operation.setOperationName(operationName);
        operation.setActive(active);
        return operation;
    }
}
