package ir.daneshrefah.scm.observation.trace;

import java.util.Locale;
import java.util.Set;

final class TraceAttributeSecurity {
    private static final Set<String> SAFE_JWT_FIELDS = Set.of(
            "scm.auth.jwt.present",
            "scm.auth.jwt.issuer",
            "scm.auth.jwt.subject",
            "scm.auth.jwt.username",
            "scm.auth.jwt.exp",
            "scm.auth.jwt.hash",
            "uaa.jwt.present",
            "uaa.jwt.issuer",
            "uaa.jwt.subject",
            "uaa.jwt.username",
            "uaa.jwt.masked",
            "uaa.jwt.expiration"
    );

    private TraceAttributeSecurity() {
    }

    static boolean isAllowed(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }
        String normalized = fieldName.trim().toLowerCase(Locale.ROOT);
        if (SAFE_JWT_FIELDS.contains(normalized)) {
            return true;
        }
        if (normalized.startsWith("scm.auth.jwt.") || normalized.startsWith("uaa.jwt.")) {
            return false;
        }
        String compact = normalized
                .replace(".", "")
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");
        return !compact.contains("requestbody")
                && !compact.contains("responsebody")
                && !compact.contains("rawrequest")
                && !compact.contains("rawresponse")
                && !compact.contains("rawpayload")
                && !compact.contains("payload")
                && !compact.contains("accesstoken")
                && !containsUnsafeToken(normalized, compact)
                && !normalized.contains("authorization")
                && !normalized.contains("cookie")
                && !normalized.contains("password")
                && !normalized.contains("secret")
                && !hasSegment(normalized, "pin")
                && !hasSegment(normalized, "cvv");
    }

    static boolean isReservedTraceField(String fieldName) {
        if (fieldName == null) {
            return true;
        }
        return switch (fieldName.trim().toLowerCase(Locale.ROOT)) {
            case "@timestamp",
                    "event.category",
                    "event.action",
                    "event.outcome",
                    "message",
                    "correlation.id",
                    "correlation.type",
                    "trace.id",
                    "span.id",
                    "parent.span.id",
                    "span.name",
                    "span.kind",
                    "span.start_time",
                    "span.end_time",
                    "span.duration_ms" -> true;
            default -> false;
        };
    }

    private static boolean containsUnsafeToken(String normalized, String compact) {
        return "jwt".equals(normalized)
                || "token".equals(normalized)
                || compact.equals("jwt")
                || compact.equals("token")
                || compact.endsWith("token")
                || compact.contains("refreshtoken");
    }

    private static boolean hasSegment(String normalized, String segment) {
        String[] parts = normalized.split("[._\\-\\s/]+");
        for (String part : parts) {
            if (segment.equals(part)) {
                return true;
            }
        }
        return false;
    }
}
