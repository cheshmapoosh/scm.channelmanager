package ir.daneshrefah.scm.common.provider.message;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ProviderMessageCustomizerFactoryRegistry {
    private final Map<String, ProviderMessageCustomizerFactory<?>> factories;

    public ProviderMessageCustomizerFactoryRegistry(Collection<ProviderMessageCustomizerFactory<?>> factories) {
        Map<String, ProviderMessageCustomizerFactory<?>> items = new LinkedHashMap<>();
        if (factories != null) {
            for (ProviderMessageCustomizerFactory<?> factory : factories) {
                if (factory == null || StringUtils.isBlank(factory.type())) {
                    continue;
                }
                String key = key(factory.type());
                ProviderMessageCustomizerFactory<?> previous = items.putIfAbsent(key, factory);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate provider message customizer factory type: " + factory.type());
                }
            }
        }
        this.factories = Map.copyOf(items);
    }

    public ProviderMessageCustomizerFactory<?> getRequired(String type) {
        String key = key(type);
        ProviderMessageCustomizerFactory<?> factory = factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown provider message customizer type: " + type);
        }
        return factory;
    }

    public boolean contains(String type) {
        return factories.containsKey(key(type));
    }

    private String key(String type) {
        String normalized = StringUtils.trimToNull(type);
        if (normalized == null) {
            throw new IllegalArgumentException("Provider message customizer type is required");
        }
        return normalized.toLowerCase(Locale.ROOT);
    }
}
