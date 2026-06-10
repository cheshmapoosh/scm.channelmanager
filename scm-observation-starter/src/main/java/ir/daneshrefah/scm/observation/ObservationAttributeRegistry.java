package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.ScmCommonLogAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

public class ObservationAttributeRegistry {
    private static final ObservationAttributeRegistry EFFECTIVE_LOG_REGISTRY =
            new ObservationAttributeRegistry(loadContributors());

    private final Map<String, ObservationAttributeKey<?>> attributes;

    public ObservationAttributeRegistry() {
        this(List.of());
    }

    public ObservationAttributeRegistry(Collection<ObservationAttributeContributor> contributors) {
        Map<String, ObservationAttributeKey<?>> registered = new LinkedHashMap<>();
        registerAll(registered, ScmCommonLogAttributes.attributes());
        if (contributors != null) {
            for (ObservationAttributeContributor contributor : contributors) {
                if (contributor != null) {
                    registerAll(registered, contributor.attributes());
                }
            }
        }
        this.attributes = Collections.unmodifiableMap(registered);
    }

    public static ObservationAttributeRegistry effectiveLogRegistry() {
        return EFFECTIVE_LOG_REGISTRY;
    }

    public Collection<ObservationAttributeKey<?>> all() {
        return attributes.values();
    }

    public Optional<ObservationAttributeKey<?>> findByName(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    public boolean contains(String name) {
        return attributes.containsKey(name);
    }

    public boolean containsLogAttribute(String name) {
        return findByName(name)
                .map(key -> key.streams().contains(ObservationStream.LOG))
                .orElse(false);
    }

    public Object prepareValue(String name, Object value) {
        ObservationAttributeKey<?> key = attributes.get(name);
        if (key == null || !key.streams().contains(ObservationStream.LOG) || value == null) {
            return null;
        }
        String text = value instanceof String ? ((String) value).trim() : null;
        if (text != null && (text.isBlank() || "-".equals(text) || "null".equalsIgnoreCase(text))) {
            return null;
        }
        if (key.sensitivity() == ObservationAttributeSensitivity.RAW) {
            return value;
        }
        return mask(String.valueOf(value), key);
    }

    private void registerAll(Map<String, ObservationAttributeKey<?>> registered,
                             Collection<ObservationAttributeKey<?>> keys) {
        if (keys == null) {
            return;
        }
        for (ObservationAttributeKey<?> key : keys) {
            register(registered, key);
        }
    }

    private void register(Map<String, ObservationAttributeKey<?>> registered, ObservationAttributeKey<?> key) {
        if (key == null) {
            return;
        }
        ObservationAttributeKey<?> existing = registered.get(key.name());
        if (existing != null && !existing.compatibleWith(key)) {
            throw new IllegalStateException("Observation attribute has incompatible duplicate metadata: " + key.name());
        }
        registered.putIfAbsent(key.name(), key);
    }

    private Object mask(String value, ObservationAttributeKey<?> key) {
        if (value == null) {
            return null;
        }
        return switch (key.sensitivity()) {
            case SECURE -> "[SECURE]";
            case MASK_PREFIX -> prefix(value, key.visiblePrefixLength());
            case MASK_SUFFIX -> suffix(value, key.visibleSuffixLength());
            case MASK_PREFIX_SUFFIX -> prefixSuffix(value, key.visiblePrefixLength(), key.visibleSuffixLength());
            case RAW -> value;
        };
    }

    private String prefix(String value, int length) {
        if (length <= 0 || value.length() <= length) {
            return "...";
        }
        return value.substring(0, length) + "...";
    }

    private String suffix(String value, int length) {
        if (length <= 0 || value.length() <= length) {
            return "...";
        }
        return "..." + value.substring(value.length() - length);
    }

    private String prefixSuffix(String value, int prefix, int suffix) {
        if (prefix <= 0 && suffix <= 0) {
            return "...";
        }
        if (value.length() <= prefix + suffix) {
            return "...";
        }
        return value.substring(0, Math.max(0, prefix))
                + "..."
                + value.substring(value.length() - Math.max(0, suffix));
    }

    private static Collection<ObservationAttributeContributor> loadContributors() {
        return ServiceLoader.load(ObservationAttributeContributor.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }
}
