package ir.daneshrefah.scm.core.integration.runtime;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultRuntimeTargetKindResolverTest {
    private final DefaultRuntimeTargetKindResolver resolver = new DefaultRuntimeTargetKindResolver();

    @Test
    void resolvesChannelRuntimeName() {
        GatewayChannel gatewayChannel = gateway("channel.mb");

        assertEquals(RuntimeTargetKind.CHANNEL, resolver.resolve(gatewayChannel));
    }

    @Test
    void resolvesDomainRuntimeName() {
        GatewayChannel gatewayChannel = gateway("domain.card");

        assertEquals(RuntimeTargetKind.SERVICE_DOMAIN, resolver.resolve(gatewayChannel));
    }

    @Test
    void rejectsInvalidRuntimeName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> resolver.resolve(gateway("mb")));

        assertEquals("Invalid GatewayChannel.name 'mb'. Expected channel.<code> or domain.<code>.", exception.getMessage());
    }

    private GatewayChannel gateway(String name) {
        GatewayChannel gatewayChannel = new GatewayChannel();
        gatewayChannel.setName(name);
        return gatewayChannel;
    }
}
