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

class DefaultServiceRouteUriResolverTest {
    private final DefaultServiceRouteUriResolver resolver = new DefaultServiceRouteUriResolver();

    @Test
    void resolvesNormalizedServiceDirectUri() {
        Service service = new Service();
        service.setCode(" Card Inquiry ");

        assertEquals("direct:scm.service.card-inquiry", resolver.resolve(service));
    }

    @Test
    void resolvesTargetAwareServiceDirectUriAndRouteId() {
        RuntimeRoutePlan routePlan = routePlan("domain.card");
        RuntimeServicePlan servicePlan = servicePlan("Card Inquiry", 100L);

        assertEquals(
                "direct:scm.service.service-domain.domain-card.card-inquiry.100",
                resolver.resolve(routePlan, servicePlan));
        assertEquals(
                "scm-service-service-domain-domain-card-card-inquiry-100",
                resolver.routeId(routePlan, servicePlan));
    }

    @Test
    void sameServiceUnderTwoTargetsGetsDifferentRouteIds() {
        RuntimeServicePlan servicePlan = servicePlan("card-inquiry", 100L);

        assertEquals(
                "scm-service-channel-channel-mb-card-inquiry-100",
                resolver.routeId(routePlan(RuntimeTargetKind.CHANNEL, "channel.mb"), servicePlan));
        assertEquals(
                "scm-service-service-domain-domain-card-card-inquiry-100",
                resolver.routeId(routePlan(RuntimeTargetKind.SERVICE_DOMAIN, "domain.card"), servicePlan));
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
