package ir.daneshrefah.scm.core.integration.security;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable, protocol-neutral subset of attributes obtained from an authenticated identity.
 */
public final class AuthenticatedAttributeSnapshot {
    private static final AuthenticatedAttributeSnapshot EMPTY = new AuthenticatedAttributeSnapshot(Map.of());

    private final Map<String, Object> attributes;

    private AuthenticatedAttributeSnapshot(Map<String, Object> attributes) {
        this.attributes = Map.copyOf(attributes);
    }

    public static AuthenticatedAttributeSnapshot empty() {
        return EMPTY;
    }

    public static AuthenticatedAttributeSnapshot requestedValues(
            Set<String> requestedNames,
            Map<String, ?> authenticatedAttributes
    ) {
        if (requestedNames == null || requestedNames.isEmpty()
                || authenticatedAttributes == null || authenticatedAttributes.isEmpty()) {
            return EMPTY;
        }
        Map<String, Object> selected = new LinkedHashMap<>();
        for (String name : requestedNames) {
            if (name == null || !authenticatedAttributes.containsKey(name)) {
                continue;
            }
            Object scalar = immutableScalar(authenticatedAttributes.get(name));
            if (scalar != null) {
                selected.put(name, scalar);
            }
        }
        return selected.isEmpty() ? EMPTY : new AuthenticatedAttributeSnapshot(selected);
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public Object value(String name) {
        return name == null ? null : attributes.get(name);
    }

    private static Object immutableScalar(Object value) {
        if (value instanceof CharSequence text) {
            return text.toString();
        }
        if (value instanceof Character character) {
            return character.toString();
        }
        if (value instanceof Boolean
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof BigInteger
                || value instanceof BigDecimal) {
            return value;
        }
        if (value instanceof Double doubleValue) {
            return Double.isFinite(doubleValue) ? doubleValue : null;
        }
        if (value instanceof Float floatValue) {
            return Float.isFinite(floatValue) ? floatValue : null;
        }
        if (value instanceof TemporalAccessor || value instanceof URI || value instanceof URL) {
            return value.toString();
        }
        return null;
    }
}
