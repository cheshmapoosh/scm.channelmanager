package ir.daneshrefah.scm.core.integration.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
