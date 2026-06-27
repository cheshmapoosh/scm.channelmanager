package ir.daneshrefah.scm.observation.attributes.audit;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationAttributeSensitivity;
import ir.daneshrefah.scm.observation.ObservationAttributeType;
import ir.daneshrefah.scm.observation.ObservationAttributeTypes;
import ir.daneshrefah.scm.observation.ObservationStream;

public final class AuditAttribute {
    private AuditAttribute() {
    }

    public static ObservationAttributeKey<String> keyword(
            String name, String owner, ObservationAttributePresence presence, String description) {
        return attribute(name, ObservationAttributeTypes.KEYWORD, owner, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    public static ObservationAttributeKey<String> keyword(
            String name, ObservationAttributePresence presence, String description) {
        return keyword(name, "common", presence, description);
    }

    public static ObservationAttributeKey<String> text(
            String name, String owner, ObservationAttributePresence presence, String description) {
        return attribute(name, ObservationAttributeTypes.TEXT, owner, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    public static ObservationAttributeKey<String> text(
            String name, ObservationAttributePresence presence, String description) {
        return text(name, "common", presence, description);
    }

    public static ObservationAttributeKey<String> date(
            String name, String owner, ObservationAttributePresence presence, String description) {
        return attribute(name, ObservationAttributeTypes.DATE, owner, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    public static ObservationAttributeKey<String> date(
            String name, ObservationAttributePresence presence, String description) {
        return date(name, "common", presence, description);
    }

    public static ObservationAttributeKey<Integer> integerNumber(
            String name, String owner, ObservationAttributePresence presence, String description) {
        return attribute(name, ObservationAttributeTypes.INTEGER, owner, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    public static ObservationAttributeKey<Long> longNumber(
            String name, String owner, ObservationAttributePresence presence, String description) {
        return attribute(name, ObservationAttributeTypes.LONG, owner, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    public static ObservationAttributeKey<String> maskedKeyword(
            String name,
            String owner,
            ObservationAttributePresence presence,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        return attribute(name, ObservationAttributeTypes.KEYWORD, owner, presence,
                masking(visiblePrefixLength, visibleSuffixLength), visiblePrefixLength, visibleSuffixLength, description);
    }

    public static ObservationAttributeKey<String> maskedKeyword(
            String name,
            ObservationAttributePresence presence,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        return maskedKeyword(name, "common", presence, visiblePrefixLength, visibleSuffixLength, description);
    }

    public static ObservationAttributeKey<String> secureKeyword(
            String name, String owner, ObservationAttributePresence presence, String description) {
        return attribute(name, ObservationAttributeTypes.KEYWORD, owner, presence,
                ObservationAttributeSensitivity.SECURE, 0, 0, description);
    }

    public static ObservationAttributeKey<String> secureKeyword(
            String name, ObservationAttributePresence presence, String description) {
        return secureKeyword(name, "common", presence, description);
    }

    private static <T> ObservationAttributeKey<T> attribute(
            String name,
            ObservationAttributeType<T> type,
            String owner,
            ObservationAttributePresence presence,
            ObservationAttributeSensitivity sensitivity,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        return ObservationAttributeKey.key(name, type, owner, presence, sensitivity,
                visiblePrefixLength, visibleSuffixLength, description, ObservationStream.AUDIT);
    }

    private static ObservationAttributeSensitivity masking(int prefix, int suffix) {
        if (prefix > 0 && suffix > 0) {
            return ObservationAttributeSensitivity.MASK_PREFIX_SUFFIX;
        }
        if (prefix > 0) {
            return ObservationAttributeSensitivity.MASK_PREFIX;
        }
        if (suffix > 0) {
            return ObservationAttributeSensitivity.MASK_SUFFIX;
        }
        return ObservationAttributeSensitivity.MASK_PREFIX_SUFFIX;
    }
}
