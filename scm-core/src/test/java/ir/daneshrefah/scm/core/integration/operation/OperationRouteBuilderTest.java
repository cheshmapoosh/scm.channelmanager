package ir.daneshrefah.scm.core.integration.operation;

import ir.daneshrefah.scm.common.constant.Routes;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.operation.handler.OperationTypeHandler;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationRouteBuilderTest {
    @Test
    void serviceLayerInvocationReceivesScmFaultWithoutGatewayErrorRoute() throws Exception {
        OperationService operationService = operationService();
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

        try (DefaultCamelContext context = context(operationService, pluginResolverService, globalErrorHandler)) {
            ProducerTemplate template = context.createProducerTemplate();

            Exchange exchange = template.request("direct:CARD_INQUIRY", request ->
                    request.setProperty(Message.SERVICE_LAYER_INVOCATION, true));

            assertInstanceOf(ScmFault.class, exchange.getMessage().getBody());
            verify(globalErrorHandler).handle(any(Exchange.class));
        }
    }

    @Test
    void legacyInvocationStillRoutesToGatewayErrorRoute() throws Exception {
        OperationService operationService = operationService();
        PluginResolverService pluginResolverService = mock(PluginResolverService.class);
        GlobalErrorHandler globalErrorHandler = mock(GlobalErrorHandler.class);

        try (DefaultCamelContext context = context(operationService, pluginResolverService, globalErrorHandler)) {
            ProducerTemplate template = context.createProducerTemplate();

            Exchange exchange = template.request("direct:CARD_INQUIRY", request -> {
            });

            assertEquals("global-error", exchange.getMessage().getBody());
            verify(globalErrorHandler, never()).handle(any(Exchange.class));
        }
    }

    private DefaultCamelContext context(OperationService operationService,
                                        PluginResolverService pluginResolverService,
                                        GlobalErrorHandler globalErrorHandler) throws Exception {
        DefaultCamelContext context = new DefaultCamelContext();
        context.addRoutes(new OperationRouteBuilder(
                operationService,
                pluginResolverService,
                Map.<String, PluginHandler>of(),
                List.of(throwingHandler()),
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

    private OperationService operationService() {
        Operation operation = new Operation();
        operation.setName("CARD_INQUIRY");
        operation.setActive(true);
        operation.setType(OperationType.BEAN);
        OperationService operationService = mock(OperationService.class);
        when(operationService.getAllOperations()).thenReturn(List.of(operation));
        return operationService;
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
}
