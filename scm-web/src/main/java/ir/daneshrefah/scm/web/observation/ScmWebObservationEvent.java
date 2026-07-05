package ir.daneshrefah.scm.web.observation;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;

import java.util.Map;

public record ScmWebObservationEvent(
        String category,
        String action,
        String outcome,
        String message,
        Map<String, Object> logAttributes,
        Map<String, Object> traceAttributes
) {
    public ScmWebObservationEvent {
        category = textOrDefault(category, "application");
        action = textOrDefault(action, "event");
        outcome = textOrDefault(outcome, "unknown");
        message = textOrDefault(message, action);
        logAttributes = ScmSafeEventAttributes.copyOf(logAttributes);
        traceAttributes = ScmSafeEventAttributes.copyOf(traceAttributes);
    }

    private static String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
