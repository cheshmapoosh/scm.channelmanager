package ir.daneshrefah.scm.core.integration.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScmRuntimePropertiesTest {
    @Test
    void prefersRuntimeGatewayNameOverLegacyAppName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.app-name", "channel.legacy")
                .withProperty("scm.runtime.gateway-name", "domain.card");

        assertEquals("domain.card", new ScmRuntimeProperties(environment).gatewayName());
    }

    @Test
    void fallsBackToLegacyAppName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.app-name", "channel.mb");

        assertEquals("channel.mb", new ScmRuntimeProperties(environment).gatewayName());
    }

    @Test
    void usesBothRuntimeModeByDefault() {
        MockEnvironment environment = new MockEnvironment();

        assertEquals(RuntimeMode.CHANNEL_AND_SERVICE_DOMAIN, new ScmRuntimeProperties(environment).runtimeMode());
    }

    @Test
    void resolvesConfiguredRuntimeMode() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.mode", "service-domain");

        assertEquals(RuntimeMode.SERVICE_DOMAIN, new ScmRuntimeProperties(environment).runtimeMode());
    }

    @Test
    void rejectsUnknownRuntimeMode() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.mode", "operation");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ScmRuntimeProperties(environment).runtimeMode());

        assertEquals(
                "Invalid scm.runtime.mode 'operation'. Expected CHANNEL, SERVICE_DOMAIN, or CHANNEL_AND_SERVICE_DOMAIN.",
                exception.getMessage());
    }
}
