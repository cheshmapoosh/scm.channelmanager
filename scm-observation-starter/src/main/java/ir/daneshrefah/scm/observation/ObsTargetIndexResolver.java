package ir.daneshrefah.scm.observation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

public class ObsTargetIndexResolver {
    private static final DateTimeFormatter INDEX_HOUR = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");
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
            throw new IllegalArgumentException("Observation context is required to resolve scm.obs.target.index.");
        }
        return resolve(stream, context.platform(), context.namespace(), context.appProfile(),
                context.channelCode(), timestamp, context.observationZoneId());
    }

    public String resolve(
            ObservationStream stream,
            String platform,
            String namespace,
            String environment,
            String channelCode,
            Instant timestamp
    ) {
        return resolve(stream, platform, namespace, environment, channelCode, timestamp, ZoneId.of("UTC"));
    }

    public String resolve(
            ObservationStream stream,
            String platform,
            String namespace,
            String environment,
            String channelCode,
            Instant timestamp,
            ZoneId zoneId
    ) {
        if (stream == null) {
            throw new IllegalArgumentException("Observation stream is required to resolve scm.obs.target.index.");
        }
        String resolvedNamespace = normalizeRequired(namespace, "scm.obs.target.namespace");
        String resolvedEnvironment = normalizeRequired(environment, "deployment.environment");
        String resolvedPlatform = normalizeRequired(platform, "scm.platform");
        Instant resolvedTimestamp = timestamp == null ? Instant.now() : timestamp;
        ZoneId resolvedZone = zoneId == null ? ZoneId.of("UTC") : zoneId;
        String hour = INDEX_HOUR.withZone(resolvedZone).format(resolvedTimestamp);
        String resolvedChannelCode = normalizeChannelCode(channelCode);
        String prefix = stream.value() + "-" + resolvedPlatform + "-" + resolvedNamespace + "-" + resolvedEnvironment;
        return resolvedChannelCode == null
                ? prefix + "-" + hour
                : prefix + "-" + resolvedChannelCode + "-" + hour;
    }

    public String normalizeNamespace(String value) {
        return normalizeRequired(value, "scm.obs.target.namespace");
    }

    public String normalizePlatform(String value) {
        return normalizeRequired(value, "scm.platform");
    }

    public String normalizeEnvironment(String value) {
        return normalizeRequired(value, "deployment.environment");
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
            throw new IllegalArgumentException(fieldName + " is required to resolve scm.obs.target.index.");
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
}
