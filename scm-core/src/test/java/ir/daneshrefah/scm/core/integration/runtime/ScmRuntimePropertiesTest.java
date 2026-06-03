package ir.daneshrefah.scm.core.integration.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Map;

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

    @Test
    void resolvesConfiguredRuntimeTargets() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.targets.channel.enabled", "true")
                .withProperty("scm.runtime.targets.channel.gateway-names[0]", "channel.mb")
                .withProperty("scm.runtime.targets.service-domain.enabled", "true")
                .withProperty("scm.runtime.targets.service-domain.gateway-names[0]", "domain.card");

        ScmRuntimeProperties properties = new ScmRuntimeProperties(environment);

        assertEquals(List.of("channel.mb"), properties.gatewayNamesByTargetKind().get(RuntimeTargetKind.CHANNEL));
        assertEquals(List.of("domain.card"), properties.gatewayNamesByTargetKind().get(RuntimeTargetKind.SERVICE_DOMAIN));
    }

    @Test
    void legacyGatewayNameCreatesSingleRuntimeTarget() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.gateway-name", "domain.card");

        ScmRuntimeProperties properties = new ScmRuntimeProperties(environment);

        assertEquals(Map.of(RuntimeTargetKind.SERVICE_DOMAIN, List.of("domain.card")),
                properties.gatewayNamesByTargetKind());
    }

    @Test
    void enabledTargetWithoutGatewayNamesFailsClearly() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.targets.channel.enabled", "true");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new ScmRuntimeProperties(environment).runtimeTargets());

        assertEquals(
                "Runtime target CHANNEL is enabled but no gateway names are configured under scm.runtime.targets.channel.gateway-names.",
                exception.getMessage());
    }

    @Test
    void explicitDisabledRuntimeTargetsDoNotFallBackToLegacyGatewayName() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("scm.runtime.gateway-name", "channel.legacy")
                .withProperty("scm.runtime.targets.channel.enabled", "false")
                .withProperty("scm.runtime.targets.service-domain.enabled", "false");

        ScmRuntimeProperties properties = new ScmRuntimeProperties(environment);

        assertEquals(List.of(), properties.runtimeTargets());
        assertEquals(Map.of(), properties.gatewayNamesByTargetKind());
    }
}
