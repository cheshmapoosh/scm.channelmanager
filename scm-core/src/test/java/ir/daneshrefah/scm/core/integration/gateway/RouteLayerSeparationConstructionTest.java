package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.core.integration.audit.ServiceAuditEventPublisher;
import ir.daneshrefah.scm.core.integration.error.GlobalErrorHandler;
import ir.daneshrefah.scm.core.integration.gateway.contract.ClientContractResolver;
import ir.daneshrefah.scm.core.integration.gateway.contract.RequestContractDecoder;
import ir.daneshrefah.scm.core.integration.observability.ScmExchangeMdc;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlanProvider;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRouteActivation;
import ir.daneshrefah.scm.core.integration.service.ServiceLayerRouteBuilder;
import ir.daneshrefah.scm.core.integration.service.ServiceRouteUriResolver;
import ir.daneshrefah.scm.core.integration.service.ServiceTargetRouter;
import ir.daneshrefah.scm.core.integration.service.guard.ChannelServiceAccessGuard;
import ir.daneshrefah.scm.core.integration.service.guard.IncomingChannelCodeResolver;
import ir.daneshrefah.scm.core.integration.service.guard.RuntimeChannelGuard;
import ir.daneshrefah.scm.core.integration.service.metrics.ServicePluginMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RouteLayerSeparationConstructionTest {

    @Test
    void gatewayAndServiceRouteBuildersCanBeConstructedSeparately() {
        GatewayLayerRouteBuilder gatewayLayerRouteBuilder = new GatewayLayerRouteBuilder(
                mock(ClientContractResolver.class),
                Map.<String, RequestContractDecoder>of(),
                mock(ServiceRouteUriResolver.class),
                mock(ScmExchangeMdc.class),
                mock(IncomingChannelCodeResolver.class));

        GatewayChannelRouteBuilder gatewayChannelRouteBuilder = new GatewayChannelRouteBuilder(
                mock(GatewayService.class),
                mock(RuntimeRoutePlanProvider.class),
                List.of(mock(ProtocolHandler.class)),
                gatewayLayerRouteBuilder,
                mock(RuntimeRouteActivation.class));

        ServiceLayerRouteBuilder serviceLayerRouteBuilder = new ServiceLayerRouteBuilder(
                mock(GatewayService.class),
                mock(RuntimeRoutePlanProvider.class),
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
                mock(RuntimeRouteActivation.class));

        assertThat(gatewayLayerRouteBuilder).isNotNull();
        assertThat(gatewayChannelRouteBuilder).isNotNull();
        assertThat(serviceLayerRouteBuilder).isNotNull();
        assertThat(gatewayLayerRouteBuilder).isNotInstanceOf(ServiceLayerRouteBuilder.class);
    }
}
