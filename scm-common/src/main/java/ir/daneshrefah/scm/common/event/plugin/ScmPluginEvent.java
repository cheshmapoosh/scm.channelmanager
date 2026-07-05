package ir.daneshrefah.scm.common.event.plugin;

import ir.daneshrefah.scm.common.event.ScmEvent;
import ir.daneshrefah.scm.common.event.ScmEventMetadata;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ScmPluginEvent(
        ScmPluginEventType type,
        ScmEventMetadata metadata,
        Map<String, Object> attributes
) implements ScmEvent {
    public ScmPluginEvent {
        type = Objects.requireNonNull(type, "type");
        metadata = metadata == null ? ScmEventMetadata.now("plugin") : metadata;
        attributes = ScmSafeEventAttributes.copyOf(attributes);
    }

    public static ScmPluginEvent of(ScmPluginEventType type, Map<String, ?> attributes) {
        Map<String, Object> safeAttributes = new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(attributes));
        safeAttributes.put("plugin.event.type", type.code());
        return new ScmPluginEvent(type, ScmEventMetadata.now("plugin"), safeAttributes);
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
