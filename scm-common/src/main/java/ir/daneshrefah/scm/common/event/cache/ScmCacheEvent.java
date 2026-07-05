package ir.daneshrefah.scm.common.event.cache;

import ir.daneshrefah.scm.common.event.ScmEvent;
import ir.daneshrefah.scm.common.event.ScmEventMetadata;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ScmCacheEvent(
        ScmCacheEventType type,
        ScmEventMetadata metadata,
        Map<String, Object> attributes
) implements ScmEvent {
    public ScmCacheEvent {
        type = Objects.requireNonNull(type, "type");
        metadata = metadata == null ? ScmEventMetadata.now("cache") : metadata;
        attributes = ScmSafeEventAttributes.copyOf(attributes);
    }

    public static ScmCacheEvent of(ScmCacheEventType type, Map<String, ?> attributes) {
        Map<String, Object> safeAttributes = new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(attributes));
        safeAttributes.put("cache.event.type", type.code());
        return new ScmCacheEvent(type, ScmEventMetadata.now("cache"), safeAttributes);
    }

    @Override
    public String eventType() {
        return type.code();
    }

    @Override
    public Instant occurredAt() {
        return metadata.occurredAt();
    }
}
