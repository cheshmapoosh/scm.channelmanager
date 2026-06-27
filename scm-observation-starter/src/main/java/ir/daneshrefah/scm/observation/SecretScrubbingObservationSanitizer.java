package ir.daneshrefah.scm.observation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class SecretScrubbingObservationSanitizer implements ObservationSanitizer {
    private static final String SECURE = "[SECURE]";
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._~+/=-]+");
    private static final Pattern JWT_TOKEN = Pattern.compile("\\beyJ[A-Za-z0-9_-]*\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b");
    private static final Pattern SENSITIVE_ASSIGNMENT = Pattern.compile(
            "(?i)(password|token|authorization|client_secret|authorization_code|pin|cvv2?|pan|account[_ -]?number)\\s*[:=]\\s*\\S+"
    );
    private static final String JWT_HASH_FIELD = "scm.auth.jwt.hash";
    private static final String UAA_JWT_MASKED_FIELD = "uaa.jwt.masked";

    @Override
    public Object sanitize(String fieldName, Object value) {
        if (value == null) {
            return null;
        }
        String normalizedField = fieldName == null ? "" : fieldName.toLowerCase(Locale.ROOT);
        if (isAuthorizationField(normalizedField)) {
            return null;
        }
        if (value instanceof String text) {
            return sanitizeString(normalizedField, text);
        }
        if (value instanceof Map<?, ?> map) {
            return sanitizeMap(normalizedField, map);
        }
        if (value instanceof Iterable<?> iterable) {
            return sanitizeIterable(normalizedField, iterable);
        }
        if (value.getClass().isArray()) {
            return sanitizeArray(normalizedField, value);
        }
        return value;
    }

    private boolean isAuthorizationField(String normalizedField) {
        return "authorization".equals(normalizedField)
                || normalizedField.endsWith(".authorization")
                || normalizedField.endsWith("_authorization");
    }

    private Object sanitizeString(String normalizedField, String value) {
        if (UAA_JWT_MASKED_FIELD.equals(normalizedField)) {
            return value;
        }
        String sanitized = BEARER_TOKEN.matcher(value).replaceAll(SECURE);
        sanitized = JWT_TOKEN.matcher(sanitized).replaceAll(SECURE);
        sanitized = SENSITIVE_ASSIGNMENT.matcher(sanitized).replaceAll("$1=" + SECURE);
        if (JWT_HASH_FIELD.equals(normalizedField)) {
            return isSha256Hash(sanitized) ? sanitized : SECURE;
        }
        return sanitized;
    }

    private boolean isSha256Hash(String value) {
        return value.startsWith("sha256:") && !value.contains(SECURE);
    }

    private Map<String, Object> sanitizeMap(String parentField, Map<?, ?> value) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            String childField = String.valueOf(entry.getKey());
            Object sanitizedValue = sanitize(parentField.isBlank() ? childField : parentField + "." + childField, entry.getValue());
            if (sanitizedValue != null) {
                sanitized.put(childField, sanitizedValue);
            }
        }
        return sanitized;
    }

    private List<Object> sanitizeIterable(String fieldName, Iterable<?> value) {
        List<Object> sanitized = new ArrayList<>();
        for (Object item : value) {
            Object sanitizedValue = sanitize(fieldName, item);
            if (sanitizedValue != null) {
                sanitized.add(sanitizedValue);
            }
        }
        return sanitized;
    }

    private List<Object> sanitizeArray(String fieldName, Object value) {
        List<Object> sanitized = new ArrayList<>();
        int length = Array.getLength(value);
        for (int index = 0; index < length; index++) {
            Object sanitizedValue = sanitize(fieldName, Array.get(value, index));
            if (sanitizedValue != null) {
                sanitized.add(sanitizedValue);
            }
        }
        return sanitized;
    }
}
