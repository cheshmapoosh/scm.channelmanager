package ir.daneshrefah.scm.provider.nab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderConfigurationBinder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderConfigurationBinderNabTest {

    @Test
    void nabProviderInstanceBindingPreservesMapKeysAndNestedHeaderFields() {
        NabProviderInstanceProperties config = ProviderConfigurationBinder.bind(Map.of(
                "type", "nab",
                "protocol", "ATPI",
                "endpoint", "10.10.10.10:3080",
                "user-id", "999998",
                "password", "secret",
                "service-codes-by-terminal-type", Map.of(
                        "ATM", "01",
                        "POS-Branch", "02"
                ),
                "service-codes-by-channel-code", Map.of(
                        "MB", "03",
                        "USSD-01", "04"
                ),
                "header-fields-by-protocol", Map.of(
                        "ATPI", List.of(
                                Map.of("name", "protocol", "length", 4, "required", true),
                                Map.of("name", "clientAddress", "length", 64, "required", true)
                        ),
                        "MIRS-v2", List.of(
                                Map.of("name", "protocol", "length", 4, "required", true)
                        )
                ),
                "character-normalization", Map.of(
                        "replacements", Map.of(
                                "dash-key", "dash-value",
                                "under_score", "under-value"
                        )
                )
        ), NabProviderInstanceProperties.class, "nab provider test");

        assertEquals("01", config.getServiceCodesByTerminalType().get("ATM"));
        assertEquals("02", config.getServiceCodesByTerminalType().get("POS-Branch"));
        assertEquals("03", config.getServiceCodesByChannelCode().get("MB"));
        assertEquals("04", config.getServiceCodesByChannelCode().get("USSD-01"));
        assertTrue(config.getHeaderFieldsByProtocol().containsKey("ATPI"));
        assertTrue(config.getHeaderFieldsByProtocol().containsKey("MIRS-v2"));
        assertEquals(2, config.getHeaderFieldsByProtocol().get("ATPI").size());
        assertEquals("clientAddress", config.getHeaderFieldsByProtocol().get("ATPI").get(1).getName());
        assertEquals(64, config.getHeaderFieldsByProtocol().get("ATPI").get(1).getLength());
        assertEquals("dash-value", config.getCharacterNormalization().getReplacements().get("dash-key"));
        assertEquals("under-value", config.getCharacterNormalization().getReplacements().get("under_score"));
    }
}
