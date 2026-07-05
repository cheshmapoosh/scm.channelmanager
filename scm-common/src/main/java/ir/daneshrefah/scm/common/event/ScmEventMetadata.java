package ir.daneshrefah.scm.common.event;

import java.time.Instant;
import java.util.Objects;

public record ScmEventMetadata(
        Instant occurredAt,
        String source
) {
    public ScmEventMetadata {
        occurredAt = Objects.requireNonNullElseGet(occurredAt, Instant::now);
        source = textOrDefault(source, "application");
    }

    public static ScmEventMetadata now(String source) {
        return new ScmEventMetadata(Instant.now(), source);
    }

    private static String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
