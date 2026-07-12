package ir.daneshrefah.scm.cache.starter.config.validation;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
import org.springframework.util.StringUtils;

import java.util.Map;

public final class CacheClientPropertiesValidator {

    private CacheClientPropertiesValidator() {
    }

    public static void validate(CacheClientProperties properties) {
        if (properties == null) {
            return;
        }
        validateGlobalDefaults(properties);
        validateCacheDefinitions(properties);
        validateNearRequirements(properties);
    }

    private static void validateGlobalDefaults(CacheClientProperties properties) {
        if (properties.getLocal() != null && properties.getLocal().getMaximumSize() <= 0) {
            throw new IllegalStateException("scm.cache.client.local.maximum-size must be positive.");
        }
        if (properties.getNear() != null && properties.getNear().getMaximumSize() <= 0) {
            throw new IllegalStateException("scm.cache.client.near.maximum-size must be positive.");
        }
    }

    private static void validateCacheDefinitions(CacheClientProperties properties) {
        for (Map.Entry<String, CacheClientProperties.CacheDefinition> entry : properties.getCaches().entrySet()) {
            String cacheName = entry.getKey();
            CacheClientProperties.CacheDefinition definition = entry.getValue();
            if (definition == null) {
                continue;
            }
            CacheType effectiveType = effectiveType(properties, definition);
            validateAllowedFields(cacheName, effectiveType, definition);
            validateFieldValues(cacheName, definition);
        }
    }

    private static void validateAllowedFields(
            String cacheName,
            CacheType effectiveType,
            CacheClientProperties.CacheDefinition definition
    ) {
        switch (effectiveType) {
            case LOCAL -> {
                if (definition.getRemoteName() != null) {
                    throw new IllegalStateException(
                            "Cache '%s' has effective type LOCAL and must not define remote-name.".formatted(cacheName)
                    );
                }
            }
            case REMOTE -> {
                if (definition.getTtl() != null) {
                    throw new IllegalStateException(
                            "Cache '%s' has effective type REMOTE and must not define ttl.".formatted(cacheName)
                    );
                }
                if (definition.getMaximumSize() != null) {
                    throw new IllegalStateException(
                            "Cache '%s' has effective type REMOTE and must not define maximum-size.".formatted(cacheName)
                    );
                }
            }
            case NEAR -> {
                if (definition.getTtl() != null) {
                    throw new IllegalStateException(
                            "Cache '%s' has effective type NEAR and must not define ttl.".formatted(cacheName)
                    );
                }
            }
        }
    }

    private static void validateFieldValues(String cacheName, CacheClientProperties.CacheDefinition definition) {
        if (definition.getMaximumSize() != null && definition.getMaximumSize() <= 0) {
            throw new IllegalStateException(
                    "Cache '%s' maximum-size must be positive when specified.".formatted(cacheName)
            );
        }
        if (definition.getRemoteName() != null && !StringUtils.hasText(definition.getRemoteName())) {
            throw new IllegalStateException(
                    "Cache '%s' remote-name must not be blank when specified.".formatted(cacheName)
            );
        }
    }

    private static void validateNearRequirements(CacheClientProperties properties) {
        if (!hasNearRoute(properties)) {
            return;
        }
        if (!properties.isDistributed()) {
            throw new IllegalStateException(
                    "Cache type NEAR requires scm.cache.client.distributed=true and a remote Hazelcast client."
            );
        }
        if (properties.getNear() == null || !properties.getNear().isEnabled()) {
            throw new IllegalStateException("Cache type NEAR requires scm.cache.client.near.enabled=true.");
        }
    }

    private static boolean hasNearRoute(CacheClientProperties properties) {
        if (effectiveDefaultType(properties) == CacheType.NEAR) {
            return true;
        }
        return properties.getCaches().values().stream()
                .anyMatch(definition -> definition != null && effectiveType(properties, definition) == CacheType.NEAR);
    }

    private static CacheType effectiveType(
            CacheClientProperties properties,
            CacheClientProperties.CacheDefinition definition
    ) {
        if (definition != null && definition.getType() != null) {
            return definition.getType();
        }
        return effectiveDefaultType(properties);
    }

    private static CacheType effectiveDefaultType(CacheClientProperties properties) {
        return properties.getDefaultType() == null ? CacheType.REMOTE : properties.getDefaultType();
    }
}
