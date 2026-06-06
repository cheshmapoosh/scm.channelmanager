package ir.daneshrefah.scm.common.provider.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProviderConfigurationBinder {
    private static final Set<String> MAP_PROPERTIES = Set.of(
            "headers",
            "form",
            "query",
            "body",
            "config",
            "provider-config",
            "providerConfig",
            "service-codes-by-terminal-type",
            "serviceCodesByTerminalType",
            "service-codes-by-channel-code",
            "serviceCodesByChannelCode",
            "header-fields-by-protocol",
            "headerFieldsByProtocol",
            "replacements"
    );

    private ProviderConfigurationBinder() {
    }

    public static <T> T bind(Map<String, Object> properties, Class<T> configType, String description) {
        if (configType == null || configType == Void.class || configType == Void.TYPE) {
            return null;
        }
        try {
            Map<String, Object> flattened = new LinkedHashMap<>();
            flatten("", properties == null ? Map.of() : properties, flattened, false);
            return new Binder(new MapConfigurationPropertySource(flattened))
                    .bind("", Bindable.of(configType))
                    .orElseGet(() -> instantiate(configType));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid configuration for " + description, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String path, Object value, Map<String, Object> flattened, boolean mapValue) {
        if (value instanceof Map<?, ?> map) {
            map.forEach((rawKey, rawValue) -> {
                if (rawKey == null) {
                    return;
                }
                String key = String.valueOf(rawKey);
                String childPath = childPath(path, key, mapValue);
                boolean childMapValue = mapValue || isMapProperty(key) || path.endsWith(".config");
                flatten(childPath, rawValue, flattened, childMapValue);
            });
            return;
        }
        if (value instanceof List<?> list) {
            for (int index = 0; index < list.size(); index++) {
                flatten(path + "[" + index + "]", list.get(index), flattened, false);
            }
            return;
        }
        if (StringUtils.isNotBlank(path)) {
            flattened.put(path, value);
        }
    }

    private static String childPath(String path, String key, boolean mapValue) {
        if (StringUtils.isBlank(path)) {
            return key;
        }
        if (mapValue) {
            return path + "[" + key + "]";
        }
        return path + "." + key;
    }

    private static boolean isMapProperty(String key) {
        return MAP_PROPERTIES.contains(key);
    }

    private static <T> T instantiate(Class<T> configType) {
        try {
            return configType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate configuration type " + configType.getName(), e);
        }
    }
}
