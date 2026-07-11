package ir.daneshrefah.scm.observation.starter;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

public class ObsTargetIndexResolver {
    private static final DateTimeFormatter INDEX_HOUR = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");
    public static final String WITH_CHANNEL = "{stream}-scm-{namespace}-{env}-{channelCode}-{yyyy.MM.dd.HH}";
    public static final String WITHOUT_CHANNEL = "{stream}-scm-{namespace}-{env}-{yyyy.MM.dd.HH}";
    private static final Set<String> VALID_ENVIRONMENTS = Set.of("dev", "test", "pilot", "prod");
    private static final Set<String> MISSING_CHANNEL_CODES = Set.of(
            "null",
            "blank",
            "unknown",
            "default",
            "none",
            "n/a",
            "n-a"
    );

    public String resolve(ObservationStream stream, ObservationContext context, Instant timestamp) {
        if (context == null) {
            throw new IllegalArgumentException("Observation context is required to resolve scm.observation.target.index.");
        }
        return resolve(stream, context.namespace(), context.appProfile(), context.channelCode(), timestamp);
    }

    public String resolve(
            ObservationStream stream,
            String namespace,
            String environment,
            String channelCode,
            Instant timestamp
    ) {
        if (stream == null) {
            throw new IllegalArgumentException("Observation stream is required to resolve scm.observation.target.index.");
        }
        String resolvedNamespace = normalizeRequired(namespace, "scm.metadata.namespace");
        String resolvedEnvironment = normalizeEnvironment(environment);
        Instant resolvedTimestamp = timestamp == null ? Instant.now() : timestamp;
        String hour = INDEX_HOUR.withZone(ZoneOffset.UTC).format(resolvedTimestamp);
        String resolvedChannelCode = normalizeChannelCode(channelCode);
        String streamValue = normalize(stream.value());
        if (resolvedChannelCode == null) {
            return normalizeRequired("%s-scm-%s-%s-%s".formatted(streamValue, resolvedNamespace, resolvedEnvironment, hour),
                    "scm.observation.target.index");
        }
        return normalizeRequired("%s-scm-%s-%s-%s-%s".formatted(
                        streamValue, resolvedNamespace, resolvedEnvironment, resolvedChannelCode, hour),
                "scm.observation.target.index");
    }

    public String normalizeNamespace(String value) {
        return normalizeRequired(value, "scm.metadata.namespace");
    }

    public String normalizeEnvironment(String value) {
        String resolved = textOrNull(value);
        if (resolved == null) {
            throw new IllegalArgumentException("spring.profiles.active/SCM_ENV is required to resolve scm.observation.target.index.");
        }
        if (!VALID_ENVIRONMENTS.contains(resolved)) {
            throw new IllegalArgumentException("spring.profiles.active/SCM_ENV must be one of dev, test, pilot, prod to resolve scm.observation.target.index.");
        }
        return resolved;
    }

    public String normalizeChannelCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalize(value);
        return MISSING_CHANNEL_CODES.contains(normalized) ? null : normalized;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required to resolve scm.observation.target.index.");
        }
        return normalize(value);
    }

    private String normalize(String value) {
        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Observation index part resolved to blank.");
        }
        return normalized;
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
