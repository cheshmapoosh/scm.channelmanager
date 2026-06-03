package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeRouteActivationTest {
    private final RuntimeRouteActivation activation = new RuntimeRouteActivation(
            new ScmRuntimeProperties(new MockEnvironment()),
            new DefaultRuntimeTargetKindResolver());

    @Test
    void channelModeBuildsChannelGatewayAndServiceRoutes() {
        assertTrue(activation.shouldBuildGatewayRoutes(RuntimeMode.CHANNEL, RuntimeTargetKind.CHANNEL));
        assertTrue(activation.shouldBuildServiceRoutes(RuntimeMode.CHANNEL, RuntimeTargetKind.CHANNEL));
    }

    @Test
    void channelModeSkipsServiceDomainRoutes() {
        assertFalse(activation.shouldBuildGatewayRoutes(RuntimeMode.CHANNEL, RuntimeTargetKind.SERVICE_DOMAIN));
        assertFalse(activation.shouldBuildServiceRoutes(RuntimeMode.CHANNEL, RuntimeTargetKind.SERVICE_DOMAIN));
    }

    @Test
    void serviceDomainModeSkipsChannelGatewayRoutesAndBuildsServiceRoutes() {
        assertFalse(activation.shouldBuildGatewayRoutes(RuntimeMode.SERVICE_DOMAIN, RuntimeTargetKind.SERVICE_DOMAIN));
        assertTrue(activation.shouldBuildServiceRoutes(RuntimeMode.SERVICE_DOMAIN, RuntimeTargetKind.SERVICE_DOMAIN));
    }

    @Test
    void bothModeBuildsGatewayAndServiceRoutesForBothTargetKinds() {
        assertTrue(activation.shouldBuildGatewayRoutes(RuntimeMode.CHANNEL_AND_SERVICE_DOMAIN, RuntimeTargetKind.CHANNEL));
        assertTrue(activation.shouldBuildServiceRoutes(RuntimeMode.CHANNEL_AND_SERVICE_DOMAIN, RuntimeTargetKind.CHANNEL));
        assertTrue(activation.shouldBuildGatewayRoutes(RuntimeMode.CHANNEL_AND_SERVICE_DOMAIN, RuntimeTargetKind.SERVICE_DOMAIN));
        assertTrue(activation.shouldBuildServiceRoutes(RuntimeMode.CHANNEL_AND_SERVICE_DOMAIN, RuntimeTargetKind.SERVICE_DOMAIN));
    }

    @Test
    void resolvesTargetKindFromGatewayName() {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setName("domain.card");

        assertTrue(activation.shouldBuildServiceRoutes(
                RuntimeMode.SERVICE_DOMAIN,
                activation.resolveTargetKind(gatewayChannel)));
    }
}
