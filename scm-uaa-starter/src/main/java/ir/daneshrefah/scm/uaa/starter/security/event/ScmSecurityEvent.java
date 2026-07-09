package ir.daneshrefah.scm.uaa.starter.security.event;

import ir.daneshrefah.scm.common.event.ScmEvent;
import ir.daneshrefah.scm.common.event.ScmEventMetadata;
import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ScmSecurityEvent(
        ScmSecurityEventType type,
        ScmEventMetadata metadata,
        Map<String, Object> attributes
) implements ScmEvent {
    public ScmSecurityEvent {
        type = Objects.requireNonNull(type, "type");
        metadata = metadata == null ? ScmEventMetadata.now("scm-uaa-starter") : metadata;
        attributes = ScmSafeEventAttributes.copyOf(attributes);
    }

    public static ScmSecurityEvent of(ScmSecurityEventType type, Map<String, ?> attributes) {
        Map<String, Object> safeAttributes = new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(attributes));
        safeAttributes.put("security.event.type", type.code());
        return new ScmSecurityEvent(type, ScmEventMetadata.now("scm-uaa-starter"), safeAttributes);
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
