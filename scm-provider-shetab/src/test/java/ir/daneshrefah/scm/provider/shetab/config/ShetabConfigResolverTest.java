package ir.daneshrefah.scm.provider.shetab.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShetabConfigResolverTest {

    @Test
    void resolvesTypedOperationProviderNameToShetabProviderInstance() {
        ShetabProperties properties = new ShetabProperties();
        ShetabProperties.Instance poya = new ShetabProperties.Instance();
        poya.setEndpoints(List.of("10.10.10.10:9000"));
        poya.setPackagerClass("Shetab7AsciiXAPackager");
        properties.getProviders().put("poya", poya);

        ShetabResolvedConfig config = new ShetabConfigResolver(properties).resolve("shetab:poya", null);

        assertEquals("poya", config.provider());
        assertEquals(List.of("10.10.10.10:9000"), config.endpoints());
    }

    @Test
    void providerSecurityOverridesDefaultsOnlyWhenConfigured() {
        ShetabProperties properties = new ShetabProperties();
        properties.getDefaults().getSecurity().getPin().setEnabled(true);
        properties.getDefaults().getSecurity().getPin().setKey("0123456789ABCDEF");

        ShetabProperties.Instance poya = new ShetabProperties.Instance();
        poya.setEndpoints(List.of("10.10.10.10:9000"));
        properties.getProviders().put("poya", poya);

        ShetabResolvedConfig config = new ShetabConfigResolver(properties).resolve("poya", null);

        assertEquals(true, config.security().pin().enabled());
        assertEquals("0123456789ABCDEF", config.security().pin().key());
    }
}
