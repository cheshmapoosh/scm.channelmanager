package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NabConfigResolverTest {


    @Test
    void resolvesUnifiedNabProviderWithoutDefaults() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider core = new ProviderRegistryProperties.Provider();
        core.setType("nab");
        core.setProtocol("ATPS");
        core.setEndpoint("127.0.0.1:9999");
        core.setUserId("999998");
        core.setPassword("secret");
        core.getRqUid().setLength(16);
        ProviderRegistryProperties.Field protocol = new ProviderRegistryProperties.Field();
        protocol.setName("protocol");
        protocol.setLength(4);
        protocol.setRequired(true);
        ProviderRegistryProperties.Field command = new ProviderRegistryProperties.Field();
        command.setName("command");
        command.setLength(2);
        command.setRequired(true);
        core.setHeaderFields(List.of(protocol, command));
        registry.getProviders().put("nab-atps", core);

        NabResolvedConfig config = new NabConfigResolver(registry, new NabProperties()).resolve("nab-atps", null);

        assertEquals("nab-atps", config.provider());
        assertEquals("nab", config.providerType());
        assertEquals("ATPS", config.protocol());
        assertEquals(2, config.headerFieldsByProtocol().get("ATPS").size());
    }

    @Test
    void unifiedNabProviderRequiresInstanceHeaderFields() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider core = new ProviderRegistryProperties.Provider();
        core.setType("nab");
        core.setProtocol("ATPS");
        core.setEndpoint("127.0.0.1:9999");
        core.setUserId("999998");
        core.setPassword("secret");
        registry.getProviders().put("nab-atps", core);

        assertThrows(IllegalArgumentException.class,
                () -> new NabConfigResolver(registry, new NabProperties()).resolve("nab-atps", null));
    }

    @Test
    void resolvesTypedProviderAndMergesDefaults() {
        NabProperties properties = new NabProperties();
        properties.getDefaults().setConnectTimeoutMs(1111);
        properties.getDefaults().setCharset("windows-1256");
        properties.getDefaults().getServiceCodesByTerminalType().put("ATM", "01");

        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        core.setUserId("999998");
        core.setPassword("secret");
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("nab:core", null);

        assertEquals("core", config.provider());
        assertEquals("127.0.0.1:9999", config.endpoint());
        assertEquals(1111, config.connectTimeoutMs());
        assertEquals("windows-1256", config.charset());
        assertEquals("01", config.serviceCodesByTerminalType().get("ATM"));
        assertEquals("999998", config.userId());
    }

    @Test
    void defaultAtpiHeaderContainsRequiredClientAddressAfterProtocolOnlyForAtpi() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals("protocol", config.headerFieldsByProtocol().get("ATPI").get(0).name());
        assertEquals("clientAddress", config.headerFieldsByProtocol().get("ATPI").get(1).name());
        assertEquals(64, config.headerFieldsByProtocol().get("ATPI").get(1).length());
        assertEquals(true, config.headerFieldsByProtocol().get("ATPI").get(1).required());
        assertFalse(config.headerFieldsByProtocol().get("ATPS").stream().anyMatch(field -> "clientAddress".equals(field.name())));
        assertFalse(config.headerFieldsByProtocol().get("MIRS").stream().anyMatch(field -> "clientAddress".equals(field.name())));
    }

    @Test
    void headerFieldsCanBeConfiguredPerProtocol() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        NabProperties.Field protocol = new NabProperties.Field();
        protocol.setName("protocol");
        protocol.setLength(4);
        protocol.setRequired(true);
        NabProperties.Field clientAddress = new NabProperties.Field();
        clientAddress.setName("clientAddress");
        clientAddress.setLength(20);
        clientAddress.setRequired(true);
        core.getHeaderFieldsByProtocol().put("ATPI", List.of(protocol, clientAddress));
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals(2, config.headerFieldsByProtocol().get("ATPI").size());
        assertEquals(20, config.headerFieldsByProtocol().get("ATPI").get(1).length());
    }

    @Test
    void headerFieldsCanBeConfiguredPerInstanceWithoutProtocolKey() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        core.setProtocol("ATPI");
        NabProperties.Field protocol = new NabProperties.Field();
        protocol.setName("protocol");
        protocol.setLength(4);
        protocol.setRequired(true);
        NabProperties.Field clientAddress = new NabProperties.Field();
        clientAddress.setName("clientAddress");
        clientAddress.setLength(20);
        clientAddress.setRequired(true);
        core.setHeaderFields(List.of(protocol, clientAddress));
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals(2, config.headerFieldsByProtocol().get("ATPI").size());
        assertEquals(20, config.headerFieldsByProtocol().get("ATPI").get(1).length());
    }

    @Test
    void headerFieldsRequiresFixedProtocolInProviderInstance() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        NabProperties.Field protocol = new NabProperties.Field();
        protocol.setName("protocol");
        protocol.setLength(4);
        protocol.setRequired(true);
        core.setHeaderFields(List.of(protocol));
        properties.getProviders().put("core", core);

        assertThrows(IllegalArgumentException.class, () -> new NabConfigResolver(properties).resolve("core", null));
    }

    @Test
    void headerFieldsByProtocolMustBeDefinedPerProviderInstance() {
        NabProperties properties = new NabProperties();
        NabProperties.Field protocol = new NabProperties.Field();
        protocol.setName("protocol");
        protocol.setLength(4);
        protocol.setRequired(true);
        NabProperties.Field clientAddress = new NabProperties.Field();
        clientAddress.setName("clientAddress");
        clientAddress.setLength(20);
        clientAddress.setRequired(true);
        properties.getDefaults().getHeaderFieldsByProtocol().put("ATPI", List.of(protocol, clientAddress));

        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals(64, config.headerFieldsByProtocol().get("ATPI").get(1).length());
    }

    @Test
    void supportsLegacyEndpointsPropertyWithSingleValue() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoints(List.of("127.0.0.1:9999"));
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals("127.0.0.1:9999", config.endpoint());
    }

    @Test
    void rejectsLegacyEndpointsPropertyWithMultipleValues() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoints(List.of("127.0.0.1:9999", "127.0.0.1:9998"));
        properties.getProviders().put("core", core);

        assertThrows(IllegalArgumentException.class, () -> new NabConfigResolver(properties).resolve("core", null));
    }

    @Test
    void resolvesFixedProtocolPerProviderInstance() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        core.setProtocol("atpi");
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals("ATPI", config.protocol());
    }

    @Test
    void resolvesRateLimitAndAppliesOverrides() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoint("127.0.0.1:9999");
        core.getRateLimit().setEnabled(false);
        core.getRateLimit().setBucket("core-default");
        core.getRateLimit().setKey("provider");
        properties.getProviders().put("core", core);

        NabEndpointOverrides overrides = new NabEndpointOverrides(1000, "windows-1252", true, "bucket-x", "operation");
        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", overrides);

        assertTrue(config.rateLimit().enabled());
        assertEquals("bucket-x", config.rateLimit().bucket());
        assertEquals("operation", config.rateLimit().key());
    }
}
