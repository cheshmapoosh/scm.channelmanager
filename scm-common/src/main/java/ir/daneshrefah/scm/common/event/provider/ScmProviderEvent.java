package ir.daneshrefah.scm.common.event.provider;

import ir.daneshrefah.scm.common.event.ScmEvent;
import ir.daneshrefah.scm.common.event.ScmEventMetadata;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ScmProviderEvent(
        ScmProviderEventType type,
        ScmEventMetadata metadata,
        Map<String, Object> attributes
) implements ScmEvent {
    public ScmProviderEvent {
        type = Objects.requireNonNull(type, "type");
        metadata = metadata == null ? ScmEventMetadata.now("provider") : metadata;
        attributes = ScmSafeEventAttributes.copyOf(attributes);
    }

    public static ScmProviderEvent of(ScmProviderEventType type, Map<String, ?> attributes) {
        Map<String, Object> safeAttributes = new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(attributes));
        safeAttributes.put("provider.event.type", type.code());
        return new ScmProviderEvent(type, ScmEventMetadata.now("provider"), safeAttributes);
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
