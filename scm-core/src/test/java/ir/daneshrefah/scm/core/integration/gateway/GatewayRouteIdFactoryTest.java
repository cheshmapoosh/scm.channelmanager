package ir.daneshrefah.scm.core.integration.gateway;

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
    void routeGroupIdKeepsVersionAndOperationHash() {
        String routeId = GatewayRouteIdFactory.groupRouteId("card-inquiry", "v2", "CARD_INQUIRY");

        assertTrue(routeId.startsWith("card-inquiry-v2-route-"));
    }
}
