package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.operation.handler.OperationTypeHandler;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeMode;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetProperties;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationLayerRouteBuilderTest {
    @Test
    void serviceLayerInvocationReceivesScmFaultWithoutGatewayErrorRoute() throws Exception {
        OperationService operationService = operationService(operation("CARD_INQUIRY"));
        PluginResolverService pluginResolverService = mock(PluginResolverService.class);
        GlobalErrorHandler globalErrorHandler = mock(GlobalErrorHandler.class);
        doAnswer(invocation -> {
            Exchange exchange = invocation.getArgument(0);
            exchange.getMessage().setBody(ScmFault.builder()
                    .errors(List.of(new ir.daneshrefah.scm.common.model.error.Error(
                            "operation",
                            500,
                            "operation failed")))
                    .build());
            return null;
        }).when(globalErrorHandler).handle(any(Exchange.class));

        try (DefaultCamelContext context = context(
                operationService,
                pluginResolverService,
                globalErrorHandler,
                runtimePlan(service("card-inquiry", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true))),
                throwingHandler())) {
            ProducerTemplate template = context.createProducerTemplate();

            Exchange exchange = template.request("direct:op.CARD_INQUIRY", request ->
                    request.setProperty(Message.SERVICE_LAYER_INVOCATION, true));

            assertInstanceOf(ScmFault.class, exchange.getMessage().getBody());
            verify(globalErrorHandler).handle(any(Exchange.class));
        }
    }

    @Test
    void legacyInvocationStillRoutesToGatewayErrorRoute() throws Exception {
        OperationService operationService = operationService(operation("CARD_INQUIRY"));
        PluginResolverService pluginResolverService = mock(PluginResolverService.class);
        GlobalErrorHandler globalErrorHandler = mock(GlobalErrorHandler.class);

        try (DefaultCamelContext context = context(
                operationService,
                pluginResolverService,
                globalErrorHandler,
                runtimePlan(service("card-inquiry", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true))),
                throwingHandler())) {
            ProducerTemplate template = context.createProducerTemplate();

            Exchange exchange = template.request("direct:op.CARD_INQUIRY", request -> {
            });

            assertEquals("global-error", exchange.getMessage().getBody());
            verify(globalErrorHandler, never()).handle(any(Exchange.class));
        }
    }

    @Test
    void buildsOnlyOperationsRequiredByRuntimeServicePlans() throws Exception {
        OperationService operationService = operationService(
                operation("CARD_INQUIRY"),
                operation("CARD_STATUS"),
                operation("UNRELATED_ACTIVE"));

        try (DefaultCamelContext context = context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                runtimePlan(
                        service("card-inquiry", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true)),
                        service("card-status", RoutingStrategy.FIRST, serviceOperation("CARD_STATUS", true))),
                noOpHandler())) {
            List<String> routeIds = routeIds(context);

            assertTrue(routeIds.contains("op.CARD_INQUIRY"));
            assertTrue(routeIds.contains("op.CARD_STATUS"));
            assertFalse(routeIds.contains("op.UNRELATED_ACTIVE"));
            assertFalse(routeIds.stream().anyMatch(routeId -> routeId.startsWith("route-")));
            assertRequiredOperationLookup(operationService, "CARD_INQUIRY", "CARD_STATUS");
            verify(operationService, never()).getAllOperations();
        }
    }

    @Test
    void duplicateOperationNamesAcrossServicesCreateOneRoute() throws Exception {
        OperationService operationService = operationService(operation("CARD_INQUIRY"));

        try (DefaultCamelContext context = context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                runtimePlan(
                        service("card-inquiry-a", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true)),
                        service("card-inquiry-b", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true))),
                noOpHandler())) {
            long count = routeIds(context).stream()
                    .filter("op.CARD_INQUIRY"::equals)
                    .count();

            assertEquals(1, count);
            assertRequiredOperationLookup(operationService, "CARD_INQUIRY");
        }
    }

    @Test
    void emptyRuntimeRoutePlanCreatesNoOperationRoutes() throws Exception {
        OperationService operationService = operationService(operation("CARD_INQUIRY"));

        try (DefaultCamelContext context = context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                new RuntimeRoutePlan(gateway(), RuntimeTargetKind.SERVICE_DOMAIN, List.of()),
                noOpHandler())) {
            assertTrue(routeIds(context).stream().noneMatch(routeId -> routeId.startsWith("op.")));
            verify(operationService, never()).findActiveOperationsByNames(any());
            verify(operationService, never()).getAllOperations();
        }
    }

    @Test
    void operationRoutePreservesDirectFromUriAndUsesCompactRouteId() throws Exception {
        OperationService operationService = operationService(operation("CARD_INQUIRY"));

        try (DefaultCamelContext context = context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                runtimePlan(service("card-inquiry", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true))),
                noOpHandler())) {
            RouteDefinition route = context.getRouteDefinition("op.CARD_INQUIRY");

            assertEquals("op.CARD_INQUIRY", route.getRouteId());
            assertEquals("direct:op.CARD_INQUIRY", route.getInput().getEndpointUri());
            assertEquals(route.getRouteId(), route.getInput().getEndpointUri().substring("direct:".length()));
        }
    }

    @Test
    void failOverAndMultiOperationPlansIncludeAllActiveServiceOperations() throws Exception {
        OperationService operationService = operationService(
                operation("CARD_INQUIRY"),
                operation("CARD_STATUS"),
                operation("CARD_LIMIT"));

        try (DefaultCamelContext context = context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                runtimePlan(
                        service("card-failover", RoutingStrategy.FAIL_OVER,
                                serviceOperation("CARD_INQUIRY", true),
                                serviceOperation("CARD_STATUS", true)),
                        service("card-multi", RoutingStrategy.MULTI_OPERATION,
                                serviceOperation("CARD_LIMIT", true),
                                serviceOperation("INACTIVE_IGNORED", false))),
                noOpHandler())) {
            List<String> routeIds = routeIds(context);

            assertTrue(routeIds.contains("op.CARD_INQUIRY"));
            assertTrue(routeIds.contains("op.CARD_STATUS"));
            assertTrue(routeIds.contains("op.CARD_LIMIT"));
            assertFalse(routeIds.contains("op.INACTIVE_IGNORED"));
            assertRequiredOperationLookup(operationService, "CARD_INQUIRY", "CARD_STATUS", "CARD_LIMIT");
        }
    }

    @Test
    void missingRequiredOperationIsNotRouted() throws Exception {
        OperationService operationService = operationService();

        try (DefaultCamelContext context = context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                runtimePlan(service("card-inquiry", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true))),
                noOpHandler())) {
            assertFalse(routeIds(context).contains("op.CARD_INQUIRY"));
            assertRequiredOperationLookup(operationService, "CARD_INQUIRY");
            verify(operationService, never()).getAllOperations();
        }
    }

    @Test
    void duplicateActiveOperationNamesFailFast() {
        OperationService operationService = mock(OperationService.class);
        when(operationService.findActiveOperationsByNames(any()))
                .thenReturn(List.of(operation("CARD_INQUIRY"), operation("CARD_INQUIRY")));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> context(
                operationService,
                mock(PluginResolverService.class),
                mock(GlobalErrorHandler.class),
                runtimePlan(service("card-inquiry", RoutingStrategy.FIRST, serviceOperation("CARD_INQUIRY", true))),
                noOpHandler()));

        assertTrue(exception.getMessage().contains("Duplicate active operation"));
        verify(operationService, never()).getAllOperations();
    }

    @SuppressWarnings("unchecked")
    private void assertRequiredOperationLookup(OperationService operationService, String... operationNames) {
        ArgumentCaptor<Collection<String>> namesCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(operationService).findActiveOperationsByNames(namesCaptor.capture());
        assertEquals(Set.of(operationNames), Set.copyOf(namesCaptor.getValue()));
    }

    private DefaultCamelContext context(OperationService operationService,
                                        PluginResolverService pluginResolverService,
                                        GlobalErrorHandler globalErrorHandler,
                                        RuntimeRoutePlan routePlan,
                                        OperationTypeHandler operationTypeHandler) throws Exception {
        DefaultCamelContext context = new DefaultCamelContext();
        RuntimeRouteActivation runtimeRouteActivation = runtimeRouteActivation();
        GatewayService gatewayService = gatewayService(routePlan.gatewayChannel());
        RuntimeRoutePlanProvider runtimeRoutePlanProvider = mock(RuntimeRoutePlanProvider.class);
        when(runtimeRoutePlanProvider.provide(routePlan.gatewayChannel())).thenReturn(routePlan);
        context.addRoutes(new OperationLayerRouteBuilder(
                runtimeRouteActivation,
                gatewayService,
                runtimeRoutePlanProvider,
                operationService,
                pluginResolverService,
                Map.<String, PluginHandler>of(),
                List.of(operationTypeHandler),
                globalErrorHandler));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(Routes.GLOBAL_ERROR_HANDLER).setBody(constant("global-error"));
            }
        });
        context.start();
        return context;
    }

    private RuntimeRouteActivation runtimeRouteActivation() {
        RuntimeRouteActivation runtimeRouteActivation = mock(RuntimeRouteActivation.class);
        when(runtimeRouteActivation.runtimeMode()).thenReturn(RuntimeMode.SERVICE_DOMAIN);
        when(runtimeRouteActivation.runtimeTargets()).thenReturn(List.of(new RuntimeTargetProperties(
                RuntimeTargetKind.SERVICE_DOMAIN,
                true,
                List.of("domain.card"))));
        when(runtimeRouteActivation.resolveTargetKind(any(GatewayChannel.class))).thenReturn(RuntimeTargetKind.SERVICE_DOMAIN);
        when(runtimeRouteActivation.shouldBuildServiceRoutes(RuntimeMode.SERVICE_DOMAIN, RuntimeTargetKind.SERVICE_DOMAIN)).thenReturn(true);
        return runtimeRouteActivation;
    }

    private GatewayService gatewayService(GatewayChannel gatewayChannel) {
        GatewayService gatewayService = mock(GatewayService.class);
        when(gatewayService.findGatewayChannelByName("domain.card")).thenReturn(gatewayChannel);
        return gatewayService;
    }

    private OperationService operationService(Operation... operations) {
        OperationService operationService = mock(OperationService.class);
        List<Operation> operationCatalog = List.of(operations);
        when(operationService.findActiveOperationsByNames(any())).thenAnswer(invocation -> {
            Collection<String> operationNames = invocation.getArgument(0);
            Set<String> requiredNames = operationNames == null
                    ? Set.of()
                    : new LinkedHashSet<>(operationNames);
            return operationCatalog.stream()
                    .filter(operation -> Boolean.TRUE.equals(operation.getActive()))
                    .filter(operation -> requiredNames.contains(operation.getName()))
                    .toList();
        });
        return operationService;
    }

    private RuntimeRoutePlan runtimePlan(Service... services) {
        GatewayChannel gatewayChannel = gateway();
        List<RuntimeServicePlan> servicePlans = java.util.Arrays.stream(services)
                .map(service -> new RuntimeServicePlan(gatewayChannel, null, service, List.of()))
                .toList();
        return new RuntimeRoutePlan(gatewayChannel, RuntimeTargetKind.SERVICE_DOMAIN, servicePlans);
    }

    private GatewayChannel gateway() {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setName("domain.card");
        gatewayChannel.setActive(true);
        return gatewayChannel;
    }

    private Service service(String code, RoutingStrategy routingStrategy, ServiceOperation... serviceOperations) {
        Service service = new Service();
        service.setCode(code);
        service.setPublish(true);
        service.setRoutingStrategy(routingStrategy);
        service.setServiceOperations(List.of(serviceOperations));
        return service;
    }

    private ServiceOperation serviceOperation(String operationName, boolean active) {
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setOperationName(operationName);
        serviceOperation.setActive(active);
        return serviceOperation;
    }

    private Operation operation(String operationName) {
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.setActive(true);
        operation.setType(OperationType.BEAN);
        operation.setPath("ignored");
        return operation;
    }

    private List<String> routeIds(DefaultCamelContext context) {
        return context.getRouteDefinitions()
                .stream()
                .map(RouteDefinition::getRouteId)
                .toList();
    }

    private OperationTypeHandler throwingHandler() {
        return new OperationTypeHandler() {
            @Override
            public OperationType getOperationType() {
                return OperationType.BEAN;
            }

            @Override
            public void internalConfig(RouteDefinition route, Operation operation) {
                route.process(exchange -> {
                    throw new IllegalStateException("boom");
                });
            }

            @Override
            public void config(RouteDefinition route, Operation operation) {
            }
        };
    }

    private OperationTypeHandler noOpHandler() {
        return new OperationTypeHandler() {
            @Override
            public OperationType getOperationType() {
                return OperationType.BEAN;
            }

            @Override
            public void config(RouteDefinition route, Operation operation) {
                route.process(exchange -> exchange.getMessage().setBody(operation.getName()));
            }
        };
    }
}
