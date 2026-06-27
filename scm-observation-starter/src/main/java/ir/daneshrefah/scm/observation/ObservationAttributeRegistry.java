package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.audit.ServiceExecuteAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.log.CommonLogAttributes;
import ir.daneshrefah.scm.observation.attributes.metric.CommonMetricTags;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ObservationAttributeRegistry {
    private static final ObservationAttributeRegistry COMMON_ONLY = new ObservationAttributeRegistry(List.of());

    private final Map<ObservationStream, Map<String, ObservationAttributeKey<?>>> attributesByStream;
    private final Map<String, ObservationAttributeKey<?>> firstAttributeByName;

    public ObservationAttributeRegistry() {
        this(List.of());
    }

    public ObservationAttributeRegistry(Collection<ObservationAttributeContributor> contributors) {
        Map<ObservationStream, Map<String, ObservationAttributeKey<?>>> registered = new EnumMap<>(ObservationStream.class);
        for (ObservationStream stream : ObservationStream.values()) {
            registered.put(stream, new LinkedHashMap<>());
        }
        registerAll(registered, CommonLogAttributes.attributes());
        registerAll(registered, CommonTraceAttributes.attributes());
        registerAll(registered, ChangeEntityAuditAttributes.attributes());
        registerAll(registered, ServiceExecuteAuditAttributes.attributes());
        registerAll(registered, CommonMetricTags.attributes());
        if (contributors != null) {
            for (ObservationAttributeContributor contributor : contributors) {
                if (contributor != null) {
                    registerAll(registered, contributor.attributes());
                }
            }
        }
        Map<ObservationStream, Map<String, ObservationAttributeKey<?>>> immutableByStream = new EnumMap<>(ObservationStream.class);
        Map<String, ObservationAttributeKey<?>> firstByName = new LinkedHashMap<>();
        for (Map.Entry<ObservationStream, Map<String, ObservationAttributeKey<?>>> entry : registered.entrySet()) {
            Map<String, ObservationAttributeKey<?>> streamAttributes = Collections.unmodifiableMap(entry.getValue());
            immutableByStream.put(entry.getKey(), streamAttributes);
            for (ObservationAttributeKey<?> key : streamAttributes.values()) {
                firstByName.putIfAbsent(key.name(), key);
            }
        }
        this.attributesByStream = Collections.unmodifiableMap(immutableByStream);
        this.firstAttributeByName = Collections.unmodifiableMap(firstByName);
    }

    public static ObservationAttributeRegistry commonOnly() {
        return COMMON_ONLY;
    }

    public Collection<ObservationAttributeKey<?>> all() {
        Set<ObservationAttributeKey<?>> attributes = new LinkedHashSet<>();
        for (Map<String, ObservationAttributeKey<?>> streamAttributes : attributesByStream.values()) {
            attributes.addAll(streamAttributes.values());
        }
        return Collections.unmodifiableSet(attributes);
    }

    public Collection<ObservationAttributeKey<?>> all(ObservationStream stream) {
        return attributes(stream).values();
    }

    public Optional<ObservationAttributeKey<?>> findByName(String name) {
        return Optional.ofNullable(firstAttributeByName.get(name));
    }

    public Optional<ObservationAttributeKey<?>> findByName(ObservationStream stream, String name) {
        return Optional.ofNullable(attributes(stream).get(name));
    }

    public boolean contains(String name) {
        return firstAttributeByName.containsKey(name);
    }

    public boolean contains(ObservationStream stream, String name) {
        return attributes(stream).containsKey(name);
    }

    public boolean containsLogAttribute(String name) {
        return contains(ObservationStream.LOG, name);
    }

    public Object prepareValue(String name, Object value) {
        return prepareValue(ObservationStream.LOG, name, value);
    }

    public Object prepareValue(ObservationStream stream, String name, Object value) {
        ObservationAttributeKey<?> key = attributes(stream).get(name);
        if (key == null || value == null) {
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

    private Map<String, ObservationAttributeKey<?>> attributes(ObservationStream stream) {
        if (stream == null) {
            return Map.of();
        }
        return attributesByStream.getOrDefault(stream, Map.of());
    }

    private void registerAll(Map<ObservationStream, Map<String, ObservationAttributeKey<?>>> registered,
                             Collection<ObservationAttributeKey<?>> keys) {
        if (keys == null) {
            return;
        }
        for (ObservationAttributeKey<?> key : keys) {
            register(registered, key);
        }
    }

    private void register(Map<ObservationStream, Map<String, ObservationAttributeKey<?>>> registered,
                          ObservationAttributeKey<?> key) {
        if (key == null) {
            return;
        }
        for (ObservationStream stream : key.streams()) {
            Map<String, ObservationAttributeKey<?>> streamAttributes = registered.get(stream);
            if (streamAttributes == null) {
                continue;
            }
            ObservationAttributeKey<?> existing = streamAttributes.get(key.name());
            if (existing != null && !existing.compatibleWith(key)) {
                throw new IllegalStateException("Observation attribute has incompatible duplicate metadata: "
                        + stream + ":" + key.name());
            }
            streamAttributes.putIfAbsent(key.name(), key);
        }
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

}
