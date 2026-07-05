package ir.daneshrefah.scm.common.event;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ScmSafeEventAttributes {
    public static final String SECURE_VALUE = "[SECURE]";

    private static final int MAX_DEPTH = 6;
    private static final List<String> SENSITIVE_CONTAINS = List.of(
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "access_token",
            "refresh_token",
            "password",
            "secret",
            "cvv",
            "cvv2",
            "card"
    );
    private static final List<String> SENSITIVE_TOKENS = List.of(
            "token",
            "jwt",
            "pin",
            "pan",
            "mac",
            "key"
    );
    private static final List<String> SAFE_KEY_SUFFIXES = List.of(
            ".event.type",
            ".event.code",
            ".event.action",
            ".provider.code",
            ".provider.type",
            ".endpoint.path",
            ".http.status.code",
            ".http.status_code",
            ".duration.ms",
            ".duration_ms",
            ".method",
            ".url.path",
            ".client.id",
            ".terminal.code",
            ".token.id",
            ".token_id",
            ".jti",
            ".span.id",
            ".trace.id",
            ".parent.span.id"
    );
    private static final List<String> SAFE_KEYS = List.of(
            "event.type",
            "event.code",
            "event.action",
            "provider.code",
            "provider.type",
            "endpoint.path",
            "http.status.code",
            "http.status_code",
            "duration",
            "duration.ms",
            "duration_ms",
            "method",
            "http.method",
            "url.path",
            "client.id",
            "scm.client.id",
            "terminal.code",
            "scm.terminal.code",
            "token.id",
            "token_id",
            "jti",
            "security.jti",
            "span.id",
            "trace.id",
            "parent.span.id"
    );

    private ScmSafeEventAttributes() {
    }

    public static Map<String, Object> copyOf(Map<String, ?> attributes) {
        return Collections.unmodifiableMap(mutableCopyOf(attributes));
    }

    public static Map<String, Object> mutableCopyOf(Map<String, ?> attributes) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (attributes == null || attributes.isEmpty()) {
            return result;
        }
        attributes.forEach((key, value) -> put(result, key, value));
        return result;
    }

    public static void put(Map<String, Object> target, String key, Object value) {
        if (target == null) {
            return;
        }
        String safeKey = safeKey(key);
        if (safeKey == null || value == null) {
            return;
        }
        Object safeValue = safeValue(safeKey, value, 0);
        if (safeValue != null) {
            target.put(safeKey, safeValue);
        }
    }

    public static boolean isSensitiveKey(String key) {
        String safeKey = safeKey(key);
        if (safeKey == null) {
            return false;
        }
        String normalized = normalizeKey(safeKey);
        if (isAllowedMetadataKey(normalized)) {
            return false;
        }
        for (String sensitive : SENSITIVE_CONTAINS) {
            if (normalized.contains(sensitive)) {
                return true;
            }
        }
        for (String sensitive : SENSITIVE_TOKENS) {
            if (containsToken(normalized, sensitive)) {
                return true;
            }
        }
        return false;
    }

    private static Object safeValue(String key, Object value, int depth) {
        if (value == null) {
            return null;
        }
        if (isSensitiveKey(key)) {
            return SECURE_VALUE;
        }
        if (value instanceof String text) {
            String sanitized = sanitizeText(text);
            return sanitized.isBlank() ? null : sanitized;
        }
        if (depth >= MAX_DEPTH) {
            return sanitizeText(String.valueOf(value));
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> safeMap = new LinkedHashMap<>();
            map.forEach((nestedKey, nestedValue) -> {
                if (nestedKey != null) {
                    putNested(safeMap, String.valueOf(nestedKey), nestedValue, depth + 1);
                }
            });
            return safeMap.isEmpty() ? null : Collections.unmodifiableMap(safeMap);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> safeValues = new ArrayList<>();
            for (Object item : iterable) {
                Object safeItem = safeValue(key, item, depth + 1);
                if (safeItem != null) {
                    safeValues.add(safeItem);
                }
            }
            return safeValues.isEmpty() ? null : List.copyOf(safeValues);
        }
        if (value.getClass().isArray()) {
            List<Object> safeValues = new ArrayList<>();
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                Object safeItem = safeValue(key, Array.get(value, index), depth + 1);
                if (safeItem != null) {
                    safeValues.add(safeItem);
                }
            }
            return safeValues.isEmpty() ? null : List.copyOf(safeValues);
        }
        return value;
    }

    private static void putNested(Map<String, Object> target, String key, Object value, int depth) {
        String safeKey = safeKey(key);
        if (safeKey == null || value == null) {
            return;
        }
        Object safeValue = safeValue(safeKey, value, depth);
        if (safeValue != null) {
            target.put(safeKey, safeValue);
        }
    }

    private static String safeKey(String key) {
        if (key == null) {
            return null;
        }
        String safeKey = sanitizeText(key);
        return safeKey.isBlank() ? null : safeKey;
    }

    private static String sanitizeText(String value) {
        return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static String normalizeKey(String key) {
        return key.toLowerCase(Locale.ROOT).replace('_', '-').trim();
    }

    private static boolean isAllowedMetadataKey(String normalizedKey) {
        if (SAFE_KEYS.contains(normalizedKey)) {
            return true;
        }
        for (String suffix : SAFE_KEY_SUFFIXES) {
            if (normalizedKey.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsToken(String normalizedKey, String token) {
        String[] parts = normalizedKey.split("[^a-z0-9]+");
        for (String part : parts) {
            if (token.equals(part)) {
                return true;
            }
        }
        return false;
    }
}
