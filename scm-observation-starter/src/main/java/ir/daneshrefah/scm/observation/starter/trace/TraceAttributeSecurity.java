package ir.daneshrefah.scm.observation.starter.trace;

import java.util.Locale;
import java.util.Set;

final class TraceAttributeSecurity {
    private static final Set<String> SAFE_JWT_FIELDS = Set.of(
            "scm.jwt.scope",
            "scm.jwt.issuer",
            "scm.jwt.issue_at",
            "scm.jwt.expire_at",
            "scm.jwt.audience",
            "scm.jwt.generator"
    );
    private static final Set<String> GATEWAY_JWT_CONTEXT_FIELDS = Set.of(
            "scm.user.nickname",
            "scm.jwt.scope",
            "scm.jwt.issuer",
            "scm.client.address",
            "scm.jwt.issue_at",
            "scm.jwt.expire_at",
            "scm.channel.code",
            "scm.jwt.audience",
            "scm.jwt.generator",
            "scm.auth.txn_method",
            "scm.auth.login_method"
    );

    private TraceAttributeSecurity() {
    }

    static boolean isAllowed(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }
        String normalized = fieldName.trim().toLowerCase(Locale.ROOT);
        if ("event.category".equals(normalized)) {
            return false;
        }
        if (SAFE_JWT_FIELDS.contains(normalized)) {
            return true;
        }
        if (normalized.startsWith("scm.jwt.")
                || normalized.startsWith("scm.auth.jwt.")
                || normalized.startsWith("uaa.jwt.")) {
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
                    "event.stream",
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
                    "span.duration_ms",
                    "span.events" -> true;
            default -> false;
        };
    }

    static boolean isAllowedSpanEventAttribute(String fieldName) {
        if (!isAllowed(fieldName)) {
            return false;
        }
        String normalized = fieldName.trim().toLowerCase(Locale.ROOT);
        if (GATEWAY_JWT_CONTEXT_FIELDS.contains(normalized)) {
            return false;
        }
        return switch (normalized) {
            case "@timestamp",
                    "event.stream",
                    "event.category",
                    "event.action",
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
                    "span.duration_ms",
                    "span.events" -> false;
            default -> true;
        };
    }

    static boolean isGatewayOnlyJwtContextField(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }
        String normalized = fieldName.trim().toLowerCase(Locale.ROOT);
        return GATEWAY_JWT_CONTEXT_FIELDS.contains(normalized)
                && !"scm.channel.code".equals(normalized);
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
