package ir.daneshrefah.scm.web.observation.propagation;

import java.util.Locale;
import java.util.regex.Pattern;

public record ScmTraceParent(
        String version,
        String traceId,
        String parentId,
        String flags
) {
    private static final Pattern VERSION = Pattern.compile("[0-9a-f]{2}");
    private static final Pattern TRACE_ID = Pattern.compile("[0-9a-f]{32}");
    private static final Pattern PARENT_ID = Pattern.compile("[0-9a-f]{16}");
    private static final Pattern FLAGS = Pattern.compile("[0-9a-f]{2}");

    public ScmTraceParent {
        version = normalize(version);
        traceId = normalize(traceId);
        parentId = normalize(parentId);
        flags = normalize(flags);
        if (!isValidVersion(version) || !isValidTraceId(traceId) || !isValidParentId(parentId) || !isValidFlags(flags)) {
            throw new IllegalArgumentException("Invalid W3C traceparent");
        }
    }

    public static boolean isValidTraceId(String value) {
        String normalized = normalize(value);
        return normalized != null && TRACE_ID.matcher(normalized).matches() && !isAllZeros(normalized);
    }

    public static boolean isValidParentId(String value) {
        String normalized = normalize(value);
        return normalized != null && PARENT_ID.matcher(normalized).matches() && !isAllZeros(normalized);
    }

    private static boolean isValidVersion(String value) {
        String normalized = normalize(value);
        return normalized != null && VERSION.matcher(normalized).matches() && !"ff".equals(normalized);
    }

    private static boolean isValidFlags(String value) {
        String normalized = normalize(value);
        return normalized != null && FLAGS.matcher(normalized).matches();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isAllZeros(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) != '0') {
                return false;
            }
        }
        return true;
    }
}
