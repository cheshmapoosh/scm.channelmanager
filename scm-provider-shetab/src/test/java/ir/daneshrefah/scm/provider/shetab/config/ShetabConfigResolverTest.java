package ir.daneshrefah.scm.provider.shetab.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShetabConfigResolverTest {


    @Test
    void resolvesUnifiedShetabProviderWithoutDefaults() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider hps = new ProviderRegistryProperties.Provider();
        hps.setType("shetab");
        hps.setEndpoint("10.10.10.10:9000");
        hps.setPackagerClass("Shetab7AsciiXAPackager");
        ProviderMessageCustomizerDefinition mac = new ProviderMessageCustomizerDefinition();
        mac.setType("shetab-mac");
        hps.getMessageCustomizers().add(mac);
        registry.getProviders().put("hps-shetab7", hps);

        ShetabResolvedConfig config = new ShetabConfigResolver(registry, new ShetabProperties()).resolve("hps-shetab7", null);

        assertEquals("hps-shetab7", config.provider());
        assertEquals("shetab", config.providerType());
        assertEquals(List.of("10.10.10.10:9000"), config.endpoints());
        assertEquals("shetab-mac", config.messageCustomizers().getFirst().getType());
    }

    @Test
    void unifiedShetabProviderFailsWhenPackagerMissing() {
        ProviderRegistryProperties registry = new ProviderRegistryProperties();
        ProviderRegistryProperties.Provider hps = new ProviderRegistryProperties.Provider();
        hps.setType("shetab");
        hps.setEndpoint("10.10.10.10:9000");
        registry.getProviders().put("hps", hps);

        assertThrows(IllegalArgumentException.class,
                () -> new ShetabConfigResolver(registry, new ShetabProperties()).resolve("hps", null));
    }

    @Test
    void resolvesTypedOperationProviderNameToShetabProviderInstance() {
        ShetabProperties properties = new ShetabProperties();
        ShetabProperties.Instance hps = new ShetabProperties.Instance();
        hps.setEndpoints(List.of("10.10.10.10:9000"));
        hps.setPackagerClass("Shetab7AsciiXAPackager");
        properties.getProviders().put("hps", hps);

        ShetabResolvedConfig config = new ShetabConfigResolver(properties).resolve("shetab:hps", null);

        assertEquals("hps", config.provider());
        assertEquals(List.of("10.10.10.10:9000"), config.endpoints());
    }

    @Test
    void providerSecurityOverridesDefaultsOnlyWhenConfigured() {
        ShetabProperties properties = new ShetabProperties();
        properties.getDefaults().getSecurity().getPin().setEnabled(true);
        properties.getDefaults().getSecurity().getPin().setKey("0123456789ABCDEF");
        properties.getDefaults().getSecurity().getExpiry().setField(14);
        properties.getDefaults().getSecurity().getCvv2().setField(48);
        properties.getDefaults().getSecurity().getCvv2().setTag("P92");

        ShetabProperties.Instance hps = new ShetabProperties.Instance();
        hps.setEndpoints(List.of("10.10.10.10:9000"));
        properties.getProviders().put("hps", hps);

        ShetabResolvedConfig config = new ShetabConfigResolver(properties).resolve("hps", null);

        assertEquals(true, config.security().pin().enabled());
        assertEquals("0123456789ABCDEF", config.security().pin().key());
        assertEquals(14, config.security().expiry().field());
        assertEquals(48, config.security().cvv2().field());
        assertEquals("P92", config.security().cvv2().tag());
    }

    @Test
    void endpointAliasCanBeUsedInsteadOfEndpointsList() {
        ShetabProperties properties = new ShetabProperties();
        ShetabProperties.Instance hps = new ShetabProperties.Instance();
        hps.setEndpoint("10.10.10.10:9000");
        hps.setPackagerClass("Shetab7AsciiXAPackager");
        properties.getProviders().put("hps", hps);

        ShetabResolvedConfig config = new ShetabConfigResolver(properties).resolve("hps", null);

        assertEquals(List.of("10.10.10.10:9000"), config.endpoints());
    }
}
