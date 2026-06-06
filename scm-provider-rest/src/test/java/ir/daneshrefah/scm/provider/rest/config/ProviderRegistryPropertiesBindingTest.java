package ir.daneshrefah.scm.provider.rest.config;

import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderRegistryPropertiesBindingTest {

    @Test
    void bindsUnifiedProvidersDirectlyUnderScmProviders() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "scm.providers.hps-rest.type", "rest",
                "scm.providers.hps-rest.base-url", "https://hps.example.ir",
                "scm.providers.hps-rest.message-customizers[0].type", "hps-rest-outlet",
                "scm.providers.hps-rest.message-customizers[0].config.name", "outlet"
        )));

        ProviderRegistryProperties properties = Binder.get(environment)
                .bind("scm.providers", Bindable.of(ProviderRegistryProperties.class))
                .orElseThrow(() -> new AssertionError("scm.providers did not bind"));

        assertTrue(properties.getProviders().containsKey("hps-rest"));
        ProviderRegistryProperties.Provider provider = properties.getProviders().get("hps-rest");
        assertEquals("rest", provider.getType());
        assertEquals("https://hps.example.ir", provider.getBaseUrl());
        assertEquals("hps-rest-outlet", provider.getMessageCustomizers().getFirst().getType());
        assertEquals("outlet", provider.getMessageCustomizers().getFirst().config().get("name"));
    }
}
