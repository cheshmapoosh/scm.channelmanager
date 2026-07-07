package ir.daneshrefah.scm.observation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

public class ObsTargetIndexResolver {
    private static final DateTimeFormatter INDEX_HOUR = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");
    private static final String DEFAULT_PATTERN_WITH_CHANNEL = "{stream}-scm-{namespace}-{env}-{channelCode}-{yyyy.MM.dd.HH}";
    private static final String DEFAULT_PATTERN_WITHOUT_CHANNEL = "{stream}-scm-{namespace}-{env}-{yyyy.MM.dd.HH}";
    private static final Set<String> MISSING_CHANNEL_CODES = Set.of(
            "null",
            "blank",
            "unknown",
            "default",
            "none",
            "n/a",
            "n-a"
    );

    private final ObservationProperties.TargetIndexProperties indexProperties;

    public ObsTargetIndexResolver() {
        this(null);
    }

    public ObsTargetIndexResolver(ObservationProperties.TargetProperties targetProperties) {
        ObservationProperties.TargetProperties safeTarget =
                targetProperties == null ? new ObservationProperties.TargetProperties() : targetProperties;
        this.indexProperties = safeTarget.getIndex() == null
                ? new ObservationProperties.TargetIndexProperties()
                : safeTarget.getIndex();
    }

    public String resolve(ObservationStream stream, ObservationContext context, Instant timestamp) {
        if (context == null) {
            throw new IllegalArgumentException("Observation context is required to resolve scm.observation.target.index.");
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
            throw new IllegalArgumentException("Observation stream is required to resolve scm.observation.target.index.");
        }
        if (!indexProperties.isEnabled()) {
            throw new IllegalStateException("scm.observation.target.index.enabled must be true to resolve scm.observation.target.index.");
        }
        String resolvedNamespace = normalizeRequired(namespace, "scm.observation.target.namespace");
        String resolvedEnvironment = normalizeRequired(environment, "deployment.environment");
        String resolvedPlatform = normalizeRequired(platform, "scm.platform");
        Instant resolvedTimestamp = timestamp == null ? Instant.now() : timestamp;
        ZoneId resolvedZone = zoneId == null ? ZoneId.of("UTC") : zoneId;
        String hour = INDEX_HOUR.withZone(resolvedZone).format(resolvedTimestamp);
        String resolvedChannelCode = normalizeChannelCode(channelCode);
        String pattern = resolvedChannelCode == null
                ? textOrDefault(indexProperties.getPatternWithoutChannel(), DEFAULT_PATTERN_WITHOUT_CHANNEL)
                : textOrDefault(indexProperties.getPatternWithChannel(), DEFAULT_PATTERN_WITH_CHANNEL);
        return render(pattern, Map.of(
                "stream", normalize(stream.value()),
                "platform", resolvedPlatform,
                "namespace", resolvedNamespace,
                "env", resolvedEnvironment,
                "channelCode", resolvedChannelCode == null ? "" : resolvedChannelCode,
                "yyyy.MM.dd.HH", hour
        ));
    }

    public String normalizeNamespace(String value) {
        return normalizeRequired(value, "scm.observation.target.namespace");
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

    private String render(String pattern, Map<String, String> parts) {
        String rendered = pattern;
        for (Map.Entry<String, String> entry : parts.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return normalizeRequired(rendered, "scm.observation.target.index");
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
