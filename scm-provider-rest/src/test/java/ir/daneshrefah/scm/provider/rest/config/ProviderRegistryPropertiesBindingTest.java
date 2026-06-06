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

        assertTrue(properties.containsKey("hps-rest"));
        Map<String, Object> provider = properties.get("hps-rest");
        assertEquals("rest", provider.get("type"));
        assertEquals("https://hps.example.ir", provider.get("base-url"));
        assertTrue(provider.containsKey("message-customizers"));
    }
}
