package ir.daneshrefah.scm.common.event;

import java.time.Instant;
import java.util.Map;

public interface ScmEvent {
    String eventType();

    Instant occurredAt();

    Map<String, Object> attributes();
}
