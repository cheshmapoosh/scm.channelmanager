package ir.daneshrefah.scm.provider.nab.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NabConfigResolverTest {

    @Test
    void resolvesTypedProviderAndMergesDefaults() {
        NabProperties properties = new NabProperties();
        properties.getDefaults().setConnectTimeoutMs(1111);
        properties.getDefaults().setCharset("windows-1256");
        properties.getDefaults().getServiceCodesByTerminalType().put("ATM", "01");

        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoints(List.of("127.0.0.1:9999"));
        core.setUserId("999998");
        core.setPassword("secret");
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("nab:core", null);

        assertEquals("core", config.provider());
        assertEquals(1111, config.connectTimeoutMs());
        assertEquals("windows-1256", config.charset());
        assertEquals("01", config.serviceCodesByTerminalType().get("ATM"));
        assertEquals("999998", config.userId());
    }

    @Test
    void defaultAtpiHeaderContainsRequiredClientAddressAfterProtocolOnlyForAtpi() {
        NabProperties properties = new NabProperties();
        NabProperties.Instance core = new NabProperties.Instance();
        core.setEndpoints(List.of("127.0.0.1:9999"));
        properties.getProviders().put("core", core);

        NabResolvedConfig config = new NabConfigResolver(properties).resolve("core", null);

        assertEquals("nabProtocol", config.headerFieldsByProtocol().get("ATPI").get(0).name());
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
        core.setEndpoints(List.of("127.0.0.1:9999"));
        NabProperties.Field protocol = new NabProperties.Field();
        protocol.setName("nabProtocol");
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
}
