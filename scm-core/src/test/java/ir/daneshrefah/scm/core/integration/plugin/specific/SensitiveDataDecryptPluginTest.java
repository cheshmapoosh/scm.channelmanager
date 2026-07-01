package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.core.services.crypto.SensitiveDataDecryptService;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SensitiveDataDecryptPluginTest {

    private final SensitiveDataDecryptService decryptService = mock(SensitiveDataDecryptService.class);
    private final SensitiveDataDecryptPlugin plugin = new SensitiveDataDecryptPlugin(decryptService, new ObjectMapper());

    @Test
    void decryptsWhenChannelFilterIsMissing() {
        Exchange exchange = exchange();
        exchange.getMessage().setBody("""
                {"trk2EquivData":{"cvv2":"encrypted-cvv2"}}
                """);
        when(decryptService.decrypt("encrypted-cvv2")).thenReturn("123");

        plugin.handle(exchange, pluginDetail(baseConfig()));

        assertEquals("123", exchange.getMessage().getHeader("cvv2"));
    }

    @Test
    void decryptsWhenChannelFilterIsDisabled() {
        Exchange exchange = exchange();
        exchange.getMessage().setBody("""
                {"trk2EquivData":{"cvv2":"encrypted-cvv2"}}
                """);
        when(decryptService.decrypt("encrypted-cvv2")).thenReturn("123");

        plugin.handle(exchange, pluginDetail(configWithChannelFilter(false, List.of("IB"))));

        assertEquals("123", exchange.getMessage().getHeader("cvv2"));
    }

    @Test
    void decryptsWhenChannelMatchesAllowedValueIgnoringCase() {
        Exchange exchange = exchange();
        exchange.getMessage().setHeader("channelCode", "ib");
        exchange.getMessage().setBody("""
                {"trk2EquivData":{"cvv2":"encrypted-cvv2"}}
                """);
        when(decryptService.decrypt("encrypted-cvv2")).thenReturn("123");

        plugin.handle(exchange, pluginDetail(configWithChannelFilter(true, List.of("IB", "CIB"))));

        assertEquals("123", exchange.getMessage().getHeader("cvv2"));
    }

    @Test
    void skipsWhenChannelDoesNotMatchAllowedValues() {
        Exchange exchange = exchange();
        exchange.getMessage().setHeader("channelCode", "POS");
        exchange.getMessage().setBody("""
                {"trk2EquivData":{"cvv2":"encrypted-cvv2"}}
                """);

        plugin.handle(exchange, pluginDetail(configWithChannelFilter(true, List.of("IB", "CIB"))));

        verifyNoInteractions(decryptService);
    }

    @Test
    void skipsWhenChannelFilterIsEnabledAndNoChannelValueExists() {
        Exchange exchange = exchange();
        exchange.getMessage().setBody("""
                {"trk2EquivData":{"cvv2":"encrypted-cvv2"}}
                """);

        plugin.handle(exchange, pluginDetail(configWithChannelFilter(true, List.of("IB", "CIB"))));

        verifyNoInteractions(decryptService);
    }

    @Test
    void readsChannelFromBodySource() {
        Exchange exchange = exchange();
        exchange.getMessage().setBody("""
                {"channelCode":"MB","trk2EquivData":{"cvv2":"encrypted-cvv2"}}
                """);
        when(decryptService.decrypt("encrypted-cvv2")).thenReturn("123");

        plugin.handle(exchange, pluginDetail(Map.of(
                "enabled", true,
                "failOnDecryptError", true,
                "channelFilter", Map.of(
                        "enabled", true,
                        "sources", List.of("body:channelCode"),
                        "allowedValues", List.of("MB")
                ),
                "fields", fields()
        )));

        assertEquals("123", exchange.getMessage().getHeader("cvv2"));
    }

    private Exchange exchange() {
        return new DefaultExchange(new DefaultCamelContext());
    }

    private PluginDetail pluginDetail(Map<String, ?> config) {
        PluginDetail pluginDetail = new PluginDetail();
        pluginDetail.setName("sensitiveDataDecryptPlugin");
        pluginDetail.setConfig(config);
        return pluginDetail;
    }

    private Map<String, ?> baseConfig() {
        return Map.of(
                "enabled", true,
                "failOnDecryptError", true,
                "fields", fields()
        );
    }

    private Map<String, ?> configWithChannelFilter(boolean channelFilterEnabled, List<String> allowedValues) {
        return Map.of(
                "enabled", true,
                "failOnDecryptError", true,
                "channelFilter", Map.of(
                        "enabled", channelFilterEnabled,
                        "sources", List.of("header:channelCode", "body:channelCode"),
                        "allowedValues", allowedValues
                ),
                "fields", fields()
        );
    }

    private List<Map<String, ?>> fields() {
        return List.of(Map.of(
                "name", "cvv2",
                "sources", List.of("body:trk2EquivData.cvv2"),
                "targets", List.of("header:cvv2"),
                "required", false,
                "validateRegex", "\\d{3,4}"
        ));
    }
}
