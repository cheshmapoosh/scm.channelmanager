package ir.daneshrefah.scm.core.integration.gateway;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRouteIdFactoryTest {
    @Test
    void routeIdIncludesVersion() {
        assertEquals("card-inquiry-v1-route", GatewayRouteIdFactory.singleRouteId("card-inquiry", "v1"));
        assertEquals("card-inquiry-v2-route", GatewayRouteIdFactory.singleRouteId("card-inquiry", "v2"));
    }

    @Test
    void sameServiceWithDifferentVersionsCreatesDifferentRouteIds() {
        String v1RouteId = GatewayRouteIdFactory.singleRouteId("card-inquiry", "v1");
        String v2RouteId = GatewayRouteIdFactory.singleRouteId("card-inquiry", "v2");

        assertNotEquals(v1RouteId, v2RouteId);
    }

    @Test
    void duplicateInboundRouteIdKeepsVersionAndStableSuffix() {
        InboundChannelServiceDefinition definition = new InboundChannelServiceDefinition();
        definition.setMethod(HttpMethod.POST);
        definition.setPath("/v2/card/status");

        String routeId = GatewayRouteIdFactory.inboundRouteId("card-inquiry", "v2", definition);

        assertTrue(routeId.startsWith("card-inquiry-v2-route-"));
    }
}
