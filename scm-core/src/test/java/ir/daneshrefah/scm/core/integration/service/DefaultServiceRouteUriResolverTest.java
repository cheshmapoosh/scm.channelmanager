package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefaultServiceRouteUriResolverTest {
    private final DefaultServiceRouteUriResolver resolver = new DefaultServiceRouteUriResolver();

    @Test
    void resolvesTargetAwareServiceDirectUriAndRouteId() {
        RuntimeRoutePlan routePlan = routePlan("domain.card");
        RuntimeServicePlan servicePlan = servicePlan("cardInquiry", 100L);

        assertEquals("svc.dm.card.cardinquiry", resolver.routeId(routePlan, servicePlan));
        assertEquals("direct:svc.dm.card.cardinquiry", resolver.resolve(routePlan, servicePlan));
    }

    @Test
    void sameServiceUnderTwoTargetsGetsDifferentRouteIds() {
        RuntimeServicePlan servicePlan = servicePlan("cardInquiry", 100L);

        assertEquals(
                "svc.ch.mb.cardinquiry",
                resolver.routeId(routePlan(RuntimeTargetKind.CHANNEL, "channel.mb"), servicePlan));
        assertEquals(
                "svc.dm.card.cardinquiry",
                resolver.routeId(routePlan(RuntimeTargetKind.SERVICE_DOMAIN, "domain.card"), servicePlan));
    }

    @Test
    void routeIdAndDirectEndpointKeyAreIdentical() {
        RuntimeRoutePlan routePlan = routePlan(RuntimeTargetKind.SERVICE_DOMAIN, "domain.card");
        RuntimeServicePlan servicePlan = servicePlan("cardInquiry", 100L);

        String routeId = resolver.routeId(routePlan, servicePlan);
        String uri = resolver.resolve(routePlan, servicePlan);

        assertEquals(routeId, uri.substring("direct:".length()));
        assertFalse(uri.startsWith("direct:" + "scm.service"));
    }

    @Test
    void routeIdsDoNotContainRepeatedLayerWordsOrGatewayPrefixes() {
        RuntimeServicePlan servicePlan = servicePlan("cardInquiry", 100L);

        String routeId = resolver.routeId(routePlan(RuntimeTargetKind.SERVICE_DOMAIN, "domain.card"), servicePlan);

        assertFalse(routeId.contains("service-service"));
        assertFalse(routeId.contains("domain-domain"));
        assertFalse(routeId.contains("domain" + "-card"));
        assertFalse(routeId.contains("channel" + "-mb"));
    }

    private RuntimeRoutePlan routePlan(String gatewayName) {
        return routePlan(RuntimeTargetKind.SERVICE_DOMAIN, gatewayName);
    }

    private RuntimeRoutePlan routePlan(RuntimeTargetKind targetKind, String gatewayName) {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setName(gatewayName);
        return new RuntimeRoutePlan(gatewayChannel, targetKind, List.of());
    }

    private RuntimeServicePlan servicePlan(String serviceCode, Long channelServiceAccessId) {
        Channel channel = new Channel();
        channel.setCode("mb");

        Service service = new Service();
        service.setCode(serviceCode);

        ChannelServiceAccess access = new ChannelServiceAccess();
        access.setId(channelServiceAccessId);
        access.setChannel(channel);
        access.setService(service);

        return new RuntimeServicePlan(null, access, service, List.of());
    }
}
