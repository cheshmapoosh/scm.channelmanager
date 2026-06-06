package ir.daneshrefah.scm.common.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "scm.providers")
public class ProviderRegistryProperties extends LinkedHashMap<String, Map<String, Object>> {

    public Map<String, Map<String, Object>> providers() {
        return this;
    }

    public Map<String, Object> provider(String providerCode) {
        return get(providerCode);
    }

    public boolean hasProvider(String providerCode) {
        return containsKey(providerCode);
    }
}
