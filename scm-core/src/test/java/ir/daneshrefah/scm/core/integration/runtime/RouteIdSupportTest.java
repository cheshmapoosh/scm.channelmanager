package ir.daneshrefah.scm.core.integration.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteIdSupportTest {
    @Test
    void normalizesGatewayScopeNamesWithoutTargetPrefixes() {
        assertEquals("card", RouteIdSupport.normalizeGatewayScopeName("domain.card"));
        assertEquals("mb", RouteIdSupport.normalizeGatewayScopeName("channel.mb"));
        assertEquals("card-inquiry", RouteIdSupport.normalizeGatewayScopeName("domain.card.inquiry"));
        assertEquals("mobile-bank", RouteIdSupport.normalizeGatewayScopeName("channel.mobile.bank"));
    }

    @Test
    void targetKindsUseCompactMarkers() {
        assertEquals("dm", RouteIdSupport.targetKindShort(RuntimeTargetKind.SERVICE_DOMAIN));
        assertEquals("ch", RouteIdSupport.targetKindShort(RuntimeTargetKind.CHANNEL));
    }

    @Test
    void serviceGatewayAndOperationRouteIdsUseCanonicalNames() {
        assertEquals(
                "svc.dm.card.cardinquiry",
                RouteIdSupport.serviceRouteId(RuntimeTargetKind.SERVICE_DOMAIN, "domain.card", "cardInquiry"));
        assertEquals(
                "gw.dm.card.cardinquiry.v1",
                RouteIdSupport.gatewayRouteId(RuntimeTargetKind.SERVICE_DOMAIN, "domain.card", "cardInquiry", "v1"));
        assertEquals(
                "op.SVC_CARD_INQUIRY_TCP",
                RouteIdSupport.operationRouteId("SVC_CARD_INQUIRY_TCP"));
    }
}
