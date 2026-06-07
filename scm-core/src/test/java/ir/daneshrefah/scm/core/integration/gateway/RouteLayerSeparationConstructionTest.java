package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.audit.ServiceAuditEventPublisher;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.RequestContractDecoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.operation.OperationLayerRouteBuilder;
import ir.daneshrefah.scm.core.integration.operation.handler.OperationTypeHandler;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.service.ServiceLayerRouteBuilder;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import ir.daneshrefah.scm.core.integration.service.ServiceTargetRouter;
import ir.daneshrefah.scm.core.integration.service.guard.ChannelServiceAccessGuard;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import ir.daneshrefah.scm.core.integration.service.guard.RuntimeChannelGuard;
import ir.daneshrefah.scm.core.integration.service.metrics.ServicePluginMetrics;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RouteLayerSeparationConstructionTest {

    @Test
    void scmLayerRouteBuildersCanBeConstructedSeparately() {
        GatewayRoutePipelineConfigurer gatewayRoutePipelineConfigurer = new GatewayRoutePipelineConfigurer(
                mock(ClientContractResolver.class),
                Map.<String, RequestContractDecoder>of(),
                mock(ServiceRouteUriResolver.class),
                mock(ScmExchangeMdc.class),
                mock(IncomingChannelCodeResolver.class));

        RuntimeRouteActivation runtimeRouteActivation = mock(RuntimeRouteActivation.class);
        RuntimeRoutePlanProvider runtimeRoutePlanProvider = mock(RuntimeRoutePlanProvider.class);
        GatewayService gatewayService = mock(GatewayService.class);

        GatewayChannelLayerRouteBuilder gatewayChannelLayerRouteBuilder = new GatewayChannelLayerRouteBuilder(
                gatewayService,
                runtimeRoutePlanProvider,
                List.of(mock(ProtocolHandler.class)),
                gatewayRoutePipelineConfigurer,
                runtimeRouteActivation);

        ServiceLayerRouteBuilder serviceLayerRouteBuilder = new ServiceLayerRouteBuilder(
                gatewayService,
                runtimeRoutePlanProvider,
                mock(ServiceRouteUriResolver.class),
                mock(RuntimeChannelGuard.class),
                mock(ChannelServiceAccessGuard.class),
                mock(PluginResolverService.class),
                Map.<String, PluginHandler>of(),
                mock(ServiceTargetRouter.class),
                mock(GlobalErrorHandler.class),
                mock(ScmExchangeMdc.class),
                mock(ServicePluginMetrics.class),
                mock(ServiceAuditEventPublisher.class),
                mock(IncomingChannelCodeResolver.class),
                runtimeRouteActivation);

        OperationLayerRouteBuilder operationLayerRouteBuilder = new OperationLayerRouteBuilder(
                runtimeRouteActivation,
                gatewayService,
                runtimeRoutePlanProvider,
                mock(OperationService.class),
                mock(PluginResolverService.class),
                Map.<String, PluginHandler>of(),
                List.of(mock(OperationTypeHandler.class)),
                mock(GlobalErrorHandler.class));

        assertThat(gatewayChannelLayerRouteBuilder).isInstanceOf(RouteBuilder.class);
        assertThat(serviceLayerRouteBuilder).isInstanceOf(RouteBuilder.class);
        assertThat(operationLayerRouteBuilder).isInstanceOf(RouteBuilder.class);
        assertThat(gatewayRoutePipelineConfigurer).isNotInstanceOf(RouteBuilder.class);
    }

    @Test
    void oldRouteBuilderClassNamesAreNotPresent() {
        assertThat(classExists("ir.daneshrefah.scm.core.integration.gateway.GatewayChannel" + "RouteBuilder")).isFalse();
        assertThat(classExists("ir.daneshrefah.scm.core.integration.gateway.GatewayLayer" + "RouteBuilder")).isFalse();
        assertThat(classExists("ir.daneshrefah.scm.core.integration.operation.Operation" + "RouteBuilder")).isFalse();
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
