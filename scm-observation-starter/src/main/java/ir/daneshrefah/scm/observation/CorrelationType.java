package ir.daneshrefah.scm.observation;

import java.util.Locale;

public enum CorrelationType {
    LIFECYCLE("lifecycle"),
    REQUEST("request"),
    MESSAGE("message"),
    JOB("job"),
    BATCH("batch"),
    OPERATION("operation"),
    UNKNOWN("unknown");

    private final String value;

    CorrelationType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CorrelationType fromValueOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (CorrelationType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isAllowed(String value) {
        return fromValueOrNull(value) != null;
    }

    public static String valueOrUnknown(String value) {
        CorrelationType type = fromValueOrNull(value);
        return type == null ? UNKNOWN.value : type.value;
    }
}
