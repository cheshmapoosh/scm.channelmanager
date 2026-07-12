package ir.daneshrefah.scm.observation.starter;

import java.util.Locale;

public final class TraceFlags {
    public static final String DEFAULT = "01";

    private TraceFlags() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.length() == 2 && isHex(normalized.charAt(0)) && isHex(normalized.charAt(1))
                ? normalized
                : null;
    }

    public static String normalizeOrDefault(String value) {
        String normalized = normalize(value);
        return normalized == null ? DEFAULT : normalized;
    }

    public static Boolean sampled(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        return (Integer.parseInt(normalized, 16) & 0x01) == 0x01;
    }

    public static String withSamplingDecision(String value, Boolean sampled) {
        String normalized = normalizeOrDefault(value);
        if (sampled == null) {
            return normalized;
        }
        int flags = Integer.parseInt(normalized, 16);
        flags = sampled ? flags | 0x01 : flags & ~0x01;
        return String.format(Locale.ROOT, "%02x", flags & 0xff);
    }

    private static boolean isHex(char value) {
        return value >= '0' && value <= '9' || value >= 'a' && value <= 'f';
    }
}
