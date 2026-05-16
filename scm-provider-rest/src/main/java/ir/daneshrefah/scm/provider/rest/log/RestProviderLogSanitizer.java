package ir.daneshrefah.scm.provider.rest.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.provider.rest.config.RestProviderResolvedConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RestProviderLogSanitizer {
    private static final String MASKED = "***";

    private final ObjectMapper objectMapper;

    public Map<String, Object> sanitizeHeaders(Map<String, ?> headers, RestProviderResolvedConfig.Security security) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Set<String> sensitiveHeaders = normalizedSet(security.sensitiveHeaders());
        Map<String, Object> sanitized = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (StringUtils.isBlank(key)) {
                return;
            }
            String normalizedKey = normalizeKey(key);
            sanitized.put(key, sensitiveHeaders.contains(normalizedKey) ? MASKED : sanitizeSimpleValue(value, security.maxBodyLogLength()));
        });
        return sanitized;
    }

    public Object sanitizeBody(Object body, RestProviderResolvedConfig.Security security) {
        return sanitizeBody(null, body, normalizedSet(security.sensitiveBodyKeys()), security.maxBodyLogLength());
    }

    public Object parseJsonIfPossible(String body) {
        if (StringUtils.isBlank(body)) {
            return body;
        }
        try {
            return objectMapper.readValue(body, Object.class);
        } catch (Exception ignored) {
            return body;
        }
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeBody(String key, Object body, Set<String> sensitiveKeys, int maxBodyLogLength) {
        if (key != null && sensitiveKeys.contains(normalizeKey(key))) {
            return MASKED;
        }
        if (body == null) {
            return null;
        }
        if (body instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            map.forEach((childKey, childValue) -> sanitized.put(String.valueOf(childKey),
                    sanitizeBody(String.valueOf(childKey), childValue, sensitiveKeys, maxBodyLogLength)));
            return sanitized;
        }
        if (body instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> sanitizeBody(null, item, sensitiveKeys, maxBodyLogLength))
                    .toList();
        }
        if (body.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(body);
            Object[] sanitized = new Object[length];
            for (int i = 0; i < length; i++) {
                sanitized[i] = sanitizeBody(null, java.lang.reflect.Array.get(body, i), sensitiveKeys, maxBodyLogLength);
            }
            return sanitized;
        }
        return sanitizeSimpleValue(body, maxBodyLogLength);
    }

    private Object sanitizeSimpleValue(Object value, int maxBodyLogLength) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        String text = String.valueOf(value)
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        if (text.length() <= maxBodyLogLength) {
            return text;
        }
        return text.substring(0, maxBodyLogLength) + "...[truncated,len=" + text.length() + "]";
    }

    private Set<String> normalizedSet(Collection<String> items) {
        if (items == null || items.isEmpty()) {
            return Set.of();
        }
        return items.stream()
                .filter(StringUtils::isNotBlank)
                .map(this::normalizeKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private String normalizeKey(String key) {
        return key.replace("-", "")
                .replace("_", "")
                .replace(".", "")
                .toLowerCase(Locale.ROOT);
    }
}
