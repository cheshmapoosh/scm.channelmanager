package ir.daneshrefah.scm.docs.client.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ScmDocDescriptor(
        String id,
        String moduleCode,
        String serviceCode,
        String version,
        ScmDocCategory category,
        ScmDocType type,
        Map<String, String> title,
        Map<String, String> description,
        String mediaType,
        String fileName,
        String href,
        int order
) {

    private static final List<String> FALLBACK_LANGUAGES = List.of("fa", "en", "id");

    public ScmDocDescriptor {
        id = requireText(id, "id");
        moduleCode = hasText(moduleCode) ? moduleCode.trim() : "scm";
        serviceCode = trimToNull(serviceCode);
        version = trimToNull(version);
        category = category == null ? ScmDocCategory.GUIDE : category;
        type = type == null ? ScmDocType.MARKDOWN : type;
        title = normalizeMap(title, Map.of("en", id));
        description = normalizeMap(description, Map.of());
        mediaType = hasText(mediaType) ? mediaType.trim() : type.getDefaultMediaType();
        fileName = hasText(fileName) ? fileName.trim() : id + type.getDefaultExtension();
        href = trimToNull(href);
    }

    public String titleFor(String language) {
        return localizedValue(title, language, id);
    }

    public String descriptionFor(String language) {
        return localizedValue(description, language, "");
    }

    public ScmDocDescriptor withHref(String href) {
        return new ScmDocDescriptor(
                id,
                moduleCode,
                serviceCode,
                version,
                category,
                type,
                title,
                description,
                mediaType,
                fileName,
                href,
                order
        );
    }

    public static String localizedValue(Map<String, String> values, String language, String fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }
        String requested = trimToNull(language);
        if (requested != null) {
            String exactValue = valueFor(values, requested);
            if (exactValue != null) {
                return exactValue;
            }
            String languageOnly = requested.split("[-_]")[0];
            String languageValue = valueFor(values, languageOnly);
            if (languageValue != null) {
                return languageValue;
            }
        }
        for (String fallbackLanguage : FALLBACK_LANGUAGES) {
            String fallbackValue = valueFor(values, fallbackLanguage);
            if (fallbackValue != null) {
                return fallbackValue;
            }
        }
        return fallback;
    }

    public static Map<String, String> normalizeMap(Map<String, String> values, Map<String, String> fallback) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = trimToNull(entry.getKey());
                String value = trimToNull(entry.getValue());
                if (key != null && value != null) {
                    normalized.put(key.toLowerCase(), value);
                }
            }
        }
        if (normalized.isEmpty() && fallback != null) {
            normalized.putAll(fallback);
        }
        return Map.copyOf(normalized);
    }

    private static String requireText(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static String valueFor(Map<String, String> values, String language) {
        if (language == null) {
            return null;
        }
        return values.get(language.toLowerCase());
    }
}
