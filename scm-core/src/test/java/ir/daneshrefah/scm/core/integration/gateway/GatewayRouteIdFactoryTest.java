package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeTargetKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRouteIdFactoryTest {
    @Test
    void routeIdIncludesVersion() {
        assertEquals("gw.dm.domain-card.card-inquiry.v1",
                GatewayRouteIdFactory.singleRouteId(
                        RuntimeTargetKind.SERVICE_DOMAIN,
                        "domain.card",
                        "card-inquiry",
                        "v1"));
        assertEquals("gw.ch.channel-mb.card-inquiry.v2",
                GatewayRouteIdFactory.singleRouteId(
                        RuntimeTargetKind.CHANNEL,
                        "channel.mb",
                        "card-inquiry",
                        "v2"));
    }

    @Test
    void sameServiceWithDifferentVersionsCreatesDifferentRouteIds() {
        String v1RouteId = GatewayRouteIdFactory.singleRouteId(
                RuntimeTargetKind.SERVICE_DOMAIN,
                "domain.card",
                "card-inquiry",
                "v1");
        String v2RouteId = GatewayRouteIdFactory.singleRouteId(
                RuntimeTargetKind.SERVICE_DOMAIN,
                "domain.card",
                "card-inquiry",
                "v2");

        assertNotEquals(v1RouteId, v2RouteId);
    }

    @Test
    void duplicateInboundRouteIdKeepsVersionAndStableSuffix() {
        InboundChannelServiceDefinition definition = new InboundChannelServiceDefinition();
        definition.setMethod(HttpMethod.POST);
        definition.setPath("/v2/card/status");

        String routeId = GatewayRouteIdFactory.inboundRouteId(
                RuntimeTargetKind.SERVICE_DOMAIN,
                "domain.card",
                "card-inquiry",
                "v2",
                definition);

        assertTrue(routeId.startsWith("gw.dm.domain-card.card-inquiry.v2."));
    }
}
