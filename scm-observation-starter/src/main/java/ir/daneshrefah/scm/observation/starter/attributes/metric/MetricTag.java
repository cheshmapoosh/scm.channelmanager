package ir.daneshrefah.scm.observation.starter.attributes.metric;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeSensitivity;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeTypes;
import ir.daneshrefah.scm.observation.starter.ObservationStream;

import java.util.Locale;
import java.util.Set;

public final class MetricTag {
    private static final Set<String> FORBIDDEN_NAMES = Set.of(
            "correlation.id",
            "scm.correlation_id",
            "trace.id",
            "span.id",
            "username",
            "user.name",
            "card.no",
            "scm.card.no",
            "account.no",
            "scm.account.no",
            "phone",
            "phone.number",
            "scm.client.phone_number",
            "token",
            "otp",
            "message.sequence.id",
            "scm.message.sequence_id"
    );

    private MetricTag() {
    }

    public static ObservationAttributeKey<String> lowCardinality(
            String name, String owner, String description) {
        if (isForbidden(name)) {
            throw new IllegalArgumentException("High-cardinality metric tag is forbidden: " + name);
        }
        return ObservationAttributeKey.key(
                name,
                ObservationAttributeTypes.KEYWORD,
                owner,
                ObservationAttributePresence.EVENT_OPTIONAL,
                ObservationAttributeSensitivity.RAW,
                0,
                0,
                description,
                ObservationStream.METRIC
        );
    }

    public static ObservationAttributeKey<String> lowCardinality(String name, String description) {
        return lowCardinality(name, "common", description);
    }

    public static boolean isForbidden(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return FORBIDDEN_NAMES.contains(normalized)
                || normalized.contains("username")
                || normalized.contains("card.no")
                || normalized.contains("account.no")
                || normalized.contains("phone")
                || normalized.contains("token")
                || normalized.contains("otp")
                || normalized.contains("message.sequence")
                || normalized.contains("payload")
                || normalized.contains("request.body")
                || normalized.contains("response.body")
                || normalized.contains("raw_request")
                || normalized.contains("raw_response");
    }
}
